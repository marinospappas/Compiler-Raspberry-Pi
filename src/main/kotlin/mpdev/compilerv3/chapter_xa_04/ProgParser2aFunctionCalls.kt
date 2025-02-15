package mpdev.compilerv3.chapter_xa_04

/**
 * parse a function call
 * <function_call> ::= <function_name> ( [ <parameter> [, <parameter> ] ] )
 * returns the data type of the function
 */
fun parseFunctionCall(): DataType {
    val funcName = inp.match(Kwd.identifier).value
    inp.match(Kwd.leftParen)
    parseAssignFunParams(funcName)
    setInpFunParams(funcName)
    inp.match(Kwd.rightParen)
    code.callFunction(funcName)
    restoreFunctionStackParams(funcName)
    restoreParamRegisters(funcName)
    return getType(funcName)
}

/**
 * assign values to function parameters
 * <parameter> ::= <boolean expression>
 */
fun parseAssignFunParams(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in paramTypeList.indices) {
        if (i > 0)
            inp.match(Kwd.commaToken)
        val paramExprType = parseBooleanExpression()
        if (paramExprType != paramTypeList[i].type)
            abort("line ${inp.currentLineNumber}: parameter #${i + 1} must be type ${paramTypeList[i].type}, found $paramExprType")
        // all params but the last one are saved to temp registers
        // the last param remains in the accumulator
        if (i < paramTypeList.size - 1)
            code.setIntTempFunParam(i)
    }
}

/** set the registers to pass the parameter values as per assembler spec */
fun setInpFunParams(functionName: String) {
    code.outputCommentNl("\tset input parameters")
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    if (paramTypeList.size > 0)
        code.setFunParamRegFromAcc(paramTypeList.size-1)    // last param is still in accumulator
    for (i in 0 until paramTypeList.size-1)
        code.setFunParamRegFromTempReg(i)
}

/** restore the cpu registers used for the function params that were saved before the call */
fun restoreParamRegisters(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in 0 until paramTypeList.size-1)
        code.restoreFunTempParamReg(paramTypeList.size - i - 2)
}

/** recover the space taken by any stack parameters */
fun restoreFunctionStackParams(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in paramTypeList.indices)
        code.restoreFunStackParam(paramTypeList.size - i - 1)
}