package mpdev.compilerv5.parser.expressions

import mpdev.compilerv5.code_module.AsmInstructions
import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.parser.function_calls.FunctionCallParser
import mpdev.compilerv5.parser.operations.NumericAssignementParser
import mpdev.compilerv5.parser.operations.OperationsParser
import mpdev.compilerv5.parser.operations.StringAssignmentParser
import mpdev.compilerv5.scanner.*
import mpdev.compilerv5.util.Utils.Companion.abort

/**
 * Program parsing - module
 * Numerical Expressions
 */

class ExpressionParser(val context: CompilerContext) {

    private lateinit var scanner: InputProgramScanner
    private lateinit var code: AsmInstructions
    private lateinit var booleanExprParser: BooleanExpressionParser
    private lateinit var operationsParser: OperationsParser
    private lateinit var funCallParser: FunctionCallParser
    private lateinit var numAssgnmtParser: NumericAssignementParser
    private lateinit var strAssgnmtParser: StringAssignmentParser

    fun initialise() {
        scanner = Config.scanner
        code = Config.codeModule
        booleanExprParser = Config.booleanExpressionParser
        operationsParser = Config.operationsParser
        funCallParser = Config.functionCallParser
        numAssgnmtParser = Config.numericAssgnmtParser
        strAssgnmtParser = Config.stringAssgnmtParser
    }

