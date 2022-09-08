package mpdev.compilerv3.chapter_xa_02

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
        DataType.int, DataType.intptr -> parseNumAssignment(identName)
        DataType.string -> parseStringAssignment(identName)
        else -> {}
    }
}

/**
 * parse pointer assignment
 * <assignment> ::= [ <pointer> ] = <expression>
 * stores the result of <expression> to the address the pointer points to
 */
fun parsePtrAssignment() {
    parsePtrExpression()
    inp.match(Kwd.equalsOp)
    val typeExp = parseBooleanExpression()
    checkOperandTypeCompatibility(DataType.ptrExpr, typeExp, ASSIGN)
    code.pointerAssignment()
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
            Kwd.orOp -> bitwiseOr(typeT1)
            Kwd.xorOp -> bitwiseXor(typeT1)
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
    //TODO: implement shift left and shift right
    val typeF1 = parseSignedFactor()
    while (inp.lookahead().type == TokType.mulOps) {
        if (typeF1 == DataType.string)
            code.saveString()
        else
            code.saveAccumulator()
        when (inp.lookahead().encToken) {
            Kwd.mulOp -> multiply(typeF1)
            Kwd.divOp -> divide(typeF1)
            Kwd.modOp -> modulo(typeF1)
            Kwd.shlOp -> shiftLeft(typeF1)
            Kwd.shrOp -> shiftRight(typeF1)
            Kwd.andOp -> bitwiseAnd(typeF1)
            else -> inp.expected("multiply or divide operator")
        }
    }
    return typeF1
}

/**
 * parse a signed factor
 * this can be only the first factor in a term
 * <signed factor> ::= [ addop ] <factor>
 * also parses bitwise not in a similar manner
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
        if (inp.lookahead().encToken == Kwd.notOp) {
            inp.match()
            factType = parseFactor()
            checkOperandTypeCompatibility(factType, DataType.none, NOT)
            code.notAccumulator()
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
        Kwd.addressOfVar -> return parseAddressOfVar()
        Kwd.ptrOpen -> return parsePointer()
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
 * parse a call to address of variable
 * returns the data type intptr
 */
fun parseAddressOfVar(): DataType {
    inp.match()
    inp.match(Kwd.leftParen)
    val nextToken = inp.match(Kwd.identifier)
    val varName = nextToken.value
    if (nextToken.type != TokType.variable)
        abort("line ${inp.currentLineNumber}: expected variable name, found ${varName}")
    if (identifiersMap[varName]?.isStackVar == true)
        identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalVarAddress(it) }
    else
        code.setAccumulatorToVarAddress(varName)
    inp.match(Kwd.rightParen)
    return DataType.intptr
}

/** parse Pointer - returns the value a pointer points to */
fun parsePointer(): DataType {
    val expType = parsePtrExpression()
    code.setAccumulatorToPointerVar()
    return expType
}

/**
 * parse a pointer expression
 * parses an expression containing a pointer and saves the address it points to
 */
fun parsePtrExpression(): DataType {
    inp.match()
    val expType = parseExpression()
    if (expType != DataType.ptrExpr)
        abort("line ${inp.currentLineNumber}: expected pointer expression, found ${expType}")
    code.savePtrValue()
    inp.match(Kwd.ptrClose)
    return expType
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
        DataType.intptr -> parsePtrVariable()
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
        DataType.int, DataType.intptr -> addNumber()
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
        DataType.int, DataType.intptr -> subtractNumber()
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

/** parse a modulo op */
fun modulo(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, MODULO)
    when (typeF1) {
        DataType.int -> moduloNumber()
        else -> {}
    }
}

/** parse a shift left op */
fun shiftLeft(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_LEFT)
    when (typeF1) {
        DataType.int -> shiftLeftNumber()
        else -> {}
    }
}

/** parse a shift right op */
fun shiftRight(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, SHIFT_RIGHT)
    when (typeF1) {
        DataType.int -> shiftRightNumber()
        else -> {}
    }
}

/** parse a bitwise or */
fun bitwiseOr(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, OR)
    when (typeF1) {
        DataType.int -> orNumber()
        else -> {}
    }
}

/** parse a bitwise xor */
fun bitwiseXor(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, XOR)
    when (typeF1) {
        DataType.int -> xorNumber()
        else -> {}
    }
}

/** parse a bitwise or */
fun bitwiseAnd(typeF1: DataType) {
    inp.match()
    val typeF2 = parseFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, AND)
    when (typeF1) {
        DataType.int -> andNumber()
        else -> {}
    }
}