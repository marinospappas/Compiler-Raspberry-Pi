package mpdev.compilerv5.scanner

import mpdev.compilerv3._legacy.chapter_xa_01.NO_TOKEN
import mpdev.compilerv5.config.Config

class CommentScanner {

    val startOfComment = Config.codeModule.COMMENT
    lateinit var inputProgram: String
    var cursor = 0

    /**
     * get a comment and return in in the value of the commentToken
     * also return the length of the comment string (including newlines and comment end
     */
    fun getComment(commentToken: Token, inputProgram: String, cursor: Int): Int {
        this.cursor = cursor
        this.inputProgram = inputProgram
        return when (commentToken.encToken) {
            Kwd.blockComment -> getCommentBlock(commentToken)
            Kwd.blockCommentOut -> getCommentBlock(commentToken, true)
            Kwd.inlineComment -> getCommentInline(commentToken)
            Kwd.inlineCommentOut -> getCommentInline(commentToken, true)
            else -> { commentToken.value = NO_TOKEN; return cursor }
        }
    }

    /** get a block comment */
    private fun getCommentBlock(commentToken: Token, printToOut: Boolean = false): Int {
        var localCommentString = startOfComment
        val endComment: String = decodeToken(Kwd.commentEnd)
        while (!inputProgram.substring(cursor).startsWith(endComment) && nextChar != endOfInput) {
            localCommentString += nextChar
            if (nextChar == '\n')
                localCommentString += startOfComment
            getNextChar()
        }
        localCommentString += '\n'
        if (printToOut)
            //todo: set comment token type accordingly
    }

    /** get an in-line comment */
    private fun getCommentInline(commentToken: Token, printToOut: Boolean = false): Int {
        var localCommentString = startOfComment
        while (nextChar != '\n' && nextChar != endOfInput) {
            localCommentString += nextChar
            getNextChar()
        }
        localCommentString += '\n'
        commentToken.value = localCommentString
        if (printToOut)
            //todo: set comment token type accordingly
    }
}