package mpdev.compilerv3._legacy.chapter_xa_02

/**
 * Program parsing - module 0
 * Top Level - program structure
 */

/** program / library flag */
var isLibrary = false

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
    generatePostMainCode()
    parseProgEnd()
}

/**
 * parse program header
 * <program header> ::= program <identifier>
 */
fun parseProgHeader() {
    when (inp.lookahead().encToken) {
        Kwd.startOfProgram -> isLibrary = false
        Kwd.startOfLibrary -> isLibrary = true
        else -> inp.expected("program or library")
    }
    code.progInit(inp.match().value, inp.match(Kwd.identifier).value)
}

/**
 * parse main block
 * <main block> ::= main <block>
 */
fun parseMainBlock() {
    if (isLibrary) return   // no main block for libraries
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
    val endModule: Token
    if (isLibrary)
        endModule = inp.match(Kwd.endOfLibrary)
    else
        endModule = inp.match(Kwd.endOfProgram)
    code.progEnd(endModule.value)
    inp.match(Kwd.endOfInput)
}

/** add any string constants at the end of the assembler output */
fun generatePostMainCode() {
    code.createRelativeAddresses()
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
