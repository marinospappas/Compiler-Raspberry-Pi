package mpdev.compilerv3.debugger

import mpdev.compilerv3.CompilerContext
import mpdev.compilerv3.scanner.Kwd
import mpdev.compilerv3.scanner.Token
import mpdev.compilerv3.util.Utils.Companion.exit

class CompilerDebugger(val context: CompilerContext) {

    /** analyse tokens - debug mode */
    fun printDebugInfo() {
        val scanner = context.scanner
        println("environment")
        System.getenv().forEach { (k, v) -> println("$k-> [$v]") }
        println("\nstarting debug run")
        var t: Token
        while(true) {
            t = scanner.match()
            println("${scanner.debugGetLineInfo()}, ${scanner.debugGetNextChar()}, ${scanner.debugGetCursor()} "+
                    "| current token: [${t.encToken} ${t.type} ${t.value}] " +
                    "| next token: [${scanner.lookahead().encToken} ${scanner.lookahead().type} ${scanner.lookahead().value}] |")
            if (t.encToken == Kwd.endOfInput)
                break
        }
        exit("end of debug run")
    }
}