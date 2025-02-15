package mpdev.compilerv3.chapter_xa_04

/**
 * parse a function declaration
 * <function declaration> ::= fun <identifier> ( ) <block>
 */
fun parseFunDecl() {
    while (inp.lookahead().encToken == Kwd.funDecl) {
        var isExternal = false
        var isPackageGlobal = false
        inp.match()
        val functionName = inp.match(Kwd.identifier).value
        labelPrefix = functionName        // set label prefix and label index to function name
        labelIndx = 0
        funName = functionName      // set global var s that we know which function we are parsing
        inp.match(Kwd.leftParen)
        parseFunParams(functionName)
        inp.match(Kwd.rightParen)
        when (inp.lookahead().encToken) {     // check for "package-global" or "external" function
            Kwd.global -> { inp.match(); isPackageGlobal = true }
            Kwd.external -> { inp.match(); isExternal = true }
            else -> {}
        }
        inp.match(Kwd.colonToken)
        var funType: DataType = DataType.void
        when (inp.lookahead().encToken) {
            Kwd.intType -> funType = DataType.int
            Kwd.byteType -> funType = DataType.byte
            Kwd.intArrayType -> funType = DataType.intarray
            Kwd.byteArrayType -> funType = DataType.bytearray
            Kwd.memPtrType -> funType = DataType.memptr
            Kwd.stringType -> funType = DataType.string
            Kwd.voidType -> funType = DataType.void
            else -> inp.expected("function type (int, byte, intarray, bytearray, string, pointer or void)")
        }
        inp.match()
        if (identifiersMap[functionName] != null)
            abort ("line ${inp.currentLineNumber}: identifier $functionName already declared")
        identifiersMap[functionName] = IdentifierDecl(TokType.function, funType)
        if (!isExternal) {    // external functions do not have body
            declareFun(functionName, isPackageGlobal)
            storeParamsToStack(functionName)
            parseFunctionBlock()
        }
    }
}

/** parse function parameters */
fun parseFunParams(functionName: String) {
    var paramCount = 0
    val paramTypesList = mutableListOf<FunctionParameter>()
    if (inp.lookahead().encToken == Kwd.identifier) {
        do {
            if (paramCount++ >= code.MAX_FUN_PARAMS)
                abort("line ${inp.currentLineNumber}: a function can have only up to ${code.MAX_FUN_PARAMS} parameters maximum")
            if (inp.lookahead().encToken == Kwd.commaToken)
                inp.match()
            paramTypesList.add(parseOneFunParam())
        } while (inp.lookahead().encToken == Kwd.commaToken)
    }
    funParamsMap[functionName] = paramTypesList
}

/** parse one function parameter - returns the type for this parameter */
fun parseOneFunParam(): FunctionParameter {
    val paramName = inp.match(Kwd.identifier).value
    if (identifiersMap[paramName] != null)
        abort("line ${inp.currentLineNumber}: parameter name $paramName has already been declared")
    inp.match(Kwd.colonToken)
    var paramType = DataType.none
    when (inp.lookahead().encToken) {
        Kwd.intType -> paramType = DataType.int
        Kwd.byteType -> paramType = DataType.byte
        Kwd.intArrayType -> paramType = DataType.intarray
        Kwd.byteArrayType -> paramType = DataType.bytearray
        Kwd.memPtrType -> paramType = DataType.memptr
        Kwd.stringType -> paramType = DataType.string
        else -> inp.expected("variable type (int, byte, intarray, bytearray, string, pointer or void)")
    }
    inp.match()
    return FunctionParameter(paramName, paramType)
}

/** transfer the function parameters to stack */
fun storeParamsToStack(functionName: String) {
    val paramsList = funParamsMap[functionName] ?: listOf()
    for (i in paramsList.indices) {
        val foundOffset = code.isFunParamInStack(i)
        if (foundOffset < 0) {   // parameter in register
            val paramVarOffs = code.allocateStackVar(code.INT_SIZE)
            identifiersMap[paramsList[i].name] = IdentifierDecl(
                TokType.variable, paramsList[i].type, initialised = true, size = code.INT_SIZE, isStackVar = true,
                stackOffset = paramVarOffs, canAssign = setOf(DataType.intarray, DataType.bytearray, DataType.memptr, DataType.string).contains(paramsList[i].type)
            )
            code.storeFunParamToStack(i, paramVarOffs)
            code.outputCommentNl("parameter ${paramsList[i].name} offset from frame ${paramVarOffs}")
        }
        else {          // parameter in stack already
            identifiersMap[paramsList[i].name] = IdentifierDecl(
                TokType.variable, paramsList[i].type, initialised = true, size = code.INT_SIZE,
                isStackVar = true, stackOffset = foundOffset, canAssign = false
            )
            code.outputCommentNl("parameter ${paramsList[i].name} offset from frame ${foundOffset}")
        }
    }
}

/** parse a function block */
fun parseFunctionBlock() {
    hasReturn = false
    parseBlock()
    if (!hasReturn)
        abort("line ${inp.currentLineNumber}: function $funName has no ${inp.decodeToken(Kwd.retToken)}")
    // clean up declarations of parameters so that the names can be reused in other functions
    funParamsMap[funName]?.forEach { identifiersMap.remove(it.name) }
}
