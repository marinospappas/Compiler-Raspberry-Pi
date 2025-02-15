package mpdev.compilerv5.config

import mpdev.compilerv5.CPUArch

data class CompilerContext(
    var debugMode: Boolean = false,                 // debug flag set by cmd line options
    var inFile: String = "",                        // the input and output files
    var outFile: String = "",
    var cpuArchitecture: CPUArch = CPUArch.x86,     // the target cpu architecture
    var stringBufferSize: Int = 1024                    // the buffer size for string operations
)