package mpdev.compilerv5.parser.operations

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.scanner.*

class StringAssignmentParser(val context: CompilerContext) {

    private val scanner = Config.scanner
    private val code = Config.codeModule

    /** parse string assignment */
    fun parseStringAssignment(varName: String) {
        if (identifiersMap[varName]?.isStackVar == true)
            identifiersMap[varName]?.stackOffset?.let { code.assignmentStringLocalVar(it) }
        else
            code.assignmentString(varName)
    }

    /** parse string literal */
    fun parseStringLiteral(): DataType {
        val stringValue = scanner.match(Kwd.string).value
        // check if this string exists already in our map of constant strings and add it if not
        var stringAddress = ""
        stringConstants.forEach { (k, v) -> if (v == stringValue) stringAddress = k }
        if (stringAddress == "") {  // if not found
            // save the string in the map of constant strings
            stringAddress = STRING_CONST_PREFIX + (++stringCnstIndx).toString()
            stringConstants[stringAddress] = stringValue
        }
        code.getStringVarAddress(stringAddress)
        return DataType.string
    }

    /** parse string variable */
    fun parseStringVariable(): DataType {
        val strVarName = scanner.match(Kwd.identifier).value
        if (identifiersMap[strVarName]?.isStackVar == true)
            identifiersMap[strVarName]?.stackOffset?.let { code.setAccumulatorToLocalVar(it) }
        else
            code.getStringVarAddress(strVarName)
        return DataType.string
    }
}