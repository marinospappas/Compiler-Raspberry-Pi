package mpdev.compilerv3.chapter_xa_04

/** parse an addition */
fun add(typeT1: DataType) {
    inp.match()
    val typeT2 = parseTerm()
    checkOperandTypeCompatibility(typeT1, typeT2, ADD)
    when (typeT1) {
        DataType.int, DataType.memptr, DataType.byte -> addNumber(typeT1)
        DataType.string -> addString()
        else -> {}
    }
}

/** parse a subtraction */
fun subtract(typeT1: DataType) {
    inp.match()
    val typeT2 = parseTerm()
    checkOperandTypeCompatibility(typeT1, typeT2, SUBTRACT)
    when (typeT1) {
        DataType.int, DataType.memptr, DataType.byte -> subtractNumber(typeT1)
        else -> {}
    }
}

/** parse a multiplication */
fun multiply(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, MULTIPLY)
    when (typeF1) {
        DataType.int, DataType.byte -> multiplyNumber(typeF1)
        else -> {}
    }
}

/** parse a division */
fun divide(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, DIVIDE)
    when (typeF1) {
        DataType.int, DataType.byte -> divideNumber(typeF1)
        else -> {}
    }
}

/** parse a modulo op */
fun modulo(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, MODULO)
    when (typeF1) {
        DataType.int, DataType.byte -> moduloNumber(typeF1)
        else -> {}
    }
}

/** parse a shift left op */
fun shiftLeft(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_LEFT)
    when (typeF1) {
        DataType.int, DataType.byte -> shiftLeftNumber(typeF1)
        else -> {}
    }
}

/** parse a shift right op */
fun shiftRight(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_RIGHT)
    when (typeF1) {
        DataType.int, DataType.byte -> shiftRightNumber(typeF1)
        else -> {}
    }
}

/** parse a bitwise or */
fun bitwiseOr(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, OR)
    when (typeF1) {
        DataType.int, DataType.byte -> orNumber(typeF1)
        else -> {}
    }
}

/** parse a bitwise xor */
fun bitwiseXor(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, XOR)
    when (typeF1) {
        DataType.int, DataType.byte -> xorNumber(typeF1)
        else -> {}
    }
}

/** parse a bitwise and */
fun bitwiseAnd(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, AND)
    when (typeF1) {
        DataType.int, DataType.byte -> andNumber(typeF1)
        else -> {}
    }
}
