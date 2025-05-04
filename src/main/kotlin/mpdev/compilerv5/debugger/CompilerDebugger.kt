package mpdev.compilerv5.debugger

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.scanner.Kwd
import mpdev.compilerv5.scanner.Token
import mpdev.compilerv5.util.Utils.Companion.exit

class CompilerDebugger(val context: CompilerContext) {

    /** analyse tokens - debug mode */
    fun printDebugInfo() {
        val tokenizer = Config.tokenizer
        val scanner = Config.scanner
        println("environment")
        System.getenv().forEach { (k, v) -> println("$k-> [$v]") }
        println("\nstarting debug run")
        context.tokenizedProgram.forEach {
            println("${tokenizer.debugGetLineInfo()}, ${tokenizer.debugGetNextChar()}, ${tokenizer.debugGetCursor()} "+
                    "| current token: [${it.encToken} ${it.type} ${it.value}] " +
                    "| next token: [${scanner.lookahead().encToken} ${scanner.lookahead().type} ${scanner.lookahead().value}] |")
        }
        exit("end of debug run")
    }
}