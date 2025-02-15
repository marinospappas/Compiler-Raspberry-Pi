package mpdev.compilerv5.parser.expressions

import mpdev.compilerv3.abort
import mpdev.compilerv3.code
import mpdev.compilerv3.inp
import mpdev.compilerv3.parser.*
import mpdev.compilerv3.parser.function_calls.parseFunctionCall
import mpdev.compilerv3.parser.operations.*
import mpdev.compilerv3.scanner.*

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
    if (setOf(DataType.intarray, DataType.bytearray).contains(typeVar)) {
        parseArrayIndex()
        code.saveAccToTempAssigmentReg()
    }
    inp.match(Kwd.equalsOp)
    val typeExp = parseBooleanExpression()
    checkOperandTypeCompatibility(typeVar, typeExp, ASSIGN)
    when (typeVar) {
        DataType.int, DataType.memptr -> parseNumAssignment(identName)
        DataType.byte -> parseByteNumAssignment(identName)
        DataType.intarray -> parseArrayAssignment(identName)
        DataType.bytearray -> parseByteArrayAssignment(identName)
        DataType.string -> mpdev.compilerv5.parser.operations.parseStringAssignment(identName)
        else -> {}
    }
}

/**
 * parse pointer assignment
 * <assignment> ::= [ <pointer> ] = <expression>
 * stores the result of <expression> to the address the pointer points to
 */
fun parsePtrAssignment() {
    val ptrType = parsePtrExpression()
    code.saveAccToTempAssigmentReg()
    inp.match(Kwd.equalsOp)
    val typeExp = parseBooleanExpression()
    checkOperandTypeCompatibility(ptrType, typeExp, ASSIGN)
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
    else if (inp.lookahead().encToken == Kwd.notOp) {
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
        Kwd.string -> return mpdev.compilerv5.parser.operations.parseStringLiteral()
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
 * returns the data type memptr
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
    return DataType.memptr
}

/** parse Pointer - returns the value a pointer points to */
fun parsePointer(): DataType {
    val expType = parsePtrExpression()
    code.saveAccToTempReg()
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
    if (expType != DataType.memptr)
        abort("line ${inp.currentLineNumber}: expected pointer expression, found ${expType}")
    inp.match(Kwd.ptrClose)
    return if (expType == DataType.memptr) DataType.int else DataType.none
}

/**
 * parse array index
 * parses an array element index expression and saves its value
 */
fun parseArrayIndex() {
    inp.match(Kwd.arrayIndx)
    val expType = parseExpression()
    if (expType != DataType.int)
        abort("line ${inp.currentLineNumber}: expected int array index, found ${expType}")
    inp.match(Kwd.arrayIndx)
}

/**
 * parse an array element
 * parses an array element expression and saves its address
 */
fun parseArrayElement(): DataType {
    var arrayName = inp.match().value
    if (inp.lookahead().encToken == Kwd.arrayIndx) {
        parseArrayIndex()
        code.saveAccToTempReg()
        return parseArrayVariable(arrayName)
    }
    else {
        if (identifiersMap[arrayName]?.isStackVar == true)
            identifiersMap[arrayName]?.stackOffset?.let { code.setAccumulatorToLocalVarAddress(it) }
        else
            code.setAccumulatorToVarAddress(arrayName)
        return DataType.intarray
    }
}

/**
 * parse an array element
 * parses an array element expression and saves its address
 */
fun parseByteArrayElement(): DataType {
    val arrayName = inp.match().value
    if (inp.lookahead().encToken == Kwd.arrayIndx) {
        parseArrayIndex()
        code.saveAccToTempReg()
        return parseByteArrayVariable(arrayName)
    }
    else {
        if (identifiersMap[arrayName]?.isStackVar == true)
            identifiersMap[arrayName]?.stackOffset?.let { code.setAccumulatorToLocalVarAddress(it) }
        else
            code.setAccumulatorToVarAddress(arrayName)
        return DataType.bytearray
    }
}

/**
 * parse a reference to a variable
 * different code generated for local or global variable
 * returns the data type of the variable
 */
fun parseVariable(): DataType {
    return when (getType(inp.lookahead().value)) {
        DataType.int -> parseNumVariable(DataType.int)
        DataType.byte -> parseNumByteVariable()
        DataType.memptr -> parseNumVariable(DataType.memptr)
        DataType.intarray -> parseArrayElement()
        DataType.bytearray -> parseByteArrayElement()
        DataType.string -> mpdev.compilerv5.parser.operations.parseStringVariable()
        else -> DataType.void
    }
}
