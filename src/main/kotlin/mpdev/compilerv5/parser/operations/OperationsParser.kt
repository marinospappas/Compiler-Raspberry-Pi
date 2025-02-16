package mpdev.compilerv5.parser.operations

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.parser.expressions.ExpressionParser
import mpdev.compilerv5.parser.input_output.InputOutputParser
import mpdev.compilerv5.scanner.*

class OperationsParser(val context: CompilerContext) {

    private lateinit var inp: InputProgramScanner
    private lateinit var exprParser: ExpressionParser
    private lateinit var opsNumeric: OpsNumeric
    private lateinit var opsStrings: OpsStrings

    fun initialise() {
        inp = Config.scanner
        exprParser = Config.expressionParser
        opsNumeric = OpsNumeric()
        opsStrings = OpsStrings()
    }

    /** parse an addition */
    fun add(typeT1: DataType) {
        inp.match()
        val typeT2 = exprParser.parseTerm()
        checkOperandTypeCompatibility(typeT1, typeT2, ADD)
        when (typeT1) {
            DataType.int, DataType.memptr, DataType.byte -> opsNumeric.add(typeT1)
            DataType.string -> opsStrings.add()
            else -> {}
        }
    }

    /** parse a subtraction */
    fun subtract(typeT1: DataType) {
        inp.match()
        val typeT2 = exprParser.parseTerm()
        checkOperandTypeCompatibility(typeT1, typeT2, SUBTRACT)
        when (typeT1) {
            DataType.int, DataType.memptr, DataType.byte -> opsNumeric.subtract(typeT1)
            else -> {}
        }
    }

    /** parse a multiplication */
    fun multiply(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, MULTIPLY)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.multiply(typeF1)
            else -> {}
        }
    }

    /** parse a division */
    fun divide(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, DIVIDE)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.divide(typeF1)
            else -> {}
        }
    }

    /** parse a modulo op */
    fun modulo(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, MODULO)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.modulo(typeF1)
            else -> {}
        }
    }

    /** parse a shift left op */
    fun shiftLeft(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_LEFT)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.shiftLeft(typeF1)
            else -> {}
        }
    }

    /** parse a shift right op */
    fun shiftRight(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_RIGHT)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.shiftRight(typeF1)
            else -> {}
        }
    }

    /** parse a bitwise or */
    fun bitwiseOr(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, OR)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.or(typeF1)
            else -> {}
        }
    }

    /** parse a bitwise xor */
    fun bitwiseXor(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, XOR)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.xor(typeF1)
            else -> {}
        }
    }

    /** parse a bitwise and */
    fun bitwiseAnd(typeF1: DataType) {
        inp.match()
        val typeF2 = exprParser.parseFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, AND)
        when (typeF1) {
            DataType.int, DataType.byte -> opsNumeric.and(typeF1)
            else -> {}
        }
    }
}
