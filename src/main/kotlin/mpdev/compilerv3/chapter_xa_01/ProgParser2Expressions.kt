package mpdev.compilerv3.chapter_xa_01

/**
 * Program parsing - module
 * Numerical Expressions
 */

/**
 * parse assignment
 * <assignment> ::= <identifier> = <expression>
 */
fun parseAssignment() {
    val identName: String = inp.match(Kwd.identifier).value
    checkCanAssign(identName)
    val typeVar = getType(identName)
    inp.match(Kwd.equalsOp)
    val typeExp = parseBooleanExpression()
    checkOperandTypeCompatibility(typeVar, typeExp, ASSIGN)
    when (typeVar) {
        DataType.int -> parseNumAssignment(identName)
        DataType.string -> parseStringAssignment(identName)
        else -> {}
    }
}

/** check if variable can be assigned a value */
fun checkCanAssign(identName: String) {
    if (!getCanAssign(identName))
        abort ("line ${inp.currentLineNumber}: variable/parameter $identName cannot be assigned a value")
}

/**
 * parse a numeric expression
 * <expression> ::= <term> [ <addop> <term> ] *
 * returns the data type of the expression
 */
fun parseExpression(): DataType {
    val typeT1 = parseTerm()
    while (inp.lookahead().type == TokType.addOps) {
        if (typeT1 == DataType.string)
            code.saveString()
        else
            code.saveAccumulator()
        when (inp.lookahead().encToken) {
            Kwd.addOp -> add(typeT1)
            Kwd.subOp -> subtract(typeT1)
            else -> inp.expected("add or subtract operator")
        }
    }
    return typeT1
}

/**
 * parse a term
 * <term> ::= <signed factor> [ <mulop> <factor> ] *
 * returns the data type of the term
 */
fun parseTerm(): DataType {
    val typeF1 = parseSignedFactor()
    while (inp.lookahead().type == TokType.mulOps) {
        if (typeF1 == DataType.string)
            code.saveString()
        else
            code.saveAccumulator()
        when (inp.lookahead().encToken) {
            Kwd.mulOp -> multiply(typeF1)
            Kwd.divOp -> divide(typeF1)
            else -> inp.expected("multiply or divide operator")
        }
    }
    return typeF1
}

/**
 * parse a signed factor
 * this can be only the first factor in a term
 * <signed factor> ::= [ addop ] <factor>
 */
fun parseSignedFactor(): DataType {
    val factType: DataType
    if (inp.lookahead().encToken == Kwd.addOp)
        inp.match()
    if (inp.lookahead().encToken == Kwd.subOp) {
        inp.match()
        if (inp.lookahead().encToken == Kwd.number) {
            factType = DataType.int
            checkOperandTypeCompatibility(factType, DataType.none, SIGNED)
            code.setAccumulator("-${inp.match(Kwd.number).value}")
        }
        else {
            factType = parseFactor()
            checkOperandTypeCompatibility(factType, DataType.none, SIGNED)
            code.negateAccumulator()
        }
    }
    else
        factType = parseFactor()
    return factType
}

/**
 * parse a factor
 * <factor> ::= ( <expression> ) | <integer> | <identifier>
 * returns the data type of factor
 */
fun parseFactor(): DataType {
    when (inp.lookahead().encToken) {
        Kwd.leftParen -> return parseParenExpression()
        Kwd.identifier -> return parseIdentifier()
        Kwd.number -> return parseNumber()
        Kwd.string -> return parseStringLiteral()
        else -> inp.expected("valid factor (expression, number or string)")
    }
    return DataType.void    // dummy instruction
}

/**
 * parse a parenthesised expression
 * returns the data type of the parenth. expression
 */
fun parseParenExpression(): DataType {
    inp.match()
    val expType = parseExpression()
    if (expType == DataType.string)
        abort("line ${inp.currentLineNumber}: parenthesis not allowed in string expressions")
    inp.match(Kwd.rightParen)
    return expType
}

/**
 * parse an identifier
 * <identifier> ::= <variable> | <function>
 * returns the data type of the identifier
 */
fun parseIdentifier(): DataType {
    when (inp.lookahead().type) {
        TokType.variable -> return parseVariable()
        TokType.function -> return parseFunctionCall()
        else -> abort("line ${inp.currentLineNumber}: undeclared identifier [${inp.lookahead().value}]")
    }
    return DataType.void    // dummy instruction
}

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
            abort ("line ${inp.currentLineNumber}: parameter #${i+1} must be type ${paramTypeList[i].type}, found $paramExprType")
        when (paramExprType) {
            DataType.int -> code.setIntTempFunParam(i)      // the same code is used both for int and for string parameters
            DataType.string -> code.setIntTempFunParam(i)   // i.e. moves %rax to the appropriate register for this parameter
            else -> {}
        }
    }
}

/** set the registers to pass the parameter values as per assembler spec */
fun setInpFunParams(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in paramTypeList.indices)
        code.setFunParamReg(i)
}

/** restore the cpu registers used for the function params that were saved before the call */
fun restoreParamRegisters(functionName: String) {
    val paramTypeList = funParamsMap[functionName] ?: listOf()
    for (i in paramTypeList.indices)
        code.restoreFunTempParamReg(paramTypeList.size - i - 1)
}

/**
 * parse a reference to a variable
 * different code generated for local or global variable
 * returns the data type of the variable
 */
fun parseVariable(): DataType {
    return when (getType(inp.lookahead().value)) {
        DataType.int -> parseNumVariable()
        DataType.string -> parseStringVariable()
        else -> DataType.void
    }
}

/** parse an addition */
fun add(typeT1: DataType) {
    inp.match()
    val typeT2 = parseTerm()
    checkOperandTypeCompatibility(typeT1, typeT2, ADD)
    when (typeT1) {
        DataType.int -> addNumber()
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
        DataType.int -> subtractNumber()
        else -> {}
    }
}

/** parse a multiplication */
fun multiply(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, MULTIPLY)
    when (typeF1) {
        DataType.int -> multiplyNumber()
        else -> {}
    }
}

/** parse a division */
fun divide(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, DIVIDE)
    when (typeF1) {
        DataType.int -> divideNumber()
        else -> {}
    }
}

