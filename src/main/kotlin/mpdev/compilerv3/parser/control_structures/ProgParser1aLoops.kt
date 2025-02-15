package mpdev.compilerv3.parser.control_structures

import mpdev.compilerv3.scanner.Kwd
import mpdev.compilerv3.abort
import mpdev.compilerv3.code
import mpdev.compilerv3.inp
import mpdev.compilerv3.parser.expressions.parseBooleanExpression

/**
 * parse while statement
 * <while> ::= while ( <b-expression> ) <block>
 */
fun parseWhile() {
    inp.match()
    val label1 = newLabel()
    val label2 = newLabel()
    postLabel(label1)
    inp.match(Kwd.leftParen)
    parseBooleanExpression()
    inp.match(Kwd.rightParen)
    code.jumpIfFalse(label2)
    parseBlock(label2, label1)
    code.jump(label1)
    postLabel(label2)
}

/**
 * parse repeat statement
 * <repeat> ::= repeat <block> until ( <b-expression> )
 */
fun parseRepeat() {
    inp.match()
    val label1 = newLabel()
    val label2 = newLabel()
    postLabel(label1)
    parseBlock(label2, label1)
    inp.match(Kwd.untilToken)
    inp.match(Kwd.leftParen)
    parseBooleanExpression()
    inp.match(Kwd.rightParen)
    code.jumpIfFalse(label1)
    postLabel(label2)
}

/**
 * parse break statement
 * <break> ::= break
 */
fun parseBreak(label: String) {
    inp.match()
    if (label == "")
        abort("line ${inp.currentLineNumber}: no loop to break of")
    code.jump(label)
}

/**
 * parse break statement
 * <continue> ::= continue
 */
fun parseContinue(label: String) {
    inp.match()
    if (label == "")
        abort("line ${inp.currentLineNumber}: no loop to continue")
    code.jump(label)
}
