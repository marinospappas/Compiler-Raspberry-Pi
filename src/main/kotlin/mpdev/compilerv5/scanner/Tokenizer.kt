package mpdev.compilerv5.scanner

import mpdev.compilerv5.config.CompilerContext
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
class Tokenizer(val context: CompilerContext = CompilerContext()) {

    // the input program as string
    private var inputProgram: String = ""
    private var cursor = 0

    // end of input mark
    private val endOfInput = NULL_CHAR.toChar()

    // the next character from input
    // this is our lookahead character
    private var nextChar: Char = ' '

    // input program line number (the line where the nextToken is)
    private var lineNumber = 1

    // scanner for comments
    val commentScanner = CommentScanner()

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
        } catch (e: Exception) {
            abort("could not open input file - $e")
        }
    }

    /**
     * convert the input characters to a list of compiler-recognised Tokens
     */
    fun tokenize() {
        val tokenList = context.tokenizedProgram
        while(true) {
            val t = scan().also { tokenList.add(it) }
            if (t.encToken == Kwd.endOfInput)
                break
        }
    }

    /**
     * scan input for the next token and advance the "cursor"
     */
    private fun scan(): Token {
        skipWhite()
        if (checkEndOfInput())
            return Token(END_OF_INPUT, Kwd.endOfInput, TokType.none, lineNumber)
        if (checkComment())
            return getComment()
        if (checkNumeric())
            return Token(getNumber(), Kwd.number, TokType.none, lineNumber)
        if (checkAlpha())
            return keywordOrFunctionOrVariable(getName())
        if (checkQuote())
            return Token(getString(), Kwd.string, TokType.none, lineNumber)
        if (checkSpecialToken())
            return getSpecialToken()
        return getInvalidToken()
    }

    /** check if we have reached the end of input */
    private fun checkEndOfInput(): Boolean = nextChar == endOfInput

    /** check if comment follows */
    //todo: implement the below
    private fun checkComment(): Boolean = commentPresent() != Token()

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
            Token.of(languageTokens[indx], lineNumber)  // keyword found
        else {
            // function, variable or other identifier found (determined by Token type)
            Token(name, Kwd.identifier, TokType.none, lineNumber)
        }
    }

    /** check for a special sequence (operator or other special token) */
    private fun checkSpecialToken(): Boolean = specSeqPresent() >= 0

    /** check for comment */
    private fun getComment(): Token {
        //todo: implement this - ensure the actual comment is stored in the Token.value
        val commentToken = commentPresent()
        if (commentToken == Token())
            abort("line: $lineNumber: error retrieving comment token")
        cursor = min(cursor + commentToken.value.length, inputProgram.length)
        val newCursor = commentScanner.getComment(commentToken, inputProgram, cursor)
        cursor = min(newCursor, inputProgram.length)
        return commentToken
    }

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
        return Token(thisChar.toString(), Kwd.invalid, TokType.invalid, lineNumber)
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
     * check the beginning of the remaining input for comment
     * returns the actual start of comment token if found or null if not
     */
    private fun commentPresent(): Token {
        if (cursor >= inputProgram.length)         // check for end of input
            return Token()
        for (t in commaTokens) {
            if (inputProgram.substring(cursor).startsWith(t.value))  // check for keyword match
                return t
        }
        return Token()
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
            return Token(NO_TOKEN, Kwd.noToken, TokType.none, lineNumber)
        val t = Token.of(languageTokens[indx], lineNumber)
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

    /** check for a binary number - starting with 0b */
    private fun isBinaryNumber(): Boolean {
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
    private fun isHexNumber(): Boolean {
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
}
