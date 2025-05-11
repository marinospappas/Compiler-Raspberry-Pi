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

    // any comments are kept here so that they can be transferred to the output
    private var commentString = ""
    private lateinit var startOfComment: String

    fun initialise() {
        try {
            // read the whole program into a string
            // add a newline at the end to deal with end of input easier
            inputProgram = context.tokenizedProgram
            // get the first token from input
            //nextToken = advanceToken()
            // process any initial comments
            startOfComment = Config.codeModule.COMMENT
        } catch (e: Exception) {
            abort("could not initialise program scanner - $e")
        }
    }

    /**
     * token matching is initialised here by setting nextToken to the first token of the input program
     */
    fun initialiseTokenMatching() {
        if (!inputProgram.isEmpty())
            nextToken = inputProgram.first()
    }

    /**
     * get the next token from the input stream and advance the cursor
     * match this token against a specific given token 'x'
     * also produces a match if called with no token or if token is "any"
     * finally it processes any comments in the code
     * returns the token object that has been matched
     * was pointing to the line of the nextToken at the end of the previous match call
     * it is called by all the parser functions
     */
    fun match(keyWord: Kwd = Kwd.any): Token {
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
    private fun advanceToken(): Token {
        return if (cursor >= inputProgram.lastIndex)
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
     * currentToken function
     * returns current token without advancing the cursor
     */
    fun currentToken(): Token {
        return inputProgram[cursor]
    }

    /**
     * lookahead function
     * returns next token without advancing the cursor
     */
    fun lookahead(): Token {
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
        abort("(${this.javaClass.simpleName}) line ${nextToken.lineNumber}: expected [$expMsg] found $tokType[${nextToken.value}]")
    }
}
