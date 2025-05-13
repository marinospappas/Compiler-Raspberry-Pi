package mpdev.compilerv5.scanner

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Constants.Companion.NO_TOKEN
import mpdev.compilerv5.config.Constants.Companion.NULL_CHAR
import mpdev.compilerv5.util.Utils.Companion.abort
import java.io.File
import kotlin.math.min

/**
 * The input program reader class
 * Processes the char-by-char input and returns the various values from the input stream
 */
class InputReader(val context: CompilerContext = CompilerContext()) {

    // the input program as string
    private var inputProgram: String = ""
    private var cursor = 0

    // end of input mark
    val endOfInput = NULL_CHAR.toChar()

    // the next character from input
    // this is our lookahead character
    var nextChar: Char = ' '

    // input program line number (the line where the nextToken is)
    var lineNumber = 1

    fun initialise() {
        try {
            // read the whole program into a string
            // add a newline at the end to deal with end of input easier
            inputProgram = File(context.inFile).readText() + '\n'
            // set the lookahead character to the first input char and skip any white spaces
            nextChar = inputProgram[0]
        } catch (e: Exception) {
            abort("could not open input file - $e")
        }
    }

    /** get a special sequence from input (keyword or operator  */
    fun getSpecSeq(indx: Int): Token {
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
    fun getName(): String {
        var name = ""
        while (isAlphanumeric(nextChar)) {
            name += nextChar
            getNextChar()
        }
        return name
    }

    /**
     * get a decimal number
     * <number> ::= [ <digit> ] +
     */
    fun getNumber(): String {
        if (isBinaryNumber())
            return getBinaryNumber()
        if (isHexNumber())
            return getHexNumber()
        return getDecimalNumber()
    }

    /** get a decimal number */
    fun getDecimalNumber(): String {
        var value = ""
        while (isNumeric(nextChar)) {
            value += nextChar.toString()
            getNextChar()
        }
        return value
    }

    /** get a binary number */
    fun getBinaryNumber(): String {
        var value = "0b"
        while (isBinaryDigit(nextChar)) {
            value += nextChar.toString()
            getNextChar()
        }
        return value
    }

    /** get a hex number */
    fun getHexNumber(): String {
        var value = "0x"
        while (isHexDigit(nextChar)) {
            value += nextChar.toString()
            getNextChar()
        }
        return value
    }

    /** get a string literal */
    fun getString(): String {
        var value = ""
        if (!isQuote(nextChar))
            return value
        getNextChar()
        while (!isQuote(nextChar)) {
            value += nextChar.toString()
            getNextChar()
        }
        getNextChar()
        return value
    }

    /** set the lookahead character to the next char from input */
    fun getNextChar() {
        if (nextChar == '\n')
            ++lineNumber
        nextChar = if (cursor < inputProgram.length-1)
                        inputProgram[++cursor]
                    else
                        endOfInput
    }

    /** "pushes" the current character back to the queue and sets the next char to the previous one from input */
    fun pushChar() {
        if (cursor > 0)
            nextChar = inputProgram[--cursor]
    }

    /**
     * skip white spaces
     * returns true when a newline has been skipped
     */
    fun skipWhite() {
        while (isWhite(nextChar)) {
            getNextChar()
        }
    }

    /** check for an alpha char */
    fun isAlpha(c: Char): Boolean = c.uppercaseChar() in 'A'..'Z'

    /** check for a decimal numeric digit */
    fun isNumeric(c: Char): Boolean = c in '0'..'9'

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
    fun isQuote(c: Char): Boolean = c == '"'

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
