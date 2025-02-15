package mpdev.compilerv3

import mpdev.compilerv3.scanner.InputProgramScanner
import mpdev.compilerv3.code_module.Arm_32Instructions
import mpdev.compilerv3.code_module.AsmInstructions
import mpdev.compilerv3.code_module.X86_64Instructions
import mpdev.compilerv3.debugger.CompilerDebugger
import mpdev.compilerv3.parser.MainProgramParser
import mpdev.compilerv3.util.Utils.Companion.processCmdLineArgs
import kotlin.system.measureTimeMillis

/**
 * A Simple Compiler
 * Based on the Let's Build a Compiler! series by Jack Crenshaw
 * This version produces Assembly language code for the x86-64 and Arm 32-bit microprocessors
 * Version 4.0 20.11.2022
 */

enum class CPUArch {x86, arm}

/** main function */
fun main(args: Array<String>) {
    val context = CompilerContext()
    val compiler = Compiler(context)
    compiler.init(args)
    if (context.debugMode) {
        CompilerDebugger(context).printDebugInfo()
    } else {
        compiler.compile()
    }
}

data class CompilerContext(
    var debugMode: Boolean = false,                                         // debug flag set by cmd line options
    var inFile: String = "",                                                // the input and output files
    var outFile: String = "",
    var scanner: InputProgramScanner = InputProgramScanner(""),    // input program scanner
    var cpuArchitecture:CPUArch = CPUArch.x86,                              // the target cpu architecture
    var codeModule: AsmInstructions = X86_64Instructions()                  // the code module
)

class Compiler(val context: CompilerContext) {
    /** compiler initialisation */
    fun init(args: Array<String>) {
        println("TINSEL(c) compiler v5.0 February 2025, Copyright M.Pappas")
        processCmdLineArgs(args)
        println("Target architecture: ${if (context.cpuArchitecture == CPUArch.x86) "x86-64" else "Arm-32"}\n")
        // initialise the code module
        context.codeModule = when (context.cpuArchitecture) {
            CPUArch.x86 -> X86_64Instructions(context.outFile)
            CPUArch.arm -> Arm_32Instructions(context.outFile)
        }
        // initialise the scanner module
        context.scanner = InputProgramScanner(context.inFile)
    }

    /** the actual compiler */
    fun compile() {
        val elapsedTime = measureTimeMillis {
            MainProgramParser(context).parse()
        }
        println("Successful compilation, ${context.inFile}: ${context.scanner.currentLineNumber-1} source lines, ${context.outFile}: ${context.codeModule.outputLines} assembly lines")
        // -1 is needed as an extra new line was added when the input was read
        println("Completed in: $elapsedTime milliseconds")
    }
}
