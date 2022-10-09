package mpdev.compilerv3.chapter_xa_02

/**
 * Program parsing - module 3
 *
 * parse a boolean expression
 * following is the whole grammar - note how <relation> links to <expression>
 *     which is the link between Boolean and Numerical expressions
 * with this grammar any integer can be boolean and vice-versa
 * any assignment will look for a boolean expression to start with
 * and can settle for a numeric one as it goes down the grammar
 *
 * <b-expression> ::= <b-term> [ <orop> <b-term> ] *
 * <b-term> ::= <not-factor> [ <andop> <not-factor> ] *
 * <not-factor> ::= [ <notop> ] <b-factor>
 * <b-factor> ::= <b-literal> | <b-variable> | <relation>
 * <relation> ::= <expression> [ <relop> <expression ]
 * <expression> ::= <term> [ <addop> <term> ] *
 * <term> ::= <signed factor> [ <mulop> factor ] *
 * <signed factor> ::= [ <addop> ] <factor>
 * <factor> ::= <integer> | <identifier> | ( <expression> )
 *
 */

/** parse a Boolean expression */
fun parseBooleanExpression(): DataType {
    val typeT1 = parseBooleanTerm()
    while (inp.lookahead().type == TokType.boolOrOps) {
        code.saveAccumulator()
        when (inp.lookahead().encToken) {
            Kwd.boolOrOp -> boolOr(typeT1)
            else -> inp.expected("boolean or operator")
        }
    }
    return typeT1
}

/** parse a boolean term */
fun parseBooleanTerm(): DataType {
    val typeF1 = parseNotFactor()
    while (inp.lookahead().type == TokType.boolAndOps) {
        code.saveAccumulator()
        when (inp.lookahead().encToken) {
            Kwd.boolAndOp -> boolAnd(typeF1)
            else -> inp.expected("boolean and operator")
        }
    }
    return typeF1
}

/** parse a not factor */
fun parseNotFactor(): DataType {
    val typeF: DataType
    if (inp.lookahead().encToken == Kwd.boolNotOp) {
        inp.match()
        typeF = parseBooleanFactor()
        checkOperandTypeCompatibility(typeF, DataType.none, BOOL_NOT)
        code.booleanNotAccumulator()
    }
    else
        typeF = parseBooleanFactor()
    return typeF
}

/** parse a boolean factor */
fun parseBooleanFactor(): DataType {
    if (inp.lookahead().encToken == Kwd.booleanLit) {
        if (inp.match(Kwd.booleanLit).value == BOOLEAN_TRUE)
            code.setAccumulator("1")
        else
            code.clearAccumulator()
        return DataType.int
    }
    else
        return parseRelation()
}

/** parse a relation */
fun parseRelation(): DataType {
    val typeE1 = parseExpression()
    if (inp.lookahead().type == TokType.relOps) {
        if (typeE1 == DataType.string)
            code.saveString()
        else
            code.saveAccumulator()
        when (inp.lookahead().encToken) {
            Kwd.isEqual -> parseEquals(typeE1)
            Kwd.isNotEqual -> parseNotEquals(typeE1)
            Kwd.isLess -> parseLess(typeE1)
            Kwd.isLessOrEq -> parseLessEqual(typeE1)
            Kwd.isGreater -> parseGreater(typeE1)
            Kwd.isGreaterOrEq -> parseGreaterEqual(typeE1)
            else -> inp.expected("relational operator")
        }
        return DataType.int
    }
    else
        return typeE1
}

/** parse boolean or */
fun boolOr(typeE1: DataType) {
    inp.match()
    val typeE2 = parseBooleanTerm()
    checkOperandTypeCompatibility(typeE1, typeE2, BOOL_OR)
    code.booleanOrAccumulator()
}

/** parse boolean and */
fun boolAnd(typeF1: DataType) {
    inp.match()
    val typeF2 = parseNotFactor()
    checkOperandTypeCompatibility(typeF1, typeF2, BOOL_AND)
    code.booleanAndAccumulator()
}

/** parse is equal to */
fun parseEquals(typeE1: DataType) {
    inp.match()
    val typeE2 = parseExpression()
    checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_EQ)
    when (typeE1) {
        DataType.int, DataType.intptr -> code.compareEquals()
        DataType.string -> code.compareStringEquals()
        else -> {}
    }
}

/** parse is not equal to */
fun parseNotEquals(typeE1: DataType) {
    inp.match()
    val typeE2 = parseExpression()
    checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_NE)
    when (typeE1) {
        DataType.int, DataType.intptr -> code.compareNotEquals()
        DataType.string -> code.compareStringNotEquals()
        else -> {}
    }
}

/** parse is less than */
fun parseLess(typeE1: DataType) {
    inp.match()
    val typeE2 = parseExpression()
    checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_LT)
    when (typeE1) {
        DataType.int, DataType.intptr -> code.compareLess()
        else -> {}
    }
}

/** parse is less than or equal to */
fun parseLessEqual(typeE1: DataType) {
    inp.match()
    val typeE2 = parseExpression()
    checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_LE)
    when (typeE1) {
        DataType.int, DataType.intptr -> code.compareLessEqual()
        else -> {}
    }
}

/** parse is greater than */
fun parseGreater(typeE1: DataType) {
    inp.match()
    val typeE2 = parseExpression()
    checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_GT)
    when (typeE1) {
        DataType.int, DataType.intptr -> code.compareGreater()
        else -> {}
    }
}

/** parse is greater than or equal to */
fun parseGreaterEqual(typeE1: DataType) {
    inp.match()
    val typeE2 = parseExpression()
    checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_GE)
    when (typeE1) {
        DataType.int, DataType.intptr -> code.compareGreaterEqual()
        else -> {}
    }
}
