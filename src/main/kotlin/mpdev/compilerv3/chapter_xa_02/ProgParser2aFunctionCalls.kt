package mpdev.compilerv3.chapter_xa_02

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
        when (paramExprType) {
            DataType.int, DataType.intptr -> code.setIntTempFunParam(i)      // the same code is used for int, intptr and for string parameters
            DataType.string -> code.setIntTempFunParam(i)   // i.e. moves %rax to the appropriate register for this parameter
            else -> {}
        }
    }
}

/** set the registers to pass the parameter values as per assembler spec */
fun setInpFunParams(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in paramTypeList.indices)
        code.setFunParamReg(paramTypeList.size - i - 1)
}

/** restore the cpu registers used for the function params that were saved before the call */
fun restoreParamRegisters(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in paramTypeList.indices)
        code.restoreFunTempParamReg(paramTypeList.size - i - 1)
}

/** recover the space taken by any stack parameters */
fun restoreFunctionStackParams(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in paramTypeList.indices)
        code.restoreFunStackParam(paramTypeList.size - i - 1)
}