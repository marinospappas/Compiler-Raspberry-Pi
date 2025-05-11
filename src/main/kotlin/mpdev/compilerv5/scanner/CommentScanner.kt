package mpdev.compilerv5.scanner

class CommentScanner {
    //todo: move processing of comments to the right place - scanner or parser
    /*
    /** get a comment */
    private fun getComment() {
    while (nextToken.type == TokType.commentStart)
    when (nextToken.encToken) {
    Kwd.blockComment -> getCommentBlock()
    Kwd.blockCommentOut -> getCommentBlock(true)
    Kwd.inlineComment -> getCommentInline()
    Kwd.inlineCommentOut -> getCommentInline(true)
    else -> getInvalidToken()
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
    if (nextToken.encToken != Kwd.endOfInput)
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
    */
}