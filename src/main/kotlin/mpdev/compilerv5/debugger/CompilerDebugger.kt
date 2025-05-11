package mpdev.compilerv5.debugger

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.scanner.Kwd
import mpdev.compilerv5.scanner.Token
import mpdev.compilerv5.util.Utils.Companion.exit

class CompilerDebugger(val context: CompilerContext) {

    /** analyse tokens - debug mode */
    fun printDebugInfo() {
        println("Environment")
        println("===========")
        System.getenv().forEach { (k, v) -> println("$k-> [$v]") }
        println("\nTokenized Program")
        println(  "=================")
        val inputProgram = context.tokenizedProgram
        for (indx in inputProgram.indices) {
            println("line number: ${inputProgram[indx].lineNumber} "+
                    "| current token: [${inputProgram[indx].encToken} ${inputProgram[indx].type} ${inputProgram[indx].value}] " +
                    if (indx < inputProgram.lastIndex) "| next token: [${inputProgram[indx+1].encToken} ${inputProgram[indx+1].type} ${inputProgram[indx+1].value}]"
                    else "")
        }
        println("\nScanner Debug Info")
        println(  "=================")
        val scanner = Config.scanner
        var t: Token
        while(true) {
            t = scanner.match()
            //todo: check why this returns endOfPRogram as the next token after endOfInput
            println("line number: ${t.lineNumber} "+
                    "| current token: [${t.encToken} ${t.type} ${t.value}] " +
                    "| next token: [${scanner.lookahead().encToken} ${scanner.lookahead().type} ${scanner.lookahead().value}]")
            if (t.encToken == Kwd.endOfInput)
                break
        }
        exit("end of debug run")
    }
}