package mpdev.compilerv5.config

import mpdev.compilerv5.CPUArch
import mpdev.compilerv5.parser.declarations.FunctionParameter
import mpdev.compilerv5.scanner.DataType
import mpdev.compilerv5.scanner.IdentifierDecl
import mpdev.compilerv5.scanner.Token

data class CompilerContext(
    var debugMode: Boolean = false,                 // debug flag set by cmd line options
    var inFile: String = "",                        // the input and output files
    var outFile: String = "",
    var cpuArchitecture: CPUArch = CPUArch.x86,     // the target cpu architecture
    var stringBufferSize: Int = 1024,               // the buffer size for string operations
    var implementStart: Boolean = false,             // implement _start entry point (when no C lib is used)
    var startFunSrc: String = "./tinsel/lib/x86/_start.s"   // file containing the source for the _start code
) {
    // the input program tokenized
    val tokenizedProgram = mutableListOf<Token>()

    // the identifiers space map
    val identifiersMap = mutableMapOf<String, IdentifierDecl>()

    // the function parameters map - key is the functionName
    val funParamsMap = mutableMapOf<String, List<FunctionParameter>>()

    // the local variables map - key is the blockName
    val localVarsMap = mutableMapOf<String, MutableList<String>>()

    // the string constants (will be included in the output file at the end of the compilation)
    val stringConstants = mutableMapOf<String, String>()
    var stringCnstIndx = 0

    /** return the type of var/fun */
    fun getType(identifier: String): DataType = identifiersMap[identifier]?.type?: DataType.none

    /** return the canAssign flag */
    fun getCanAssign(identifier: String): Boolean = identifiersMap[identifier]?.canAssign?:false
}