    /**
     * parse assignment
     * <assignment> ::= <identifier> = <expression>
     */
    fun parseAssignment() {
        val identName: String = scanner.match(Kwd.identifier).value
        checkCanAssign(identName)
        val typeVar = getType(identName)
        if (setOf(DataType.intarray, DataType.bytearray).contains(typeVar)) {
            parseArrayIndex()
            code.saveAccToTempAssigmentReg()
        }
        scanner.match(Kwd.equalsOp)
        val typeExp = booleanExprParser.parse()
        checkOperandTypeCompatibility(typeVar, typeExp, ASSIGN)
        when (typeVar) {
            DataType.int, DataType.memptr -> numAssgnmtParser.parseNumAssignment(identName)
            DataType.byte -> numAssgnmtParser.parseByteNumAssignment(identName)
            DataType.intarray -> numAssgnmtParser.parseArrayAssignment(identName)
            DataType.bytearray -> numAssgnmtParser.parseByteArrayAssignment(identName)
            DataType.string -> strAssgnmtParser.parseStringAssignment(identName)
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
        scanner.match(Kwd.equalsOp)
        val typeExp = booleanExprParser.parse()
        checkOperandTypeCompatibility(ptrType, typeExp, ASSIGN)
        code.pointerAssignment()
    }

    /** check if variable can be assigned a value */
    private fun checkCanAssign(identName: String) {
        if (!getCanAssign(identName))
            abort("line ${scanner.currentLineNumber}: variable/parameter $identName cannot be assigned a value")
    }

    /**
     * parse a numeric expression
     * <expression> ::= <term> [ <addop> <term> ] *
     * returns the data type of the expression
     */
    fun parseExpression(): DataType {
        val typeT1 = parseTerm()
        while (scanner.lookahead().type == TokType.addOps) {
            if (typeT1 == DataType.string)
                code.saveString()
            else
                code.saveAccumulator()
            when (scanner.lookahead().encToken) {
                Kwd.addOp -> operationsParser.add(typeT1)
                Kwd.subOp -> operationsParser.subtract(typeT1)
                Kwd.orOp -> operationsParser.bitwiseOr(typeT1)
                Kwd.xorOp -> operationsParser.bitwiseXor(typeT1)
                else -> scanner.expected("add or subtract operator")
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
        while (scanner.lookahead().type == TokType.mulOps) {
            if (typeF1 == DataType.string)
                code.saveString()
            else
                code.saveAccumulator()
            when (scanner.lookahead().encToken) {
                Kwd.mulOp -> operationsParser.multiply(typeF1)
                Kwd.divOp -> operationsParser.divide(typeF1)
                Kwd.modOp -> operationsParser.modulo(typeF1)
                Kwd.shlOp -> operationsParser.shiftLeft(typeF1)
                Kwd.shrOp -> operationsParser.shiftRight(typeF1)
                Kwd.andOp -> operationsParser.bitwiseAnd(typeF1)
                else -> scanner.expected("multiply or divide operator")
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
    private fun parseSignedFactor(): DataType {
        val factType: DataType
        if (scanner.lookahead().encToken == Kwd.addOp)
            scanner.match()
        if (scanner.lookahead().encToken == Kwd.subOp) {
            scanner.match()
            if (scanner.lookahead().encToken == Kwd.number) {
                factType = DataType.int
                checkOperandTypeCompatibility(factType, DataType.none, SIGNED)
                code.setAccumulator("-${scanner.match(Kwd.number).value}")
            } else {
                factType = parseFactor()
                checkOperandTypeCompatibility(factType, DataType.none, SIGNED)
                code.negateAccumulator()
            }
        } else if (scanner.lookahead().encToken == Kwd.notOp) {
            scanner.match()
            factType = parseFactor()
            checkOperandTypeCompatibility(factType, DataType.none, NOT)
            code.notAccumulator()
        } else
            factType = parseFactor()
        return factType
    }

    /**
     * parse a factor
     * <factor> ::= ( <expression> ) | <integer> | <identifier>
     * returns the data type of factor
     */
    fun parseFactor(): DataType {
        when (scanner.lookahead().encToken) {
            Kwd.leftParen -> return parseParenExpression()
            Kwd.identifier -> return parseIdentifier()
            Kwd.number -> return numAssgnmtParser.parseNumber()
            Kwd.string -> return strAssgnmtParser.parseStringLiteral()
            Kwd.addressOfVar -> return parseAddressOfVar()
            Kwd.ptrOpen -> return parsePointer()
            else -> scanner.expected("valid factor (expression, number or string)")
        }
        return DataType.void    // dummy instruction
    }

    /**
     * parse a parenthesised expression
     * returns the data type of the parenth. expression
     */
    private fun parseParenExpression(): DataType {
        scanner.match()
        val expType = parseExpression()
        if (expType == DataType.string)
            abort("line ${scanner.currentLineNumber}: parenthesis not allowed in string expressions")
        scanner.match(Kwd.rightParen)
        return expType
    }

    /**
     * parse an identifier
     * <identifier> ::= <variable> | <function>
     * returns the data type of the identifier
     */
    private fun parseIdentifier(): DataType {
        when (scanner.lookahead().type) {
            TokType.variable -> return parseVariable()
            TokType.function -> return funCallParser.parse()
            else -> abort("line ${scanner.currentLineNumber}: undeclared identifier [${scanner.lookahead().value}]")
        }
        return DataType.void    // dummy instruction
    }

    /**
     * parse a call to address of variable
     * returns the data type memptr
     */
    private fun parseAddressOfVar(): DataType {
        scanner.match()
        scanner.match(Kwd.leftParen)
        val nextToken = scanner.match(Kwd.identifier)
        val varName = nextToken.value
        if (nextToken.type != TokType.variable)
            abort("line ${scanner.currentLineNumber}: expected variable name, found ${varName}")
        if (identifiersMap[varName]?.isStackVar == true)
            identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalVarAddress(it) }
        else
            code.setAccumulatorToVarAddress(varName)
        scanner.match(Kwd.rightParen)
        return DataType.memptr
    }

    /** parse Pointer - returns the value a pointer points to */
    private fun parsePointer(): DataType {
        val expType = parsePtrExpression()
        code.saveAccToTempReg()
        code.setAccumulatorToPointerVar()
        return expType
    }

    /**
     * parse a pointer expression
     * parses an expression containing a pointer and saves the address it points to
     */
    private fun parsePtrExpression(): DataType {
        scanner.match()
        val expType = parseExpression()
        if (expType != DataType.memptr)
            abort("line ${scanner.currentLineNumber}: expected pointer expression, found ${expType}")
        scanner.match(Kwd.ptrClose)
        return if (expType == DataType.memptr) DataType.int else DataType.none
    }

    /**
     * parse array index
     * parses an array element index expression and saves its value
     */
    private fun parseArrayIndex() {
        scanner.match(Kwd.arrayIndx)
        val expType = parseExpression()
        if (expType != DataType.int)
            abort("line ${scanner.currentLineNumber}: expected int array index, found ${expType}")
        scanner.match(Kwd.arrayIndx)
    }

    /**
     * parse an array element
     * parses an array element expression and saves its address
     */
    private fun parseArrayElement(): DataType {
        val arrayName = scanner.match().value
        if (scanner.lookahead().encToken == Kwd.arrayIndx) {
            parseArrayIndex()
            code.saveAccToTempReg()
            return numAssgnmtParser.parseArrayVariable(arrayName)
        } else {
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
    private fun parseByteArrayElement(): DataType {
        val arrayName = scanner.match().value
        if (scanner.lookahead().encToken == Kwd.arrayIndx) {
            parseArrayIndex()
            code.saveAccToTempReg()
            return numAssgnmtParser.parseByteArrayVariable(arrayName)
        } else {
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
    private fun parseVariable(): DataType {
        return when (getType(scanner.lookahead().value)) {
            DataType.int -> numAssgnmtParser.parseNumVariable(DataType.int)
            DataType.byte -> numAssgnmtParser.parseNumByteVariable()
            DataType.memptr -> numAssgnmtParser.parseNumVariable(DataType.memptr)
            DataType.intarray -> parseArrayElement()
            DataType.bytearray -> parseByteArrayElement()
            DataType.string -> strAssgnmtParser.parseStringVariable()
            else -> DataType.void
        }
    }
}
