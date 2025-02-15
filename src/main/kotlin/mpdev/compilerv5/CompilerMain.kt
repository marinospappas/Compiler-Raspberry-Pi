package mpdev.compilerv5

import mpdev.compilerv3.scanner.InputProgramScanner
import mpdev.compilerv3.code_module.Arm_32Instructions
import mpdev.compilerv3.code_module.AsmInstructions
import mpdev.compilerv3.code_module.X86_64Instructions
import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv3.config.Config
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
    val context = mpdev.compilerv5.config.CompilerContext()
    val compiler = Compiler(context)
    compiler.init(args)
    if (context.debugMode) {
        CompilerDebugger(context).printDebugInfo()
    } else {
        Compiler(context).compile()
    }
}
