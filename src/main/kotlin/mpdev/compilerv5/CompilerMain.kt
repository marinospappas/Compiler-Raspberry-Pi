package mpdev.compilerv5

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.debugger.CompilerDebugger

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
        Compiler(context).compile()
    }
}
