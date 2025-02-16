package mpdev.compilerv5.parser.control_structures

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.config.Constants.Companion.MAIN_BLOCK
import mpdev.compilerv5.scanner.*
import mpdev.compilerv5.util.Utils.Companion.abort

/**
 * Program parsing - module 1
 * Control Structures
 */
class ControlStructureParser(val context: CompilerContext) {

    companion object {
        const val BLOCK_NAME = "block_"
    }

    private val scanner = Config.scanner
    private val code = Config.codeModule
    private val labelHandler = Config.labelHandler
    private val variableParser = Config.variablesDeclParser
    private val functionParser = Config.functionDeclParser
    private val loopParser = Config.loopParser
    private val forLoopParser = Config.forLoopParser
    private val functionCallParser = Config.functionCallParser
    private val expressionParser = Config.expressionParser
    private val booleanExprParser = Config.booleanExpressionParser
    private val inputOutputParser = Config.inputOutputParser

    //TODO: check where these vars should be - local or in then context?
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
            Kwd.whileToken -> loopParser.parseWhile()
            Kwd.repeatToken -> loopParser.parseRepeat()
            Kwd.forToken -> forLoopParser.parseFor()   // in separate module due to increased complexity
            Kwd.breakToken -> loopParser.parseBreak(breakLabel)
            Kwd.continueToken -> loopParser.parseContinue(continueLabel)
            Kwd.retToken -> parseReturn()
            Kwd.readToken -> inputOutputParser.parseRead()
            Kwd.printToken -> inputOutputParser.parsePrint()
            Kwd.printLnToken -> inputOutputParser.parsePrintLn()
            Kwd.identifier -> {
                if (scanner.lookahead().type == TokType.variable) expressionParser.parseAssignment()
                else if (scanner.lookahead().type == TokType.function) functionCallParser.parse()
                else abort("line ${scanner.currentLineNumber}: identifier ${scanner.lookahead().value} not declared")
            }
            Kwd.ptrOpen -> expressionParser.parsePtrAssignment()
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
        variableParser.parse(VarScope.local, blockName)
    }

    /**
     * parse if statement
     * <if> ::= if ( <b-expression> ) <block> [ else <block> ]
     */
    private fun parseIf(breakLabel: String, continueLabel: String) {
        scanner.match()
        scanner.match(Kwd.leftParen)
        booleanExprParser.parse()
        scanner.match(Kwd.rightParen)
        val label1 = labelHandler.newLabel()
        code.jumpIfFalse(label1)
        parseBlock(breakLabel, continueLabel)
        if (scanner.lookahead().encToken == Kwd.elseToken) {
            scanner.match()
            val label2 = labelHandler.newLabel()
            code.jump(label2)
            labelHandler.postLabel(label1)
            parseBlock(breakLabel, continueLabel)
            labelHandler.postLabel(label2)
        } else
            labelHandler.postLabel(label1)
    }

    /**
     * parse return statement
     * <return> ::= return <b-expression>
     */
    private fun parseReturn() {
        scanner.match()
        if (labelHandler.labelPrefix == MAIN_BLOCK)
            abort("line ${scanner.currentLineNumber}: return is not allowed in [main]")
        functionParser.hasReturn = true       // set the return flag for this function
        val funType = getType(functionParser.funName)
        if (funType != DataType.void) {
            val expType = expressionParser.parseExpression()
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