package mpdev.compilerv3.chapter_xa_02

/** parse an addition */
fun add(typeT1: DataType) {
    inp.match()
    val typeT2 = parseTerm()
    checkOperandTypeCompatibility(typeT1, typeT2, ADD)
    when (typeT1) {
        DataType.int, DataType.ptrExpr, DataType.intptr -> addNumber()
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
        DataType.int, DataType.ptrExpr, DataType.intptr -> subtractNumber()
        else -> {}
    }
}

/** parse a multiplication */
fun multiply(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, MULTIPLY)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> multiplyNumber()
        else -> {}
    }
}

/** parse a division */
fun divide(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, DIVIDE)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> divideNumber()
        else -> {}
    }
}

/** parse a modulo op */
fun modulo(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, MODULO)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> moduloNumber()
        else -> {}
    }
}

/** parse a shift left op */
fun shiftLeft(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_LEFT)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> shiftLeftNumber()
        else -> {}
    }
}

/** parse a shift right op */
fun shiftRight(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_RIGHT)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> shiftRightNumber()
        else -> {}
    }
}

/** parse a bitwise or */
fun bitwiseOr(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, OR)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> orNumber()
        else -> {}
    }
}

/** parse a bitwise xor */
fun bitwiseXor(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, XOR)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> xorNumber()
        else -> {}
    }
}

/** parse a bitwise or */
fun bitwiseAnd(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, AND)
    when (typeF1) {
        DataType.int, DataType.ptrExpr -> andNumber()
        else -> {}
    }
}
