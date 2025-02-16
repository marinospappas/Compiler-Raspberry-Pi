package mpdev.compilerv5.parser.control_structures

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.parser.expressions.parseBooleanExpression
import mpdev.compilerv5.scanner.Kwd
import mpdev.compilerv5.util.Utils.Companion.abort


/**
 * parse while statement
 * <while> ::= while ( <b-expression> ) <block>
 */
class LoopParser(context: CompilerContext) {

    private val scanner = Config.scanner
    private val code = Config.codeModule
    private val labelHandler = Config.labelHandler
    private val contrStructParser = Config.controlStructureParser

    fun parseWhile() {
        scanner.match()
        val label1 = labelHandler.newLabel()
        val label2 = labelHandler.newLabel()
        labelHandler.postLabel(label1)
        scanner.match(Kwd.leftParen)
        parseBooleanExpression()
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
        parseBooleanExpression()
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
            abort("line ${scanner.currentLineNumber}: no loop to break of")
        code.jump(label)
    }

    /**
     * parse break statement
     * <continue> ::= continue
     */
    fun parseContinue(label: String) {
        scanner.match()
        if (label == "")
            abort("line ${scanner.currentLineNumber}: no loop to continue")
        code.jump(label)
    }
}
