package mpdev.compilerv5.parser.operations

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.scanner.DataType
import mpdev.compilerv5.scanner.Kwd
import mpdev.compilerv5.scanner.identifiersMap

class NumericAssignementParser(val context: CompilerContext) {

    private val scanner = Config.scanner
    private val code = Config.codeModule

    /** process assignment to numeric var (int) */
    fun parseNumAssignment(varName: String) {
        if (identifiersMap[varName]?.isStackVar == true)
            identifiersMap[varName]?.stackOffset?.let { code.assignmentLocalVar(it) }
        else
            code.assignment(varName)
    }

    /** process assignment to array numeric var (int) */
    fun parseArrayAssignment(arrayName: String) {
        if (identifiersMap[arrayName]?.isStackVar == true)
            identifiersMap[arrayName]?.stackOffset?.let { code.assignmentLocalArrayVar(it) }
        else
            code.arrayAssignment(arrayName)
    }

    /** process a number */
    fun parseNumber(): DataType {
        code.setAccumulator(scanner.match(Kwd.number).value)
        return DataType.int
    }

    /** process a numeric int variable */
    fun parseNumVariable(type: DataType): DataType {
        val varName = scanner.match(Kwd.identifier).value
        if (identifiersMap[varName]?.isStackVar == true)
            identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalVar(it) }
        else
            code.setAccumulatorToVar(varName)
        return type
    }

    /** process assignment to numeric var (int) */
    fun parseByteNumAssignment(varName: String) {
        if (identifiersMap[varName]?.isStackVar == true)
            identifiersMap[varName]?.stackOffset?.let { code.assignmentLocalByteVar(it) }
        else
            code.assignmentByte(varName)
    }

    /** process assignment to array numeric var (int) */
    fun parseByteArrayAssignment(arrayName: String) {
        if (identifiersMap[arrayName]?.isStackVar == true)
            identifiersMap[arrayName]?.stackOffset?.let { code.assignmentLocalByteArrayVar(it) }
        else
            code.arrayByteAssignment(arrayName)
    }

    /** process a numeric byte variable */
    fun parseNumByteVariable(): DataType {
        val varName = scanner.match(Kwd.identifier).value
        if (identifiersMap[varName]?.isStackVar == true)
            identifiersMap[varName]?.stackOffset?.let { code.setAccumulatorToLocalByteVar(it) }
        else
            code.setAccumulatorToByteVar(varName)
        return DataType.byte
    }

    /** process an array variable */
    fun parseArrayVariable(arrayName: String): DataType {
        if (identifiersMap[arrayName]?.isStackVar == true)
            identifiersMap[arrayName]?.stackOffset?.let { code.setAccumulatorToLocalArrayVar(it) }
        else
            code.setAccumulatorToArrayVar(arrayName)
        return DataType.int
    }

    /** process an array variable */
    fun parseByteArrayVariable(arrayName: String): DataType {
        if (identifiersMap[arrayName]?.isStackVar == true)
            identifiersMap[arrayName]?.stackOffset?.let { code.setAccumulatorToLocalByteArrayVar(it) }
        else
            code.setAccumulatorToByteArrayVar(arrayName)
        return DataType.int
    }
}
