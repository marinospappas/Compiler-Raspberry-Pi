package mpdev.compilerv5.parser.declarations

import mpdev.compilerv3.CompilerContext
import mpdev.compilerv3.scanner.*
import mpdev.compilerv3.util.Utils.Companion.abort

/**
 * parse variables declarations
 * <variable declarations> ::= var <identifier> [ = <value> ] [ , <identifier> [ = <value> ] ] *
 */
class VariablesParser(context: CompilerContext) {

    val scanner = context.scanner
    val code = context.codeModule

    val declarationUtils = DeclarationUtils(context)

    fun parse(scope: VarScope = VarScope.global, blockName: String = "") {
        while (scanner.lookahead().encToken == Kwd.varDecl) {
            do {
                scanner.match()
                parseOneVarDecl(scope, blockName)
            } while (scanner.lookahead().encToken == Kwd.commaToken)
        }
    }

    /** parse one variable declaration */
    private fun parseOneVarDecl(scope: VarScope, blockName: String) {
        val varName = scanner.match(Kwd.identifier).value
        var varScope = scope
        when (scanner.lookahead().encToken) {     // check for "package-global" or "external" symbol
            Kwd.global -> {
                scanner.match(); varScope = VarScope.packageGlobal
            }

            Kwd.external -> {
                scanner.match(); varScope = VarScope.external
            }

            else -> {}
        }
        scanner.match(Kwd.colonToken)
        when (scanner.lookahead().encToken) {
            Kwd.intType -> parseOneNumVarDecl(varName, varScope, DataType.int, code.INT_SIZE)
            Kwd.byteType -> parseOneNumVarDecl(varName, varScope, DataType.byte, code.BYTE_SIZE)
            Kwd.memPtrType -> parseOneNumVarDecl(varName, varScope, DataType.memptr, code.PTR_SIZE)
            Kwd.intArrayType -> parseOneArrayDecl(varName, varScope, DataType.intarray)
            Kwd.byteArrayType -> parseOneArrayDecl(varName, varScope, DataType.bytearray)
            Kwd.stringType -> parseOneStringDecl(varName, varScope)
            else -> scanner.expected("variable type (int, byte, intarray, bytearray, pointer or string)")
        }
        if (scope == VarScope.local) {      // add any local vars to the local vars map for this block
            val localVarsList: MutableList<String> = localVarsMap[blockName] ?: mutableListOf()
            localVarsList.add(varName)
            localVarsMap[blockName] = localVarsList
        }
    }

    /** parse one numeric var declaration - used for int, byte, pointer */
    private fun parseOneNumVarDecl(varName: String, scope: VarScope, type: DataType, size: Int) {
        scanner.match()
        val initValue = initIntVar()
        declarationUtils.declareVar(varName, type, initValue, size, scope)
    }

    /** parse one pointer var declaration */
    private fun parseOneArrayDecl(varName: String, scope: VarScope, type: DataType) {
        scanner.match()
        scanner.match(Kwd.leftParen)
        val size = scanner.match(Kwd.number).value
        scanner.match(Kwd.rightParen)
        val initValues = initArrayVar(size.toInt())
        declarationUtils.declareVar(varName, type, initValues, size.toInt(), scope)
    }

    /** parse one string var declaration */
    private fun parseOneStringDecl(varName: String, scope: VarScope) {
        var initValue = ""
        var varLength = 0
        scanner.match()
        if (scanner.lookahead().encToken == Kwd.equalsOp) {
            scanner.match()
            initValue = initStringVar()
            varLength = initValue.length
        } else
            if (scanner.lookahead().encToken == Kwd.leftParen) {
                scanner.match()
                varLength = scanner.match(Kwd.number).value.toInt()
                scanner.match(Kwd.rightParen)
            }
        if (initValue == "" && varLength == 0)
            abort("line ${scanner.currentLineNumber}: string variable $varName has neither initial value nor length set")
        declarationUtils.declareVar(varName, DataType.string, initValue, varLength, scope)
    }

    /** initialisation for int vars */
    private fun initIntVar(): String {
        if (scanner.lookahead().encToken == Kwd.equalsOp) {
            scanner.match()
            var sign = ""
            if (scanner.lookahead().type == TokType.addOps) {
                val plusMinus = scanner.match().value
                if (plusMinus == "-")
                    sign = "-"
            }
            return sign + scanner.match(Kwd.number).value
        } else
            return ""
    }

    /** initialisation for string vars */
    private fun initStringVar(): String {
        return scanner.match(Kwd.string).value
    }

    /** initialisation for array vars (int or byte) */
    private fun initArrayVar(size: Int): String {
        if (scanner.lookahead().encToken != Kwd.equalsOp)
            return ""
        scanner.match()
        scanner.match(Kwd.startBlock)
        var initValues = ""
        for (i in 1..size) {
            var sign = ""
            if (scanner.lookahead().type == TokType.addOps) {
                val plusMinus = scanner.match().value
                if (plusMinus == "-")
                    sign = "-"
            }
            initValues += sign + scanner.match(Kwd.number).value
            if (i < size) {
                scanner.match(Kwd.commaToken)
                initValues += ", "
            }
        }
        scanner.match(Kwd.endBlock)
        return initValues
    }
}
