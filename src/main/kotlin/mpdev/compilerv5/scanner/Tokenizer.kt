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
class Tokenizer(val context: CompilerContext = CompilerContext()) {

    private lateinit var inputReader: InputReader
    // scanner for comments
    val commentScanner = CommentScanner()

    fun initialise() {
        try {
            inputReader = Config.inputReader
            // init the list of tokens for our language
            initKeywords()
            initOperators()
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
        inputReader.skipWhite()
        if (checkEndOfInput())
            return Token(END_OF_INPUT, Kwd.endOfInput, TokType.none, inputReader.lineNumber)
        if (checkComment())
            return getComment()
        if (checkNumeric())
            return Token(inputReader.getNumber(), Kwd.number, TokType.none, inputReader.lineNumber)
        if (checkAlpha())
            return keywordOrFunctionOrVariable(inputReader.getName())
        if (checkQuote())
            return Token(inputReader.getString(), Kwd.string, TokType.none, inputReader.lineNumber)
        if (checkSpecialToken())
            return getSpecialToken()
        return getInvalidToken()
    }

    /** check if we have reached the end of input */
    private fun checkEndOfInput(): Boolean = inputReader.nextChar == inputReader.endOfInput

    /** check if comment follows */
    //todo: implement the below
    private fun checkComment(): Boolean = commentPresent() != Token()

    /** check for a numeric token */
    private fun checkNumeric(): Boolean = inputReader.isNumeric(inputReader.nextChar)

    /** check for an alpha token */
    private fun checkAlpha():Boolean = inputReader.isAlpha(inputReader.nextChar)

    /** check for a quote */
    private fun checkQuote():Boolean = inputReader.isQuote(inputReader.nextChar)

    /** return a keyword or identifier token based on the keyword tokens list */
    private fun keywordOrFunctionOrVariable(name: String): Token {
        val indx = isKeyword(name)
        return if (indx >= 0)
            Token.of(languageTokens[indx], inputReader.lineNumber)  // keyword found
        else {
            // function, variable or other identifier found (determined by Token type)
            Token(name, Kwd.identifier, TokType.none, inputReader.lineNumber)
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
            abort("line: $inputReader.lineNumber: error retrieving special token")
        return Token()  // dummy return to keep the compiler happy - unreachable
    }

    /** set the next token as invalid - it has not been recognised */
    private fun getInvalidToken(): Token {
        val thisChar = inputReader.nextChar
        inputReader.getNextChar()
        return Token(thisChar.toString(), Kwd.invalid, TokType.invalid, inputReader.lineNumber)
    }

    /** check if a specific name is a keyword */
    private fun isKeyword(name: String): Int {
        if (inputReader.cursor >= inputReader.inputProgram.length)         // check for end of input
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
}
