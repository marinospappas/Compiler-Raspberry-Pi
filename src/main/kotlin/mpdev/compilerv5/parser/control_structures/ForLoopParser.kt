package mpdev.compilerv5.parser.control_structures

import mpdev.compilerv5.code_module.AsmInstructions
import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.parser.expressions.ExpressionParser
import mpdev.compilerv5.parser.labels.LabelHandler
import mpdev.compilerv5.scanner.*
import mpdev.compilerv5.util.Utils.Companion.abort
import javax.naming.ldap.Control

/**
 * parse for in a separate class/file due to increased complexity
 * <for> ::= for ( <identifier> = <expression> [ down ] to <expression> [ step <expression> ] ) <block>
 */
class ForLoopParser(val context: CompilerContext) {

    private lateinit var scanner: InputProgramScanner
    private lateinit var code: AsmInstructions
    private lateinit var labelHandler: LabelHandler
    private lateinit var contrStructParser: ControlStructureParser
    private lateinit var exprParser: ExpressionParser

    private var controlVarName = ""
    private var ctrlVarOffs = 0
    private var downTo = false
    private var toOffs = 0
    private var hasStep = false
    private var stepOffs = 0

    fun initialise() {
        scanner = Config.scanner
        code = Config.codeModule
        labelHandler = Config.labelHandler
        contrStructParser = Config.controlStructureParser
        exprParser = Config.expressionParser
    }

    /** for parser function */
    fun parseFor() {
        scanner.match()
        parseForLine()
        presetCtrlVar()
        val label1 = labelHandler.newLabel()
        val label2 = labelHandler.newLabel()
        labelHandler.postLabel(label1)   // actual start of the loop
        stepAndCheck()      // increase (or decrease) ctrl var and check
        code.jumpIfFalse(label2)  // if limit reached, exit
        contrStructParser.parseBlock(label2, label1)    // the FOR block
        code.jump(label1) // loop back to the beginning of the loop
        labelHandler.postLabel(label2)   // exit point of the loop
        cleanUpStack()
    }

    /** parse the for line */
    private fun parseForLine() {
        scanner.match(Kwd.leftParen)
        parseCtrlVar()
        parseDown()
        parseTo()
        parseStep()
        scanner.match(Kwd.rightParen)
    }

    /** control var parser */
    private fun parseCtrlVar() {
        // get control var
        controlVarName = scanner.match(Kwd.identifier).value
        if (context.identifiersMap[controlVarName] != null)
            abort("(${this.javaClass.simpleName}) line ${scanner.currentLineNumber}: identifier $controlVarName already declared")
        scanner.match(Kwd.equalsOp)
        // allocate space in the stack for the ctrl var
        ctrlVarOffs = code.allocateStackVar(code.INT_SIZE)
        context.identifiersMap[controlVarName] = IdentifierDecl(
            TokType.variable, DataType.int, initialised = true, size = code.INT_SIZE,
            isStackVar = true, stackOffset = ctrlVarOffs, canAssign = false
        )
        // set the ctrl var to FROM
        val expType = exprParser.parseExpression()
        if (expType != DataType.int)
            abort("(${this.javaClass.simpleName}) line ${scanner.currentLineNumber}: expected integer expression found $expType")
        code.assignmentLocalVar(ctrlVarOffs)
    }

    /** check for down token */
    private fun parseDown() {
        if (scanner.lookahead().encToken == Kwd.downToken) {
            scanner.match()
            downTo = true
        }
    }

    /** parse to token and value */
    private fun parseTo() {
        // get TO value and store in the stack
        scanner.match(Kwd.toToken)
        toOffs = code.allocateStackVar(code.INT_SIZE)
        val expType = exprParser.parseExpression()
        if (expType != DataType.int)
            abort("(${this.javaClass.simpleName}) line ${scanner.currentLineNumber}: expected integer expression found $expType")
        code.assignmentLocalVar(toOffs)
    }

    /** check for step token */
    private fun parseStep() {
        if (scanner.lookahead().encToken == Kwd.stepToken) {
            scanner.match()
            hasStep = true
            // allocate space in the stack and save step value
            stepOffs = code.allocateStackVar(code.INT_SIZE)
            val expType = exprParser.parseExpression()
            if (expType != DataType.int)
                abort("(${this.javaClass.simpleName}) line ${scanner.currentLineNumber}: expected integer expression found $expType")
            code.assignmentLocalVar(stepOffs)
        }
    }

    /** preset the control var for the first iteration */
    private fun presetCtrlVar() {
        // pre-decrease/increase the control variable
        code.setAccumulatorToLocalVar(ctrlVarOffs)
        if (hasStep) {
            code.saveAccumulator()
            code.setAccumulatorToLocalVar(stepOffs)
            code.subFromAccumulator()
        }
        else {
            if (downTo)
                code.incAccumulator()
            else
                code.decAccumulator()
        }
        code.assignmentLocalVar(ctrlVarOffs)
    }

    /** next step for the control var and compare with "to" */
    private fun stepAndCheck() {
        // increase/decrease control var and check the condition
        code.setAccumulatorToLocalVar(ctrlVarOffs)
        if (hasStep) {
            code.saveAccumulator()
            code.setAccumulatorToLocalVar(stepOffs)
            code.addToAccumulator() // even if "down" we still add as the step would be negative
        }
        else {
            if (downTo)
                code.decAccumulator()
            else
                code.incAccumulator()
        }
        code.assignmentLocalVar(ctrlVarOffs)
        code.saveAccumulator()  // control variable
        code.setAccumulatorToLocalVar(toOffs)   // to value
        if (downTo)
            code.compareGreaterEqual()
        else
            code.compareLessEqual()
    }

    /** release stack variables */
    private fun cleanUpStack() {
        if (hasStep)
            code.releaseStackVar(code.INT_SIZE)
        context.identifiersMap.remove(controlVarName)
        code.releaseStackVar(2* code.INT_SIZE)
    }
}