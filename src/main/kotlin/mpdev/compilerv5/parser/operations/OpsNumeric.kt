package mpdev.compilerv5.parser.operations

import mpdev.compilerv5.config.Config
import mpdev.compilerv5.scanner.DataType

class OpsNumeric {

    private val code = Config.codeModule

    /** process a numeric addition */
    fun add(resultType: DataType) {
        code.addToAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process a numeric subtraction */
    fun subtract(resultType: DataType) {
        code.subFromAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process a numeric multiplication */
    fun multiply(resultType: DataType) {
        code.multiplyAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process a numeric division */
    fun divide(resultType: DataType) {
        code.divideAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process a numeric modulo op */
    fun modulo(resultType: DataType) {
        code.moduloAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process shift left */
    fun shiftLeft(resultType: DataType) {
        code.shiftAccumulatorLeft()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process shift right */
    fun shiftRight(resultType: DataType) {
        code.shiftAccumulatorRight()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process bitwise or */
    fun or(resultType: DataType) {
        code.orAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process bitwise xor */
    fun xor(resultType: DataType) {
        code.xorAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }

    /** process bitwise and */
    fun and(resultType: DataType) {
        code.andAccumulator()
        if (resultType == DataType.byte)
            code.convertToByte()
    }
}
