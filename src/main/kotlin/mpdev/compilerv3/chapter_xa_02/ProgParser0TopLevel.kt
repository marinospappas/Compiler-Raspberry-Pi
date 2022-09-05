package mpdev.compilerv3.chapter_xa_02

/**
 * Program parsing - module 0
 * Top Level - program structure
 */

/**
 * parse a program
 * <program> ::= <prog header> [ <var declarations> ] [ <fun declarations> ] <main block> <prog end>
 */
fun parseProgram() {
    parseProgHeader()
    if (inp.lookahead().encToken == Kwd.varDecl)
        parseVarDecl()
    code.funInit()
    if (inp.lookahead().encToken == Kwd.funDecl)
        parseFunDecl()
    parseMainBlock()
    parseStringConstants()
    parseProgEnd()
}

/**
 * parse program header
 * <program header> ::= program <identifier>
 */
fun parseProgHeader() {
    inp.match(Kwd.startOfProgram)
    code.progInit(inp.match(Kwd.identifier).value)
}

/**
 * parse variables declarations
 * <variable declarations> ::= var <identifier> [ = <value> ] [ , <identifier> [ = <value> ] ] *
 */
fun parseVarDecl(scope: VarScope = VarScope.global, blockName: String = "") {
    while (inp.lookahead().encToken == Kwd.varDecl) {
        do {
            inp.match()
            parseOneVarDecl(scope, blockName)
        } while (inp.lookahead().encToken == Kwd.commaToken)
    }
}

/** parse one variable declaration */
fun parseOneVarDecl(scope: VarScope, blockName: String) {
    val varName = inp.match(Kwd.identifier).value
    inp.match(Kwd.colonToken)
    when (inp.lookahead().encToken) {
        Kwd.intType -> parseOneIntDecl(varName, scope)
        Kwd.stringType -> parseOneStringDecl(varName, scope)
        else -> inp.expected("variable type (int or string)")
    }
    if (scope == VarScope.local) {      // add any local vars to the local vars map for this block
        val localVarsList: MutableList<String> = localVarsMap[blockName] ?: mutableListOf()
        localVarsList.add(varName)
        localVarsMap[blockName] = localVarsList
    }
}

/** parse one int var declaration */
fun parseOneIntDecl(varName: String, scope: VarScope) {
    var initValue = ""
    inp.match()
    if (inp.lookahead().encToken == Kwd.equalsOp) {
        inp.match()
        initValue = initIntVar()
    }
    declareVar(varName, DataType.int, initValue, code.INT_SIZE, scope)
}

/** parse one string var declaration */
fun parseOneStringDecl(varName: String, scope: VarScope) {
    var initValue = ""
    var varLength = 0
    inp.match()
    if (inp.lookahead().encToken == Kwd.equalsOp) {
        inp.match()
        initValue = initStringVar()
        varLength = initValue.length
    }
    else
    if (inp.lookahead().encToken == Kwd.leftParen) {
        inp.match()
        varLength = inp.match(Kwd.number).value.toInt()
        inp.match(Kwd.rightParen)
    }
    if (initValue == "" && varLength == 0)
        abort("line ${inp.currentLineNumber}: string variable $varName has neither initial value nor length set")
    declareVar(varName, DataType.string, initValue, varLength, scope)
}

/** initialisation for int vars */
fun initIntVar(): String {
    var sign = ""
    if (inp.lookahead().type == TokType.addOps) {
        val plusMinus = inp.match().value
        if (plusMinus == "-")
            sign = "-"
    }
    return sign + inp.match(Kwd.number).value
}

/** initialisation for string vars */
fun initStringVar(): String {
    return inp.match(Kwd.string).value
}

/**
 * parse a function declaration
 * <function declaration> ::= fun <identifier> ( ) <block>
 */
fun parseFunDecl() {
    while (inp.lookahead().encToken == Kwd.funDecl) {
        inp.match()
        val functionName = inp.match(Kwd.identifier).value
        labelPrefix = functionName        // set label prefix and label index to function name
        labelIndx = 0
        funName = functionName      // set global var so that we know which function we are parsing
        inp.match(Kwd.leftParen)
        parseFunParams(functionName)
        inp.match(Kwd.rightParen)
        inp.match(Kwd.colonToken)
        var funType: DataType = DataType.void
        when (inp.lookahead().encToken) {
            Kwd.intType -> funType = DataType.int
            Kwd.stringType -> funType = DataType.string
            Kwd.voidType -> funType = DataType.void
            else -> inp.expected("function type (int, string or void)")
        }
        inp.match()
        declareFun(functionName, funType)
        storeParamsToStack(functionName)
        parseFunctionBlock()
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
        Kwd.stringType -> paramType = DataType.string
        else -> inp.expected("variable type (int or string)")
    }
    inp.match()
    return FunctionParameter(paramName, paramType)
}

/** transfer the function parameters to stack */
fun storeParamsToStack(functionName: String) {
    val paramsList = funParamsMap[functionName] ?: listOf()
    for (i in paramsList.indices) {
        val paramVarOffs = code.allocateStackVar(code.INT_SIZE)
        identifiersMap[paramsList[i].name] = IdentifierDecl(
            TokType.variable, paramsList[i].type, initialised = true, size = code.INT_SIZE,
            isStackVar = true, stackOffset = paramVarOffs, canAssign = false
        )
        code.storeFunParamToStack(i, paramVarOffs)
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

/**
 * parse main block
 * <main block> ::= main <block>
 */
fun parseMainBlock() {
    labelPrefix = MAIN_BLOCK        // set label prefix and label index
    labelIndx = 0
    inp.match(Kwd.mainToken)
    code.mainInit()
    parseBlock()
    code.mainEnd()
}

/**
 * parse program end
 * <program end> ::= endprogram
 */
fun parseProgEnd() {
    inp.match(Kwd.endOfProgram)
    code.progEnd()
    inp.match(Kwd.endOfInput)
}

/** add any string constants at the end of the assembler output */
fun parseStringConstants() {
    code.stringConstantsDataSpace()
    if (stringConstants.isEmpty())
        return
    code.outputCommentNl("constant string values go here")
    for (s in stringConstants.keys) {
        stringConstants[s]?.let {
            if (it.isNotEmpty() && it[0] >= ' ')
                code.declareString(s, it, 0)
            else
                code.declareString(s, "", it.length)
        }
    }
}

