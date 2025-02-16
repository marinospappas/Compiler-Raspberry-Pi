package mpdev.compilerv5

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.util.Utils.Companion.processCmdLineArgs
import kotlin.system.measureTimeMillis

class Compiler(val context: CompilerContext) {
    /** compiler initialisation */
    fun init(args: Array<String>) {
        println("TINSEL(c) compiler v5.0 February 2025, Copyright M.Pappas")
        processCmdLineArgs(args, context)
        println("Target architecture: ${if (context.cpuArchitecture == CPUArch.x86) "x86-64" else "Arm-32"}\n")
    }

    /** the actual compiler */
    fun compile() {
        val elapsedTime = measureTimeMillis {
            Config.programParser.parse()
        }
        println("Successful compilation, ${context.inFile}: ${Config.scanner.currentLineNumber-1} source lines, ${context.outFile}: ${Config.codeModule.outputLines} assembly lines")
        // -1 is needed as an extra new line was added when the input was read
        println("Completed in: $elapsedTime milliseconds")
    }
}