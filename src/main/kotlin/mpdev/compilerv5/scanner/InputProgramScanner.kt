package mpdev.compilerv5.scanner

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.config.Constants.Companion.END_OF_INPUT
import mpdev.compilerv5.config.Constants.Companion.NO_TOKEN
import mpdev.compilerv5.config.Constants.Companion.NULL_CHAR
import mpdev.compilerv5.util.Utils.Companion.abort
import java.io.File
import kotlin.math.min

/**
 * The input program scanner class
 * Performs the lexical scanner functions
 * Processes the char-by-char input and returns the tokens from the input stream
 */
class InputProgramScanner(val context: CompilerContext = CompilerContext()) {

    // the input program as string
    private var inputProgram: String = ""
    private var cursor = 0

    // end of input mark
    private val endOfInput = NULL_CHAR.toChar()

    // the next character from input
    // this is our lookahead character
    private var nextChar: Char = ' '

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
            inputProgram = File(context.inFile).readText() + '\n'
            // init the list of tokens for our language
            initKeywords()
            initOperators()
            // set the lookahead character to the first input char and skip any white spaces
            nextChar = inputProgram[0]
            // initialise current line
            currentLineNumber = lineNumber
            // get the first token from input
            nextToken = scan()
            // process any initial comments
            startOfComment = Config.codeModule.COMMENT
            getComment()
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
        nextToken = scan()  // advance to next token
        getComment()    // process any comments
        return thisToken
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

    /** get the next token and advance the "cursor" */
    private fun scan(): Token {
        skipWhite()
        if (checkEndofInput())
            return Token(END_OF_INPUT, Kwd.endOfInput, TokType.none)
        if (checkNumeric())
            return Token(getNumber(), Kwd.number, TokType.none)
        if (checkAlpha())
            return keywordOrFunctionOrVariable(getName())
        if (checkQuote())
            return Token(getString(), Kwd.string, TokType.none)
        if (checkSpecialToken())
            return getSpecialToken()
        return getInvalidToken()
    }

    /** check if we have reached the end of input */
    private fun checkEndofInput(): Boolean = nextChar == endOfInput

    /** check for a numeric token */
    private fun checkNumeric(): Boolean = isNumeric(nextChar)

    /** check for an alpha token */
    private fun checkAlpha():Boolean = isAlpha(nextChar)

    /** check for a quote */
    private fun checkQuote():Boolean = isQuote(nextChar)

    /** return a keyword or identifier token based on the keyword tokens list */
    private fun keywordOrFunctionOrVariable(name: String): Token {
        val indx = isKeyword(name)
        return if (indx >= 0)
                    languageTokens[indx]  // keyword found
                else {
                    // function, variable or other identifier found (determined by Token type)
                    Token(name, Kwd.identifier, context.identifiersMap[name]?.fv ?: TokType.none)
                }
    }

    /** check for a special sequence (operator or other special token) */
    private fun checkSpecialToken(): Boolean = specSeqPresent() >= 0

    /** get the special sequence */
    private fun getSpecialToken(): Token {
        val indx = specSeqPresent()
        if (indx >= 0)
            return(getSpecSeq(indx))
        else
            abort("line: $lineNumber: error retrieving special token")
        return Token()  // dummy return to keep the compiler happy - unreachable
    }

    /** set the next token as invalid - it has not been recognised */
    private fun getInvalidToken(): Token {
        val thisChar = nextChar
        getNextChar()
        return Token(thisChar.toString(), Kwd.invalid, TokType.invalid)
    }

    /** check if a specific name is a keyword */
    private fun isKeyword(name: String): Int {
        if (cursor >= inputProgram.length)         // check for end of input
            return -1
        for (i in languageTokens.indices) {
            if (languageTokens[i].value == name)  // check for keyword match
                return i
        }
        return -1
    }

    /**
     * check the beginning of the remaining input for special sequence (e.g. operator)
     * returns the index in our keywords list if found or -1 if not
     */
    private fun specSeqPresent(): Int {
        if (cursor >= inputProgram.length)         // check for end of input
            return -1
        for (i in languageTokens.indices) {
            val tokenValue = languageTokens[i].value
            if (inputProgram.substring(cursor).startsWith(tokenValue))  // check for keyword match
                return i
        }
        return -1
    }

    /** get a special sequence from input (keyword or operator  */
    private fun getSpecSeq(indx: Int): Token {
        if (indx >= languageTokens.size)
            return Token(NO_TOKEN, Kwd.noToken, TokType.none)
        val t = languageTokens[indx]
        cursor = min(cursor+t.value.length, inputProgram.length)
        nextChar = inputProgram[cursor]
        return t
    }

    /**
     * get an identifier
     * <identifier> ::= <alpha> [ <alphanumeric> | <_> ] *
     */
    private fun getName(): String {
        var token = ""
        while (isAlphanumeric(nextChar)) {
            token += nextChar
            getNextChar()
        }
        return token
    }

    /**
     * get a decimal number
     * <number> ::= [ <digit> ] +
     */
    private fun getNumber(): String {
        if (isBinaryNumber())
            return getBinaryNumber()
        if (isHexNumber())
            return getHexNumber()
        return getDecimalNumber()
    }

