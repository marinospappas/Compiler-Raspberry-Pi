package mpdev.compilerv3._legacy.chapter_xa_04

/**
 * parse for in a separate class/file due to increased complexity
 * <for> ::= for ( <identifier> = <expression> [ down ] to <expression> [ step <expression> ] ) <block>
 */
class ForParser {

    private var controlVarName = ""
    private var ctrlVarOffs = 0
    private var downTo = false
    private var toOffs = 0
    private var hasStep = false
    private var stepOffs = 0

    /** for parser function */
    fun parseFor() {
        inp.match()
        parseForLine()
        presetCtrlVar()
        val label1 = newLabel()
        val label2 = newLabel()
        postLabel(label1)   // actual start of the loop
        stepAndCheck()      // increase (or decrease) ctrl var and check
        code.jumpIfFalse(label2)  // if limit reached, exit
        parseBlock(label2, label1)    // the FOR block
        code.jump(label1) // loop back to the beginning of the loop
        postLabel(label2)   // exit point of the loop
        cleanUpStack()
    }

    /** parse the for line */
    private fun parseForLine() {
        inp.match(Kwd.leftParen)
        parseCtrlVar()
        parseDown()
        parseTo()
        parseStep()
        inp.match(Kwd.rightParen)
    }

    /** control var parser */
    private fun parseCtrlVar() {
        // get control var
        controlVarName = inp.match(Kwd.identifier).value
        if (identifiersMap[controlVarName] != null)
            abort("line ${inp.currentLineNumber}: identifier $controlVarName already declared")
        inp.match(Kwd.equalsOp)
        // allocate space in the stack for the ctrl var
        ctrlVarOffs = code.allocateStackVar(code.INT_SIZE)
        identifiersMap[controlVarName] = IdentifierDecl(
            TokType.variable, DataType.int, initialised = true, size = code.INT_SIZE,
            isStackVar = true, stackOffset = ctrlVarOffs, canAssign = false
        )
        // set the ctrl var to FROM
        val expType = parseExpression()
        if (expType != DataType.int)
            abort("line ${inp.currentLineNumber}: expected integer expression found $expType")
        code.assignmentLocalVar(ctrlVarOffs)
    }

    /** check for down token */
    private fun parseDown() {
        if (inp.lookahead().encToken == Kwd.downToken) {
            inp.match()
            downTo = true
        }
    }

    /** parse to token and value */
    private fun parseTo() {
        // get TO value and store in the stack
        inp.match(Kwd.toToken)
        toOffs = code.allocateStackVar(code.INT_SIZE)
        val expType = parseExpression()
        if (expType != DataType.int)
            abort("line ${inp.currentLineNumber}: expected integer expression found $expType")
        code.assignmentLocalVar(toOffs)
    }

    /** check for step token */
    private fun parseStep() {
        if (inp.lookahead().encToken == Kwd.stepToken) {
            inp.match()
            hasStep = true
            // allocate space in the stack and save step value
            stepOffs = code.allocateStackVar(code.INT_SIZE)
            val expType = parseExpression()
            if (expType != DataType.int)
                abort("line ${inp.currentLineNumber}: expected integer expression found $expType")
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
        identifiersMap.remove(controlVarName)
        code.releaseStackVar(2* code.INT_SIZE)
    }
}