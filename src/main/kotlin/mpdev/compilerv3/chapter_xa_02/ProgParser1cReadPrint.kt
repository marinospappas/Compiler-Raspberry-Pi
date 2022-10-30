package mpdev.compilerv3.chapter_xa_02

/**
 * parse read statement
 * <read> :: = read <identifier> [ , <identifier> ] *
 */
fun parseRead() {
    var varToken: Token
    do {
        inp.match()
        varToken = inp.match(Kwd.identifier)
        if (varToken.type == TokType.none)
            abort("line ${inp.currentLineNumber}: identifier ${varToken.value} not declared")
        if (varToken.type != TokType.variable)
            abort("line ${inp.currentLineNumber}: identifier ${varToken.value} is not a variable")
        val identName = varToken.value
        val strLen = identifiersMap[identName]?.size!!
        when (getType(identName)) {
            DataType.int -> parseReadInt(identName)
            DataType.string -> parseReadString(identName, strLen)
            else -> {}
        }
    } while (inp.lookahead().encToken == Kwd.commaToken)
}

/** parse a read int instruction */
fun parseReadInt(identName: String) {
    if (identifiersMap[identName]?.isStackVar!!) {
        code.readIntLocal(identifiersMap[identName]?.stackOffset!!)
        code.assignmentLocalVar(identifiersMap[identName]?.stackOffset!!)
    }
    else {
        code.readInt(identName)
        code.assignment(identName)
    }
}

/** parse a read string instruction */
fun parseReadString(identName: String, strLen: Int) {
    if (identifiersMap[identName]?.isStackVar!!)
        code.readStringLocal(identifiersMap[identName]?.stackOffset!!, strLen)
    else
        code.readString(identName, strLen)
}

/**
 * parse print statement
 * <print> ::= print <b-expression> [ , <b-expression> ] *
 */
fun parsePrint() {
    inp.match()
    printExpressions()
}
fun parsePrintLn() {
    inp.match()
    if (inp.lookahead().encToken != Kwd.semiColonToken)
        printExpressions()
    code.printNewline()
}

fun printExpressions() {
    do {
        var decFmt = code.DEF_INT_FMT
        if (inp.lookahead().encToken == Kwd.commaToken)
            inp.match() // skip the comma
        val exprType = parseBooleanExpression()
        checkOperandTypeCompatibility(exprType, DataType.none, PRINT)
        if (inp.lookahead().encToken == Kwd.colonToken) {
            inp.match()
            decFmt = getPrintFormat()
        }
        when (exprType) {
            DataType.int, DataType.intptr -> code.printInt(decFmt)
            DataType.string -> code.printStr()
            else -> {}
        }
    } while (inp.lookahead().encToken == Kwd.commaToken)
}

fun getPrintFormat(): String {
    var fmt = ""
    var fmtLen = ""
    var fmtType = ""
    if (inp.lookahead().encToken == Kwd.number) {
        fmtLen = inp.lookahead().value
        inp.match()
    }
    if (inp.lookahead().encToken == Kwd.identifier) {
        fmtType = inp.lookahead().value
        inp.match()
    }
    fmt = "%$fmtLen$fmtType"
    // fmt must be added to the map of constant strings
    var fmtStringName = ""
    stringConstants.forEach { (k, v) -> if (v == fmt) fmtStringName = k }
    if (fmtStringName == "") {  // if not found
        // save the string in the map of constant strings
        fmtStringName = "${code.INT_FMT}_${fmt.substring(1)}"
        stringConstants[fmtStringName] = fmt
    }
    return fmtStringName
}