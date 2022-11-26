package mpdev.compilerv3.chapter_xa_02

/**
 * Program parsing - module 1
 * Control Structures
 */

// global vars
var labelIndx: Int = 0
var labelPrefix = ""
const val BLOCK_NAME = "block_"
var blockId = 0
var mustRestoreSP = false

/** create a unique label*/
fun newLabel(): String = "${labelPrefix}_L${labelIndx++}_"

/** post a label to output */
fun postLabel(label: String) = code.outputLabel(label)

////////////////////////////////////////////////////////////

/**
 * parse a block
 * <block> ::= { <statement> * }
 */
fun parseBlock(breakLabel: String = "", continueLabel: String = "") {
    inp.match(Kwd.startBlock)
    mustRestoreSP = true
    val blockName = "$BLOCK_NAME${blockId++}"       // blockName is used as key to the local vars map for this block
    while (inp.lookahead().type != TokType.endOfBlock && !inp.isEndOfProgram()) {
        parseStatement(breakLabel, continueLabel, blockName)
    }
    releaseLocalVars(blockName, mustRestoreSP)
    inp.match(Kwd.endBlock)
}

/**
 * releaseLocalVars
 * releases any local variables allocated in this block
 */
fun releaseLocalVars(blockName: String, restoreSP: Boolean) {
    var localVarSize = 0
    localVarsMap[blockName]?.forEach {
        localVarSize +=
            when (identifiersMap[it]?.type) {
                DataType.int -> code.INT_SIZE
                DataType.string -> code.PTR_SIZE + identifiersMap[it]?.size!!
                else-> code.INT_SIZE
            }
        identifiersMap.remove(it)
    }
    if (localVarSize > 0 && restoreSP)
        code.releaseStackVar(localVarSize)
}

/**
 * parse a statement
 * <statement> ::= <block> | <if> | <while> | <repeat> | <for> | <break> |
 *                 <return> | <read> | <print> | <assignment> | <function_call> | null [ ; ]
 */
fun parseStatement(breakLabel: String, continueLabel: String, blockName: String) {
    when (inp.lookahead().encToken) {
        Kwd.varDecl -> parseLocalVars(blockName)
        Kwd.startBlock -> parseBlock(breakLabel, continueLabel)
        Kwd.ifToken -> parseIf(breakLabel, continueLabel)
        Kwd.whileToken -> parseWhile()
        Kwd.repeatToken -> parseRepeat()
        Kwd.forToken -> ForParser().parseFor()   // in separate module due to increased complexity
        Kwd.breakToken -> parseBreak(breakLabel)
        Kwd.continueToken -> parseContinue(continueLabel)
        Kwd.retToken -> parseReturn()
        Kwd.readToken -> parseRead()
        Kwd.printToken -> parsePrint()
        Kwd.printLnToken -> parsePrintLn()
        Kwd.identifier -> {
            if (inp.lookahead().type == TokType.variable) parseAssignment()
            else if (inp.lookahead().type == TokType.function) parseFunctionCall()
            else abort("line ${inp.currentLineNumber}: identifier ${inp.lookahead().value} not declared")
        }
        Kwd.ptrOpen -> parsePtrAssignment()
        Kwd.exitToken -> parseExit()
        Kwd.semiColonToken -> inp.match()   // semicolons are simply ignored
        else -> inp.expected("valid keyword, semicolon or identifier")
    }
}

/**
 * parseLocalVars
 * parses any local vars declared in this block
 * (can be anywhere in the block)
 */
fun parseLocalVars(blockName: String) {
    parseVarDecl(VarScope.local, blockName)
}

/**
 * parse if statement
 * <if> ::= if ( <b-expression> ) <block> [ else <block> ]
 */
fun parseIf(breakLabel: String, continueLabel: String) {
    inp.match()
    inp.match(Kwd.leftParen)
    parseBooleanExpression()
    inp.match(Kwd.rightParen)
    val label1 = newLabel()
    code.jumpIfFalse(label1)
    parseBlock(breakLabel, continueLabel)
    if (inp.lookahead().encToken == Kwd.elseToken) {
        inp.match()
        val label2 = newLabel()
        code.jump(label2)
        postLabel(label1)
        parseBlock(breakLabel, continueLabel)
        postLabel(label2)
    }
    else
        postLabel(label1)
}

/**
 * parse return statement
 * <return> ::= return <b-expression>
 */
fun parseReturn() {
    inp.match()
    if (labelPrefix == MAIN_BLOCK)
        abort("line ${inp.currentLineNumber}: return is not allowed in [main]")
    hasReturn = true       // set the return flag for this function
    val funType = getType(funName)
    if (funType != DataType.void) {
        val expType = parseExpression()
        if (expType != funType)
            abort("line ${inp.currentLineNumber}: $funType function cannot return $expType")
    }
    code.returnFromCall()
    mustRestoreSP = false
}

/**
 * parse exit
 * <exit> :: exit [ <exit_code> ]
 * current version implements exit(0) - exit code will be supported later
 */
fun parseExit() {
    inp.match()
    code.exitProgram()
}