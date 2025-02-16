package mpdev.compilerv5.parser.expressions

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.config.Constants.Companion.BOOLEAN_TRUE
import mpdev.compilerv5.scanner.*

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
class BooleanExpressionParser(val context: CompilerContext) {

    private val scanner = Config.scanner
    private val code = Config.codeModule
    private val exprParser = Config.expressionParser

    /** parse a Boolean expression */
    fun parse(): DataType {
        val typeT1 = parseBooleanTerm()
        while (scanner.lookahead().type == TokType.boolOrOps) {
            code.saveAccumulator()
            when (scanner.lookahead().encToken) {
                Kwd.boolOrOp -> boolOr(typeT1)
                else -> scanner.expected("boolean or operator")
            }
        }
        return typeT1
    }

    /** parse a boolean term */
    private fun parseBooleanTerm(): DataType {
        val typeF1 = parseNotFactor()
        while (scanner.lookahead().type == TokType.boolAndOps) {
            code.saveAccumulator()
            when (scanner.lookahead().encToken) {
                Kwd.boolAndOp -> boolAnd(typeF1)
                else -> scanner.expected("boolean and operator")
            }
        }
        return typeF1
    }

    /** parse a not factor */
    private fun parseNotFactor(): DataType {
        val typeF: DataType
        if (scanner.lookahead().encToken == Kwd.boolNotOp) {
            scanner.match()
            typeF = parseBooleanFactor()
            checkOperandTypeCompatibility(typeF, DataType.none, BOOL_NOT)
            code.booleanNotAccumulator()
        } else
            typeF = parseBooleanFactor()
        return typeF
    }

    /** parse a boolean factor */
    private fun parseBooleanFactor(): DataType {
        if (scanner.lookahead().encToken == Kwd.booleanLit) {
            if (scanner.match(Kwd.booleanLit).value == BOOLEAN_TRUE)
                code.setAccumulator("1")
            else
                code.clearAccumulator()
            return DataType.int
        } else
            return parseRelation()
    }

    /** parse a relation */
    private fun parseRelation(): DataType {
        val typeE1 = exprParser.parseExpression()
        if (scanner.lookahead().type == TokType.relOps) {
            if (typeE1 == DataType.string)
                code.saveString()
            else
                code.saveAccumulator()
            when (scanner.lookahead().encToken) {
                Kwd.isEqual -> parseEquals(typeE1)
                Kwd.isNotEqual -> parseNotEquals(typeE1)
                Kwd.isLess -> parseLess(typeE1)
                Kwd.isLessOrEq -> parseLessEqual(typeE1)
                Kwd.isGreater -> parseGreater(typeE1)
                Kwd.isGreaterOrEq -> parseGreaterEqual(typeE1)
                else -> scanner.expected("relational operator")
            }
            return DataType.int
        } else
            return typeE1
    }

    /** parse boolean or */
    private fun boolOr(typeE1: DataType) {
        scanner.match()
        val typeE2 = parseBooleanTerm()
        checkOperandTypeCompatibility(typeE1, typeE2, BOOL_OR)
        code.booleanOrAccumulator()
    }

    /** parse boolean and */
    private fun boolAnd(typeF1: DataType) {
        scanner.match()
        val typeF2 = parseNotFactor()
        checkOperandTypeCompatibility(typeF1, typeF2, BOOL_AND)
        code.booleanAndAccumulator()
    }

    /** parse is equal to */
    private fun parseEquals(typeE1: DataType) {
        scanner.match()
        val typeE2 = exprParser.parseExpression()
        checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_EQ)
        when (typeE1) {
            DataType.int, DataType.memptr -> code.compareEquals()
            DataType.string -> code.compareStringEquals()
            else -> {}
        }
    }

    /** parse is not equal to */
    private fun parseNotEquals(typeE1: DataType) {
        scanner.match()
        val typeE2 = exprParser.parseExpression()
        checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_NE)
        when (typeE1) {
            DataType.int, DataType.memptr -> code.compareNotEquals()
            DataType.string -> code.compareStringNotEquals()
            else -> {}
        }
    }

    /** parse is less than */
    private fun parseLess(typeE1: DataType) {
        scanner.match()
        val typeE2 = exprParser.parseExpression()
        checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_LT)
        when (typeE1) {
            DataType.int, DataType.memptr -> code.compareLess()
            else -> {}
        }
    }

    /** parse is less than or equal to */
    private fun parseLessEqual(typeE1: DataType) {
        scanner.match()
        val typeE2 = exprParser.parseExpression()
        checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_LE)
        when (typeE1) {
            DataType.int, DataType.memptr -> code.compareLessEqual()
            else -> {}
        }
    }

    /** parse is greater than */
    private fun parseGreater(typeE1: DataType) {
        scanner.match()
        val typeE2 = exprParser.parseExpression()
        checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_GT)
        when (typeE1) {
            DataType.int, DataType.memptr -> code.compareGreater()
            else -> {}
        }
    }

    /** parse is greater than or equal to */
    private fun parseGreaterEqual(typeE1: DataType) {
        scanner.match()
        val typeE2 = exprParser.parseExpression()
        checkOperandTypeCompatibility(typeE1, typeE2, COMPARE_GE)
        when (typeE1) {
            DataType.int, DataType.memptr -> code.compareGreaterEqual()
            else -> {}
        }
    }
}
