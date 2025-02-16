package mpdev.compilerv5.parser.control_structures

import mpdev.compilerv5.code_module.AsmInstructions
import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.parser.expressions.BooleanExpressionParser
import mpdev.compilerv5.parser.labels.LabelHandler
import mpdev.compilerv5.scanner.InputProgramScanner
import mpdev.compilerv5.scanner.Kwd
import mpdev.compilerv5.util.Utils.Companion.abort

class LoopParser(val context: CompilerContext) {

    private lateinit var scanner: InputProgramScanner
    private lateinit var code: AsmInstructions
    private lateinit var labelHandler: LabelHandler
    private lateinit var contrStructParser: ControlStructureParser
    private lateinit var booleanExprParser: BooleanExpressionParser

    fun initialise() {
        scanner = Config.scanner
        code = Config.codeModule
        labelHandler = Config.labelHandler
        contrStructParser = Config.controlStructureParser
        booleanExprParser = Config.booleanExpressionParser
    }

    /**
     * parse while statement
     * <while> ::= while ( <b-expression> ) <block>
     */
    fun parseWhile() {
        scanner.match()
        val label1 = labelHandler.newLabel()
        val label2 = labelHandler.newLabel()
        labelHandler.postLabel(label1)
        scanner.match(Kwd.leftParen)
        booleanExprParser.parse()
        scanner.match(Kwd.rightParen)
        code.jumpIfFalse(label2)
        contrStructParser.parseBlock(label2, label1)
        code.jump(label1)
        labelHandler.postLabel(label2)
    }

    /**
     * parse repeat statement
     * <repeat> ::= repeat <block> until ( <b-expression> )
     */
    fun parseRepeat() {
        scanner.match()
        val label1 = labelHandler.newLabel()
        val label2 = labelHandler.newLabel()
        labelHandler.postLabel(label1)
        contrStructParser.parseBlock(label2, label1)
        scanner.match(Kwd.untilToken)
        scanner.match(Kwd.leftParen)
        booleanExprParser.parse()
        scanner.match(Kwd.rightParen)
        code.jumpIfFalse(label1)
        labelHandler.postLabel(label2)
    }

    /**
     * parse break statement
     * <break> ::= break
     */
    fun parseBreak(label: String) {
        scanner.match()
        if (label == "")
            abort("(${this.javaClass.simpleName}) line ${scanner.currentLineNumber}: no loop to break of")
        code.jump(label)
    }

    /**
     * parse break statement
     * <continue> ::= continue
     */
    fun parseContinue(label: String) {
        scanner.match()
        if (label == "")
            abort("(${this.javaClass.simpleName}) line ${scanner.currentLineNumber}: no loop to continue")
        code.jump(label)
    }
}
