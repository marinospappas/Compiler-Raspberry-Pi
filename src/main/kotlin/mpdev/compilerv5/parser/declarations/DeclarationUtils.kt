package mpdev.compilerv5.parser.declarations

import mpdev.compilerv3.CompilerContext
import mpdev.compilerv3.scanner.*
import mpdev.compilerv3.util.Utils.Companion.abort

/**
 * Support for variables and functions declarations
 */
class DeclarationUtils(context: CompilerContext) {

    private val scanner = context.scanner
    private val code = context.codeModule

    var hasReturn: Boolean = false
    var funName: String = ""

    /** process a variable declaration */
    fun declareVar(name: String, type: DataType, initValue: String, size: Int, scope: VarScope) {
        // check for duplicate var declaration
        if (identifiersMap[name] != null)
            abort("line ${scanner.currentLineNumber}: identifier $name already declared")
        when (scope) {
            VarScope.packageGlobal -> declarePackageGlobalVar(name, type, initValue, size)
            VarScope.global -> declareGlobalVar(name, type, initValue, size)
            VarScope.local -> declareLocalVar(name, type, initValue, size)
            VarScope.external -> declareExternalVar(name, type, "", size)
        }
    }

    /** process a function declaration */
    fun declareFun(name: String, isPackageGlobal: Boolean) {
        code.outputCodeNl()
        if (isPackageGlobal)
            code.globalSymbol(name)
        code.declareAsmFun(name)
    }

    /** declare a package-global variable */
    private fun declarePackageGlobalVar(name: String, type: DataType, initValue: String, size: Int) {
        code.globalSymbol(name)
        declareGlobalVar(name, type, initValue, size)
    }

    /** declare a global variable */
    private fun declareGlobalVar(name: String, type: DataType, initValue: String, size: Int) {
        identifiersMap[name] = IdentifierDecl(TokType.variable, type, initValue != "", size)
        when (type) {
            DataType.int, DataType.memptr -> code.declareInt(name, initValue)
            DataType.byte -> code.declareByte(name, initValue)
            DataType.intarray -> code.declareIntArray(name, size.toString(), initValue)
            DataType.bytearray -> code.declareByteArray(name, size.toString(), initValue)
            DataType.string -> code.declareString(name, initValue, size)
            else -> return
        }
    }

    /** declare a local variable */
    private fun declareLocalVar(name: String, type: DataType, initValue: String, length: Int) {
        val stackOffset: Int
        val lengthRoundedToWord = (length / code.INT_SIZE + 1) * code.INT_SIZE
        when (type) {
            DataType.int, DataType.memptr -> {
                stackOffset = code.allocateStackVar(code.INT_SIZE)
                initLocalIntVar(stackOffset, initValue)
            }

            DataType.intarray, DataType.bytearray -> stackOffset = code.allocateStackVar(lengthRoundedToWord)
            DataType.string -> {
                stackOffset = code.allocateStackVar(code.PTR_SIZE)
                initLocalStringVar(name, stackOffset, initValue, lengthRoundedToWord)
            }

            else -> return
        }
        identifiersMap[name] = IdentifierDecl(
            TokType.variable,
            type,
            initialised = true,
            size = lengthRoundedToWord,
            isStackVar = true,
            stackOffset = stackOffset
        )
        code.outputCommentNl("local var ${name} offset from frame ${stackOffset}")
    }

    /** declare an external variable */
    private fun declareExternalVar(name: String, type: DataType, initValue: String, length: Int) {
        identifiersMap[name] = IdentifierDecl(TokType.variable, type, initValue != "", length)
    }

    /** initialise a local int var */
    private fun initLocalIntVar(stackOffset: Int, initValue: String) {
        if (initValue.isEmpty())
            return
        code.initStackVarInt(stackOffset, initValue)
    }

    /** initialise a local string var */
    private fun initLocalStringVar(name: String, stackOffset: Int, initValue: String, length: Int) {
        if (initValue.isEmpty() && length == 0)
            abort("line ${scanner.currentLineNumber}: local variable $name is not initialised")
        var constStringAddress = ""
        // check for the constant string init value
        stringConstants.forEach { (k, v) -> if (v == initValue) constStringAddress = k }
        if (constStringAddress == "") {  // if not found
            // save the string in the map of constant strings
            constStringAddress = STRING_CONST_PREFIX + (++stringCnstIndx).toString()
            stringConstants[constStringAddress] = initValue
        }
        val stringDataOffset = code.allocateStackVar(length)
        code.initStackVarString(stackOffset, stringDataOffset, constStringAddress)
    }

}