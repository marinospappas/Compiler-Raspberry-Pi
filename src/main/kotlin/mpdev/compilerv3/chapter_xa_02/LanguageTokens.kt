package mpdev.compilerv3.chapter_xa_02

///////////////////////////////////////////////////////////
// define all the keywords used in our programming language
///////////////////////////////////////////////////////////

// the null char is used as end of input mark
val nullChar = 0

val NO_TOKEN = "No Token"
val END_OF_INPUT = "End of Input"

// boolean literals
val BOOLEAN_TRUE = "true"
val BOOLEAN_FALSE = "false"

// main function name
val MAIN_BLOCK = "main"

class Token(val value: String = NO_TOKEN,
            val encToken: Kwd = Kwd.noToken,
            val type: TokType = TokType.none
)

var languageTokens = mutableListOf<Token>()

// our language list of keywords and operands and any other special character sequence
enum class Kwd {
    addOp, subOp, mulOp, divOp,
    equalsOp,
    leftParen, rightParen,

    orOp, xorOp, andOp, notOp,
    isEqual, isNotEqual,
    isGreater, isLess,
    isGreaterOrEq, isLessOrEq,

    intType, stringType, voidType,

    startBlock, endBlock,
    commaToken, semiColonToken, colonToken,

    ifToken, elseToken,
    whileToken,

    repeatToken, untilToken,
    forToken, toToken, stepToken, downToken,
    breakToken, continueToken,

    identifier, number, booleanLit, string,

    ptrOpen, ptrClose, addressOfVar,

    readToken, printToken, printLnToken,

    quoteToken,

    blockComment, inlineComment,
    blockCommentOut, inlineCommentOut,
    commentEnd,

    startOfProgram, endOfProgram,
    varDecl, funDecl, retToken, mainToken, exitToken,
    endOfInput, any, invalid, noToken
}

enum class TokType {
    addOps, mulOps,
    orOps, andOps, relOps,
    endOfBlock, endOfPRogram, endOfInput,
    function, variable,
    commentStart,
    varType,
    invalid, none
}

// initialise our lists (= define the language)

