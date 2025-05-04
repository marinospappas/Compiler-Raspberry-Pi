package mpdev.compilerv5.scanner

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.util.Utils.Companion.abort

/**
 * The input program scanner class
 * Performs the lexical scanner functions
 * Processes the char-by-char input and returns the tokens from the input stream
 */
class ProgramScanner(val context: CompilerContext = CompilerContext()) {

    // the input program as string
    private lateinit var inputProgram: List<Token>
    private var cursor = 0

    // the next token is here so that we can look ahead
    private var nextToken: Token = Token()

    // input program line number (the line where the nextToken is)
    private var lineNumber = 1

    // the current token's line number
    var currentLineNumber = 0

    // any comments are kept here so that they can be transferred to the output
    private var commentString = ""
    private lateinit var startOfComment: String

    fun initialise() {
        try {
            // read the whole program into a string
            // add a newline at the end to deal with end of input easier
            inputProgram = context.tokenizedProgram
            // initialise current line
            currentLineNumber = lineNumber
            // get the first token from input
            nextToken = advanceToken()
            // process any initial comments
            startOfComment = Config.codeModule.COMMENT
            // todo: getComment()
        } catch (e: Exception) {
            abort("could not open input file - $e")
        }
    }

    /**
     * get the next token from the input stream and advance the cursor
     * match this token against a specific given token 'x'
     * also produces a match if called with no token or if token is "any"
     * finally it processes any comments in the code
     * returns the token object that has been matched
     * also sets the current line number at the beginning as the lineNumber
     * was pointing to the line of the nextToken at the end of the previous match call
     * it is called by all the parser functions
     */
    fun match(keyWord: Kwd = Kwd.any): Token {
        currentLineNumber = lineNumber
        printComment()  // any comments found in the previous call must be printed in the output code now
        if (keyWord != Kwd.any && nextToken.encToken != keyWord)    // check keyword to match
            expected(decodeToken(keyWord))
        val thisToken = nextToken
        nextToken = advanceToken()  // advance to next token
        // todo: getComment()    // process any comments
        return thisToken
    }

    /**
     * advance the cursor to the next token from the list
     */
    fun advanceToken(): Token {
        return if (cursor == inputProgram.lastIndex)
            Token("EOF", Kwd.endOfProgram, TokType.endOfPRogram)
        else
            inputProgram[++cursor]
    }

    /** print any comment identified in the previous call of match */
    private fun printComment() {
        if (commentString != "") {
            Config.codeModule.outputCode(commentString)
            commentString = ""
        }
    }

    /**
     * lookahead function
     * returns next token without advancing the cursor
     * sets current line number as well (same as match)
     */
    fun lookahead(): Token {
        currentLineNumber = lineNumber
        return nextToken
    }

    /** check for end of program - called by parseBlock */
    fun isEndOfProgram(): Boolean = nextToken.encToken == Kwd.endOfProgram

    /** decode an encoded token to token name */
    fun decodeToken(token: Kwd): String {
        for (i in languageTokens.indices)
            if (languageTokens[i].encToken == token)
                return languageTokens[i].value
        return "*******"
    }

    /** report what was expected and abort */
    fun expected(expMsg: String) {
        val tokType =
            if (nextToken.encToken == Kwd.number || nextToken.encToken == Kwd.identifier
                || nextToken.encToken == Kwd.string || nextToken.encToken == Kwd.booleanLit
            )
                "${nextToken.encToken} "
            else
                ""
        abort("(${this.javaClass.simpleName}) line $currentLineNumber: expected [$expMsg] found $tokType[${nextToken.value}]")
    }
}
