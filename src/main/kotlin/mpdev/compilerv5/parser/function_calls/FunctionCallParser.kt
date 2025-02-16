package mpdev.compilerv5.parser.function_calls

import mpdev.compilerv5.code_module.AsmInstructions
import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.parser.expressions.BooleanExpressionParser
import mpdev.compilerv5.parser.operations.NumericAssignementParser
import mpdev.compilerv5.parser.operations.OperationsParser
import mpdev.compilerv5.parser.operations.StringAssignmentParser
import mpdev.compilerv5.scanner.*
import mpdev.compilerv5.util.Utils.Companion.abort

/**
 * parse a function call
 * <function_call> ::= <function_name> ( [ <parameter> [, <parameter> ] ] )
 * returns the data type of the function
 */
class FunctionCallParser(val context: CompilerContext) {

    private lateinit var scanner: InputProgramScanner
    private lateinit var code: AsmInstructions
    private lateinit var booleanExprParser: BooleanExpressionParser

    fun initialise() {
        scanner = Config.scanner
        code = Config.codeModule
        booleanExprParser = Config.booleanExpressionParser
    }

    fun parse(): DataType {
        val funcName = scanner.match(Kwd.identifier).value
        scanner.match(Kwd.leftParen)
        parseAssignFunParams(funcName)
        setInpFunParams(funcName)
        scanner.match(Kwd.rightParen)
        code.callFunction(funcName)
        restoreFunctionStackParams(funcName)
        restoreParamRegisters(funcName)
        return getType(funcName)
    }

    /**
     * assign values to function parameters
     * <parameter> ::= <boolean expression>
     */
    private fun parseAssignFunParams(functionName: String) {
        val paramTypeList = funParamsMap[functionName] ?: listOf()
        for (i in paramTypeList.indices) {
            if (i > 0)
                scanner.match(Kwd.commaToken)
            val paramExprType = booleanExprParser.parse()
            if (paramExprType != paramTypeList[i].type)
                abort("line ${scanner.currentLineNumber}: parameter #${i + 1} must be type ${paramTypeList[i].type}, found $paramExprType")
            // all params but the last one are saved to temp registers
            // the last param remains in the accumulator
            if (i < paramTypeList.size - 1)
                code.setIntTempFunParam(i)
        }
    }

    /** set the registers to pass the parameter values as per assembler spec */
    private fun setInpFunParams(functionName: String) {
        code.outputCommentNl("\tset input parameters")
        val paramTypeList = funParamsMap[functionName] ?: listOf()
        if (paramTypeList.size > 0)
            code.setFunParamRegFromAcc(paramTypeList.size - 1)    // last param is still in accumulator
        for (i in 0 until paramTypeList.size - 1)
            code.setFunParamRegFromTempReg(i)
    }

    /** restore the cpu registers used for the function params that were saved before the call */
    private fun restoreParamRegisters(functionName: String) {
        val paramTypeList = funParamsMap[functionName] ?: listOf()
        for (i in 0 until paramTypeList.size - 1)
            code.restoreFunTempParamReg(paramTypeList.size - i - 2)
    }

    /** recover the space taken by any stack parameters */
    private fun restoreFunctionStackParams(functionName: String) {
        val paramTypeList = funParamsMap[functionName] ?: listOf()
        for (i in paramTypeList.indices)
            code.restoreFunStackParam(paramTypeList.size - i - 1)
    }
}