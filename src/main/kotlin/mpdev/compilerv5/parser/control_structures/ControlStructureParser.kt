package mpdev.compilerv5.parser.control_structures

import mpdev.compilerv3.CompilerContext
import mpdev.compilerv3.parser.expressions.parseAssignment
import mpdev.compilerv3.parser.expressions.parseBooleanExpression
import mpdev.compilerv3.parser.expressions.parseExpression
import mpdev.compilerv3.parser.expressions.parsePtrAssignment
import mpdev.compilerv3.parser.function_calls.parseFunctionCall
import mpdev.compilerv3.parser.input_output.parsePrint
import mpdev.compilerv3.parser.input_output.parsePrintLn
import mpdev.compilerv3.parser.input_output.parseRead
import mpdev.compilerv3.scanner.*
import mpdev.compilerv3.util.Utils.Companion.abort

/**
 * Program parsing - module 1
 * Control Structures
 */
class ControlStructureParser(context: CompilerContext) {

    companion object {
        const val BLOCK_NAME = "block_"
    }

    val scanner = context.scanner
    val code = context.codeModule

    // global vars
    var labelIndx: Int = 0
    var labelPrefix = ""
    var blockId = 0
    var mustRestoreSP = false

    /**
     * parse a block
     * <block> ::= { <statement> * }
     */
    fun parseBlock(breakLabel: String = "", continueLabel: String = "") {
        scanner.match(Kwd.startBlock)
        mustRestoreSP = true
        val blockName = "$BLOCK_NAME${blockId++}"       // blockName is used as key to the local vars map for this block
        while (scanner.lookahead().type != TokType.endOfBlock && !scanner.isEndOfProgram()) {
            parseStatement(breakLabel, continueLabel, blockName)
        }
        releaseLocalVars(blockName, mustRestoreSP)
        scanner.match(Kwd.endBlock)
    }

    /** create a unique label*/
    fun newLabel(): String = "${labelPrefix}_L${labelIndx++}_"

    /** post a label to output */
    fun postLabel(label: String) = code.outputLabel(label)

    /**
     * releaseLocalVars
     * releases any local variables allocated in this block
     */
    private fun releaseLocalVars(blockName: String, restoreSP: Boolean) {
        var localVarSize = 0
        localVarsMap[blockName]?.forEach {
            localVarSize +=
                when (identifiersMap[it]?.type) {
                    DataType.int -> code.INT_SIZE
                    DataType.string -> code.PTR_SIZE + identifiersMap[it]?.size!!
                    else -> code.INT_SIZE
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
    private fun parseStatement(breakLabel: String, continueLabel: String, blockName: String) {
        when (scanner.lookahead().encToken) {
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
                if (scanner.lookahead().type == TokType.variable) parseAssignment()
                else if (scanner.lookahead().type == TokType.function) parseFunctionCall()
                else abort("line ${scanner.currentLineNumber}: identifier ${scanner.lookahead().value} not declared")
            }

            Kwd.ptrOpen -> parsePtrAssignment()
            Kwd.exitToken -> parseExit()
            Kwd.semiColonToken -> scanner.match()   // semicolons are simply ignored
            else -> scanner.expected("valid keyword, semicolon or identifier")
        }
    }

    /**
     * parseLocalVars
     * parses any local vars declared in this block
     * (can be anywhere in the block)
     */
    private fun parseLocalVars(blockName: String) {
        parseVarDecl(VarScope.local, blockName)
    }

    /**
     * parse if statement
     * <if> ::= if ( <b-expression> ) <block> [ else <block> ]
     */
    private fun parseIf(breakLabel: String, continueLabel: String) {
        scanner.match()
        scanner.match(Kwd.leftParen)
        parseBooleanExpression()
        scanner.match(Kwd.rightParen)
        val label1 = newLabel()
        code.jumpIfFalse(label1)
        parseBlock(breakLabel, continueLabel)
        if (scanner.lookahead().encToken == Kwd.elseToken) {
            scanner.match()
            val label2 = newLabel()
            code.jump(label2)
            postLabel(label1)
            parseBlock(breakLabel, continueLabel)
            postLabel(label2)
        } else
            postLabel(label1)
    }

    /**
     * parse return statement
     * <return> ::= return <b-expression>
     */
    private fun parseReturn() {
        scanner.match()
        if (labelPrefix == MAIN_BLOCK)
            abort("line ${scanner.currentLineNumber}: return is not allowed in [main]")
        hasReturn = true       // set the return flag for this function
        val funType = getType(funName)
        if (funType != DataType.void) {
            val expType = parseExpression()
            if (expType != funType)
                abort("line ${scanner.currentLineNumber}: $funType function cannot return $expType")
        }
        code.returnFromCall()
        mustRestoreSP = false
    }

    /**
     * parse exit
     * <exit> :: exit [ <exit_code> ]
     * current version implements exit(0) - exit code will be supported later
     */
    private fun parseExit() {
        scanner.match()
        code.exitProgram()
    }
}