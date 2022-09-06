package mpdev.compilerv3.chapter_xa_02

/** process a number */
fun parseNumber(): DataType {
    code.setAccumulator(inp.match(Kwd.number).value)
    return DataType.int
}

/** process a numeric variable */
fun parseNumVariable(): DataType {
    val varName = inp.match(Kwd.identifier).value
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalVar(it) }
    else
        code.setAccumulatorToVar(varName)
    return DataType.int
}

/** process a numeric variable */
fun parsePtrVariable(): DataType {
    val varName = inp.match(Kwd.identifier).value
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalVar(it) }
    else
        code.setAccumulatorToVar(varName)
    return DataType.ptrExpr
}

/** process assignment to numeric var (int for now) */
fun parseNumAssignment(varName: String) {
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.assignmentLocalVar(it) }
    else
        code.assignment(varName)
}

/** process a numeric addition */
fun addNumber() {
    code.addToAccumulator()
}

/** process a numeric subtraction */
fun subtractNumber() {
    code.subFromAccumulator()
}

/** process a numeric multiplication */
fun multiplyNumber() {
    code.multiplyAccumulator()
}

/** process a numeric division */
fun divideNumber() {
    code.divideAccumulator()
}