fun initKeywords() {
    languageTokens.add(
        Token("program",  Kwd.startOfProgram, TokType.none)
    )
    languageTokens.add(
        Token("endprogram", Kwd.endOfProgram, TokType.endOfPRogram)
    )

    languageTokens.add(
        Token("var",      Kwd.varDecl,       TokType.none)
    )
    languageTokens.add(
        Token("fun",      Kwd.funDecl,       TokType.none)
    )
    languageTokens.add(
        Token("return",   Kwd.retToken,      TokType.none)
    )
    languageTokens.add(
        Token(MAIN_BLOCK,      Kwd.mainToken,      TokType.none)
    )
    languageTokens.add(
        Token("exit",    Kwd.exitToken,     TokType.none)
    )

    languageTokens.add(
        Token("int",     Kwd.intType,       TokType.varType)
    )
    languageTokens.add(
        Token("string",  Kwd.stringType,    TokType.varType)
    )
    languageTokens.add(
        Token("void",    Kwd.voidType,      TokType.none)
    )

    languageTokens.add(
        Token("{",        Kwd.startBlock,    TokType.none)
    )
    languageTokens.add(
        Token("}",        Kwd.endBlock,      TokType.endOfBlock)
    )
    languageTokens.add(
        Token(",",        Kwd.commaToken,    TokType.none)
    )
    languageTokens.add(
        Token(";",        Kwd.semiColonToken, TokType.none)
    )
    languageTokens.add(
        Token("\"",       Kwd.quoteToken,     TokType.none)
    )
    languageTokens.add(
        Token(":",        Kwd.colonToken,     TokType.none)
    )

    languageTokens.add(
        Token("if",       Kwd.ifToken,        TokType.none)
    )
    languageTokens.add(
        Token("else",     Kwd.elseToken,      TokType.endOfBlock)
    )
    languageTokens.add(
        Token("while",    Kwd.whileToken,     TokType.none)
    )
    languageTokens.add(
        Token("for",      Kwd.forToken,       TokType.none)
    )
    languageTokens.add(
        Token("down",     Kwd.downToken,      TokType.none)
    )
    languageTokens.add(
        Token("to",       Kwd.toToken,        TokType.none)
    )
    languageTokens.add(
        Token("step",     Kwd.stepToken,      TokType.none)
    )
    languageTokens.add(
        Token("repeat",   Kwd.repeatToken,    TokType.none)
    )
    languageTokens.add(
        Token("until",    Kwd.untilToken,     TokType.endOfBlock)
    )
    languageTokens.add(
        Token("break",    Kwd.breakToken,     TokType.none)
    )
    languageTokens.add(
        Token("continue", Kwd.continueToken,  TokType.none)
    )
    languageTokens.add(
        Token("read",     Kwd.readToken,      TokType.none)
    )
    languageTokens.add(
        Token("print",    Kwd.printToken,     TokType.none)
    )
    languageTokens.add(
        Token("println",  Kwd.printLnToken,   TokType.none)
    )

    languageTokens.add(
        Token("[",  Kwd.ptrOpen,   TokType.varType)
    )
    languageTokens.add(
        Token("]",  Kwd.ptrClose,   TokType.none)
    )
    languageTokens.add(
        Token("addr",  Kwd.addressOfVar,   TokType.none)
    )

    languageTokens.add(
        Token("/*--",     Kwd.blockCommentOut, TokType.commentStart)
    )
    languageTokens.add(
        Token("/*",       Kwd.blockComment,   TokType.commentStart)
    )
    languageTokens.add(
        Token("*/",      Kwd.commentEnd,      TokType.none)
    )
    languageTokens.add(
        Token("//--",     Kwd.inlineCommentOut, TokType.commentStart)
    )
    languageTokens.add(
        Token("//",      Kwd.inlineComment,   TokType.commentStart)
    )

    languageTokens.add(
        Token(BOOLEAN_TRUE,    Kwd.booleanLit,      TokType.none)
    )
    languageTokens.add(
        Token(BOOLEAN_FALSE,   Kwd.booleanLit,      TokType.none)
    )

    languageTokens.add(
        Token(END_OF_INPUT,    Kwd.endOfInput,      TokType.none)
    )

    languageTokens.add(
        Token("Identifier", Kwd.identifier,   TokType.none)
    )
    languageTokens.add(
        Token("Number",   Kwd.number,         TokType.none)
    )
    languageTokens.add(
        Token("String",   Kwd.string,         TokType.none)
    )
}

///////////////////////////
// define all the operators
///////////////////////////

fun initOperators() {
    // numeric operators
    languageTokens.add(
        Token("+",      Kwd.addOp,      TokType.addOps)
    )
    languageTokens.add(
        Token("-",      Kwd.subOp,      TokType.addOps)
    )
    languageTokens.add(
        Token("*",      Kwd.mulOp,      TokType.mulOps)
    )
    languageTokens.add(
        Token("/",      Kwd.divOp,      TokType.mulOps)
    )

    languageTokens.add(
        Token("(",      Kwd.leftParen,  TokType.none)
    )
    languageTokens.add(
        Token(")",      Kwd.rightParen, TokType.none)
    )
    // boolean operators
    languageTokens.add(
        Token("||",     Kwd.orOp,       TokType.orOps)
    )
    languageTokens.add(
        Token("^^",     Kwd.xorOp,      TokType.orOps)
    )
    languageTokens.add(
        Token("&&",     Kwd.andOp,      TokType.andOps)
    )
    languageTokens.add(
        Token("==",     Kwd.isEqual,    TokType.relOps)
    )
    languageTokens.add(
        Token("!=",     Kwd.isNotEqual, TokType.relOps)
    )
    languageTokens.add(
        Token("!",      Kwd.notOp,      TokType.none)
    )
    languageTokens.add(
        Token(">=",     Kwd.isGreaterOrEq, TokType.relOps)
    )
    languageTokens.add(
        Token(">",      Kwd.isGreater,  TokType.relOps)
    )
    languageTokens.add(
        Token("<=",     Kwd.isLessOrEq, TokType.relOps)
    )
    languageTokens.add(
        Token("<",      Kwd.isLess,     TokType.relOps)
    )

    languageTokens.add(
        Token("=",      Kwd.equalsOp,   TokType.none)
    )
}