    /** get a decimal number */
    private fun getDecimalNumber(): String {
        var value = ""
        while (isNumeric(nextChar)) {
            value += nextChar.toString()
            getNextChar()
        }
        return value
    }

    /** get a binary number */
    private fun getBinaryNumber(): String {
        var value = "0b"
        while (isBinaryDigit(nextChar)) {
            value += nextChar.toString()
            getNextChar()
        }
        return value
    }

    /** get a hex number */
    private fun getHexNumber(): String {
        var value = "0x"
        while (isHexDigit(nextChar)) {
            value += nextChar.toString()
            getNextChar()
        }
        return value
    }

    /** get a string literal */
    private fun getString(): String {
        var value = ""
        if (nextChar != '"')
            return value
        getNextChar()
        while (nextChar != '"') {
            value += nextChar.toString()
            getNextChar()
        }
        getNextChar()
        return value
    }

    /** set the lookahead character to the next char from input */
    private fun getNextChar() {
        if (nextChar == '\n')
            ++lineNumber
        nextChar = if (cursor < inputProgram.length-1)
                        inputProgram[++cursor]
                    else
                        endOfInput
    }

    /** "pushes" the current character back to the queue and sets the next char to the previous one from input */
    private fun pushChar() {
        if (cursor > 0)
            nextChar = inputProgram[--cursor]
    }

    /**
     * skip white spaces
     * returns true when a newline has been skipped
     */
    private fun skipWhite() {
        while (isWhite(nextChar)) {
            getNextChar()
        }
    }

    /** get a comment */
    private fun getComment() {
        while (nextToken.type == TokType.commentStart)
            when (nextToken.encToken) {
                Kwd.blockComment -> getCommentBlock()
                Kwd.blockCommentOut -> getCommentBlock(true)
                Kwd.inlineComment -> getCommentInline()
                Kwd.inlineCommentOut -> getCommentInline(true)
                else -> expected("start of comment")
            }
    }

    /** get a block comment */
    private fun getCommentBlock(printToOut: Boolean = false) {
        var localCommentString = startOfComment
        val endComment: String = decodeToken(Kwd.commentEnd)
        while (!inputProgram.substring(cursor).startsWith(endComment) && nextChar != endOfInput) {
            localCommentString += nextChar
            if (nextChar == '\n')
                localCommentString += startOfComment
            getNextChar()
        }
        localCommentString += '\n'
        nextToken = scan()      // nextToken now points to endComment or endOfInput
        if (nextToken.encToken == Kwd.endOfInput)
            expected(endComment)
        nextToken = scan()      // nextToken now points to the next token after the comment
        if (printToOut)
            commentString += localCommentString
    }

    /** get an in-line comment */
    private fun getCommentInline(printToOut: Boolean = false) {
        var localCommentString = startOfComment
        while (nextChar != '\n' && nextChar != endOfInput) {
            localCommentString += nextChar
            getNextChar()
        }
        localCommentString += '\n'
        nextToken = scan()
        if (printToOut)
            commentString += localCommentString
    }

    /** check for an alpha char */
    private fun isAlpha(c: Char): Boolean = c.uppercaseChar() in 'A'..'Z'

    /** check for a decimal numeric digit */
    private fun isNumeric(c: Char): Boolean = c in '0'..'9'

    /** check for a binary numeric digit */
    private fun isBinaryDigit(c: Char): Boolean = c in '0'..'1'

    /** check for a hex numeric digit */
    private fun isHexDigit(c: Char): Boolean = c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'

    /** check for alphanumeric */
    private fun isAlphanumeric(c: Char): Boolean = isAlpha(c) || isNumeric(c) || c == '_'

    /** check for newline only */
    private fun isNewLine(c: Char): Boolean = c == '\n'

    /** check for end of line */
    private fun isEndOfLine(c: Char): Boolean = isNewLine(c) || c == '\r'

    /** check for a white space */
    private fun isWhite(c: Char): Boolean = c == ' ' || c == '\t' || isEndOfLine(c)

    /** check for quote */
    private fun isQuote(c: Char): Boolean = c == '"'

    /** check for end of program - called by parseBlock */
    fun isEndOfProgram(): Boolean = nextToken.encToken == Kwd.endOfProgram ||
        nextToken.encToken == Kwd.endOfInput

    /** check for a binary number - starting with 0b */
    fun isBinaryNumber(): Boolean {
        if (nextChar != '0')
            return false
        getNextChar()
        if (nextChar != 'b') {
            pushChar()
            return false
        }
        getNextChar()
        return true
    }

    /** check for a hex number - starting with 0x */
    fun isHexNumber(): Boolean {
        if (nextChar != '0')
            return false
        getNextChar()
        if (nextChar != 'x') {
            pushChar()
            return false
        }
        getNextChar()
        return true
    }

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

    /** debug functions */
    fun debugGetNextChar() = "nextChar: [" +
            (if(nextChar<' ' ) "\\"+nextChar.code.toByte() else nextChar.toString()) + "]"
    fun debugGetLineInfo() = "curline: $currentLineNumber, line: $lineNumber"
    fun debugGetCursor() = "cursor: $cursor"
}
