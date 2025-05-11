package mpdev.compilerv5.debugger

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.util.Utils.Companion.exit

class CompilerDebugger(val context: CompilerContext) {

    /** analyse tokens - debug mode */
    fun printDebugInfo() {
        println("environment")
        System.getenv().forEach { (k, v) -> println("$k-> [$v]") }
        println("\nstarting debug run")
        val inputProgram = context.tokenizedProgram
        for (indx in inputProgram.indices) {
            println("line number: ${inputProgram[indx].lineNumber} "+
                    "| current token: [${inputProgram[indx].encToken} ${inputProgram[indx].type} ${inputProgram[indx].value}] " +
                    if (indx < inputProgram.lastIndex) "| next token: [${inputProgram[indx+1].encToken} ${inputProgram[indx+1].type} ${inputProgram[indx+1].value}] |"
                    else "")
        }
        exit("end of debug run")
    }
}