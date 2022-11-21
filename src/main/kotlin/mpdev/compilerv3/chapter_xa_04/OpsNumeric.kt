package mpdev.compilerv3.chapter_xa_04

/** process a number */
fun parseNumber(): DataType {
    code.setAccumulator(inp.match(Kwd.number).value)
    return DataType.int
}

/** process a numeric int variable */
fun parseNumVariable(type: DataType): DataType {
    val varName = inp.match(Kwd.identifier).value
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalVar(it) }
    else
        code.setAccumulatorToVar(varName)
    return type
}

/** process a numeric byte variable */
fun parseNumByteVariable(): DataType {
    val varName = inp.match(Kwd.identifier).value
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalByteVar(it) }
    else
        code.setAccumulatorToByteVar(varName)
    return DataType.byte
}

/** process an array variable */
fun parseArrayVariable(arrayName: String): DataType {
    if (identifiersMap[arrayName]?.isStackVar == true)
        identifiersMap[arrayName]?.stackOffset?.let { code.setAccumulatorToLocalArrayVar(it) }
    else
        code.setAccumulatorToArrayVar(arrayName)
    return DataType.int
}

/** process an array variable */
fun parseByteArrayVariable(arrayName: String): DataType {
    if (identifiersMap[arrayName]?.isStackVar == true)
        identifiersMap[arrayName]?.stackOffset?.let { code.setAccumulatorToLocalByteArrayVar(it) }
    else
        code.setAccumulatorToByteArrayVar(arrayName)
    return DataType.int
}

/** process assignment to numeric var (int) */
fun parseNumAssignment(varName: String) {
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.assignmentLocalVar(it) }
    else
        code.assignment(varName)
}

/** process assignment to array numeric var (int) */
fun parseArrayAssignment(arrayName: String) {
    if (identifiersMap[arrayName]?.isStackVar == true)
        identifiersMap[arrayName]?.stackOffset?.let { code.assignmentLocalArrayVar(it) }
    else
        code.arrayAssignment(arrayName)
}

/** process assignment to numeric var (int) */
fun parseByteNumAssignment(varName: String) {
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.assignmentLocalByteVar(it) }
    else
        code.assignmentByte(varName)
}

/** process assignment to array numeric var (int) */
fun parseByteArrayAssignment(arrayName: String) {
    if (identifiersMap[arrayName]?.isStackVar == true)
        identifiersMap[arrayName]?.stackOffset?.let { code.assignmentLocalByteArrayVar(it) }
    else
        code.arrayByteAssignment(arrayName)
}

/** process a numeric addition */
fun addNumber(resultType: DataType) {
    code.addToAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process a numeric subtraction */
fun subtractNumber(resultType: DataType) {
    code.subFromAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process a numeric multiplication */
fun multiplyNumber(resultType: DataType) {
    code.multiplyAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process a numeric division */
fun divideNumber(resultType: DataType) {
    code.divideAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process a numeric modulo op */
fun moduloNumber(resultType: DataType) {
    code.moduloAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process shift left */
fun shiftLeftNumber(resultType: DataType) {
    code.shiftAccumulatorLeft()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process shift right */
fun shiftRightNumber(resultType: DataType) {
    code.shiftAccumulatorRight()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process bitwise or */
fun orNumber(resultType: DataType) {
    code.orAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process bitwise xor */
fun xorNumber(resultType: DataType) {
    code.xorAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}

/** process bitwise and */
fun andNumber(resultType: DataType) {
    code.andAccumulator()
    if (resultType == DataType.byte)
        code.convertToByte()
}
