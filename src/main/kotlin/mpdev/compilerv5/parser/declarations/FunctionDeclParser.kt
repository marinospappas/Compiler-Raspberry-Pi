package mpdev.compilerv5.parser.declarations


import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.scanner.*
import mpdev.compilerv5.util.Utils.Companion.abort

class FunctionDeclParser(private val context: CompilerContext) {

    var funName: String = ""
    var hasReturn: Boolean = false

    private val scanner = Config.scanner
    private val code = Config.codeModule
    private val labelHandler = Config.labelHandler
    private val contrStructParser = Config.controlStructureParser
    private val declarationUtils = DeclarationUtils()

    /**
     * parse a function declaration
     * <function declaration> ::= fun <identifier> ( ) <block>
     */
    fun parse() {
        while (scanner.lookahead().encToken == Kwd.funDecl) {
            var isExternal = false
            var isPackageGlobal = false
            scanner.match()
            val functionName = scanner.match(Kwd.identifier).value
            labelHandler.labelPrefix = functionName        // set label prefix and label index to function name
            labelHandler.labelIndx = 0
            funName = functionName      // set global var s that we know which function we are parsing
            scanner.match(Kwd.leftParen)
            parseFunParams(functionName)
            scanner.match(Kwd.rightParen)
            when (scanner.lookahead().encToken) {     // check for "package-global" or "external" function
                Kwd.global -> { scanner.match(); isPackageGlobal = true }
                Kwd.external -> { scanner.match(); isExternal = true }
                else -> {}
            }
            scanner.match(Kwd.colonToken)
            var funType: DataType = DataType.void
            when (scanner.lookahead().encToken) {
                Kwd.intType -> funType = DataType.int
                Kwd.byteType -> funType = DataType.byte
                Kwd.intArrayType -> funType = DataType.intarray
                Kwd.byteArrayType -> funType = DataType.bytearray
                Kwd.memPtrType -> funType = DataType.memptr
                Kwd.stringType -> funType = DataType.string
                Kwd.voidType -> funType = DataType.void
                else -> scanner.expected("function type (int, byte, intarray, bytearray, string, pointer or void)")
            }
            scanner.match()
            if (identifiersMap[functionName] != null)
                abort("line ${scanner.currentLineNumber}: identifier $functionName already declared")
            identifiersMap[functionName] = IdentifierDecl(TokType.function, funType)
            if (!isExternal) {    // external functions do not have body
                declarationUtils.declareFun(functionName, isPackageGlobal)
                storeParamsToStack(functionName)
                parseFunctionBlock()
            }
        }
    }

    /** parse function parameters */
    private fun parseFunParams(functionName: String) {
        var paramCount = 0
        val paramTypesList = mutableListOf<FunctionParameter>()
        if (scanner.lookahead().encToken == Kwd.identifier) {
            do {
                if (paramCount++ >= code.MAX_FUN_PARAMS)
                    abort("line ${scanner.currentLineNumber}: a function can have only up to ${code.MAX_FUN_PARAMS} parameters maximum")
                if (scanner.lookahead().encToken == Kwd.commaToken)
                    scanner.match()
                paramTypesList.add(parseOneFunParam())
            } while (scanner.lookahead().encToken == Kwd.commaToken)
        }
        funParamsMap[functionName] = paramTypesList
    }

    /** parse one function parameter - returns the type for this parameter */
    private fun parseOneFunParam(): FunctionParameter {
        val paramName = scanner.match(Kwd.identifier).value
        if (identifiersMap[paramName] != null)
            abort("line ${scanner.currentLineNumber}: parameter name $paramName has already been declared")
        scanner.match(Kwd.colonToken)
        var paramType = DataType.none
        when (scanner.lookahead().encToken) {
            Kwd.intType -> paramType = DataType.int
            Kwd.byteType -> paramType = DataType.byte
            Kwd.intArrayType -> paramType = DataType.intarray
            Kwd.byteArrayType -> paramType = DataType.bytearray
            Kwd.memPtrType -> paramType = DataType.memptr
            Kwd.stringType -> paramType = DataType.string
            else -> scanner.expected("variable type (int, byte, intarray, bytearray, string, pointer or void)")
        }
        scanner.match()
        return FunctionParameter(paramName, paramType)
    }

    /** transfer the function parameters to stack */
    private fun storeParamsToStack(functionName: String) {
        val paramsList = funParamsMap[functionName] ?: listOf()
        for (i in paramsList.indices) {
            val foundOffset = code.isFunParamInStack(i)
            if (foundOffset < 0) {   // parameter in register
                val paramVarOffs = code.allocateStackVar(code.INT_SIZE)
                identifiersMap[paramsList[i].name] = IdentifierDecl(
                    TokType.variable,
                    paramsList[i].type,
                    initialised = true,
                    size = code.INT_SIZE,
                    isStackVar = true,
                    stackOffset = paramVarOffs,
                    canAssign = setOf(DataType.intarray, DataType.bytearray, DataType.memptr, DataType.string).contains(
                        paramsList[i].type
                    )
                )
                code.storeFunParamToStack(i, paramVarOffs)
                code.outputCommentNl("parameter ${paramsList[i].name} offset from frame ${paramVarOffs}")
            } else {          // parameter in stack already
                identifiersMap[paramsList[i].name] = IdentifierDecl(
                    TokType.variable, paramsList[i].type, initialised = true, size = code.INT_SIZE,
                    isStackVar = true, stackOffset = foundOffset, canAssign = false
                )
                code.outputCommentNl("parameter ${paramsList[i].name} offset from frame ${foundOffset}")
            }
        }
    }

    /** parse a function block */
    private fun parseFunctionBlock() {
        hasReturn = false
        contrStructParser.parseBlock()
        if (!hasReturn)
            abort("line ${scanner.currentLineNumber}: function $funName has no ${scanner.decodeToken(Kwd.retToken)}")
        // clean up declarations of parameters so that the names can be reused in other functions
        funParamsMap[funName]?.forEach { identifiersMap.remove(it.name) }
    }
}
