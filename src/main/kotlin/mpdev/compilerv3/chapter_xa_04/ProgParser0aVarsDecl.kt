package mpdev.compilerv3.chapter_xa_04

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
    var varScope = scope
    when (inp.lookahead().encToken) {     // check for "package-global" or "external" symbol
        Kwd.global -> { inp.match(); varScope = VarScope.packageGlobal }
        Kwd.external -> { inp.match(); varScope = VarScope.external }
        else -> {}
    }
    inp.match(Kwd.colonToken)
    when (inp.lookahead().encToken) {
        Kwd.intType -> parseOneIntDecl(varName, varScope)
        Kwd.intPtrType -> parseOnePtrDecl(varName, varScope)
        Kwd.stringType -> parseOneStringDecl(varName, varScope)
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

/** parse one pointer var declaration */
fun parseOnePtrDecl(varName: String, scope: VarScope) {
    var initValue = ""
    inp.match()
    if (inp.lookahead().encToken == Kwd.equalsOp) {
        inp.match()
        initValue = initIntVar()
    }
    declareVar(varName, DataType.intptr, initValue, code.PTR_SIZE, scope)
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
