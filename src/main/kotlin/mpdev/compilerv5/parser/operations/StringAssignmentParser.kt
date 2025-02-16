package mpdev.compilerv5.parser.operations

import mpdev.compilerv5.code_module.AsmInstructions
import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.config.Constants.Companion.STRING_CONST_PREFIX
import mpdev.compilerv5.scanner.*

class StringAssignmentParser(val context: CompilerContext) {

    private lateinit var scanner: InputProgramScanner
    private lateinit var code: AsmInstructions

    fun initialise() {
        scanner = Config.scanner
        code = Config.codeModule
    }

    /** parse string assignment */
    fun parseStringAssignment(varName: String) {
        if (context.identifiersMap[varName]?.isStackVar == true)
            context.identifiersMap[varName]?.stackOffset?.let { code.assignmentStringLocalVar(it) }
        else
            code.assignmentString(varName)
    }

    /** parse string literal */
    fun parseStringLiteral(): DataType {
        val stringValue = scanner.match(Kwd.string).value
        // check if this string exists already in our map of constant strings and add it if not
        var stringAddress = ""
        context.stringConstants.forEach { (k, v) -> if (v == stringValue) stringAddress = k }
        if (stringAddress == "") {  // if not found
            // save the string in the map of constant strings
            stringAddress = STRING_CONST_PREFIX + (++context.stringCnstIndx).toString()
            context.stringConstants[stringAddress] = stringValue
        }
        code.getStringVarAddress(stringAddress)
        return DataType.string
    }

    /** parse string variable */
    fun parseStringVariable(): DataType {
        val strVarName = scanner.match(Kwd.identifier).value
        if (context.identifiersMap[strVarName]?.isStackVar == true)
            context.identifiersMap[strVarName]?.stackOffset?.let { code.setAccumulatorToLocalVar(it) }
        else
            code.getStringVarAddress(strVarName)
        return DataType.string
    }
}