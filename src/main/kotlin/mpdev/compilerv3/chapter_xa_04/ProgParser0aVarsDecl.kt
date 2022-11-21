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
        Kwd.intType -> parseOneNumVarDecl(varName, varScope, DataType.int, code.INT_SIZE)
        Kwd.byteType -> parseOneNumVarDecl(varName, varScope, DataType.byte, code.BYTE_SIZE)
        Kwd.memPtrType -> parseOneNumVarDecl(varName, varScope, DataType.memptr, code.PTR_SIZE)
        Kwd.intArrayType -> parseOneArrayDecl(varName, varScope, DataType.intarray)
        Kwd.byteArrayType -> parseOneArrayDecl(varName, varScope, DataType.bytearray)
        Kwd.stringType -> parseOneStringDecl(varName, varScope)
        else -> inp.expected("variable type (int, byte, intarray, bytearray, pointer or string)")
    }
    if (scope == VarScope.local) {      // add any local vars to the local vars map for this block
        val localVarsList: MutableList<String> = localVarsMap[blockName] ?: mutableListOf()
        localVarsList.add(varName)
        localVarsMap[blockName] = localVarsList
    }
}

/** parse one numeric var declaration - used for int, byte, pointer */
fun parseOneNumVarDecl(varName: String, scope: VarScope, type: DataType, size: Int) {
    inp.match()
    val initValue = initIntVar()
    declareVar(varName, type, initValue, size, scope)
}

/** parse one pointer var declaration */
fun parseOneArrayDecl(varName: String, scope: VarScope, type: DataType) {
    inp.match()
    inp.match(Kwd.leftParen)
    val size = inp.match(Kwd.number).value
    inp.match(Kwd.rightParen)
    declareVar(varName, type, "", size.toInt(), scope)
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
    if (inp.lookahead().encToken == Kwd.equalsOp) {
        inp.match()
        var sign = ""
        if (inp.lookahead().type == TokType.addOps) {
            val plusMinus = inp.match().value
            if (plusMinus == "-")
                sign = "-"
        }
        return sign + inp.match(Kwd.number).value
    }
    else
        return ""
}

/** initialisation for string vars */
fun initStringVar(): String {
    return inp.match(Kwd.string).value
}
