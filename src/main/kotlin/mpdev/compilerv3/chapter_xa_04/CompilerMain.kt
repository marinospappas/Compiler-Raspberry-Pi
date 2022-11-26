package mpdev.compilerv3.chapter_xa_04

import java.lang.System.err
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

/**
 * A Simple Compiler
 * Based on the Let's Build a Compiler! series by Jack Crenshaw
 * This version produces Assembly language code for the x86-64 and Arm 32-bit microprocessors
 * Version 4.0 20.11.2022
 */

const val USAGE = "usage: CompilerMain [-debug] [-maxstring nnnn] [-x86 | -arm] [-o output_file] input_file"

// compiler flags set by cmd line options
var debugMode = false

// the input and output files
var inFile = ""
var outFile = ""

// the input program scanner
lateinit var inp: InputProgramScanner
// the code module
lateinit var code: CodeModule
// the target cpu architecture
var cpuArchitecture = CPUArch.x86
enum class CPUArch {x86, arm}

/** report an error */
fun error(errMsg: String) {
    err.println("Error: $errMsg")
}

/** abort compilation */
fun abort(errMsg: String) {
    error(errMsg)
    exitProcess(1)
}

/** print message and exit */
fun exit(msg: String) {
    err.println(msg)
    exitProcess(0)
}

/** process command line arguments */
fun getNextArg(args: Array<String>, index: Int, argName: String = ""): String {
    if (index >= args.size)
        exit("missing argument(s) $argName, $USAGE")
    return args[index]
}
fun processCmdLineArgs(args: Array<String>) {
    var argIndx = -1
    while (++argIndx < args.size) {
        val arg = getNextArg(args, argIndx)
        if (arg[0] == '-')
            when (arg) {
                "-?", "-h", "-H" -> exit(USAGE)
                "-debug" -> debugMode = true
                "-maxstring" -> { STR_BUF_SIZE = getNextArg(args, ++argIndx, "max_string").toInt(); continue }
                "-o", "-O" -> { outFile = getNextArg(args, ++argIndx, "output_file"); continue }
                "-x86" -> cpuArchitecture = CPUArch.x86
                "-arm" -> cpuArchitecture = CPUArch.arm
                else -> exit("invalid option [$arg]\n$USAGE")
            }
        else
            inFile = arg
    }
    if (inFile == "")
        exit("missing argument input_file, $USAGE")
    if (inFile == outFile)
        exit("input and output files are identical ($inFile) - aborting\n$USAGE")
}

/** compiler initialisation */
fun initCompiler(args: Array<String>) {
    processCmdLineArgs(args)
    // initialise the code module
    code = when (cpuArchitecture) {
        CPUArch.x86 -> X86_64Instructions(outFile)
        CPUArch.arm -> Arm_32Instructions(outFile)
    }
    // initialise the scanner module
    inp = InputProgramScanner(inFile)
}

/** analyse tokens - debug mode */
fun debugCompiler() {
    println("environment")
    System.getenv().forEach { (k, v) -> println("$k-> [$v]") }
    println("\nstarting debug run")
    var t: Token
    while(true) {
        t = inp.match()
        println("${inp.debugGetLineInfo()}, ${inp.debugGetNextChar()}, ${inp.debugGetCursor()} "+
            "| current token: [${t.encToken} ${t.type} ${t.value}] " +
            "| next token: [${inp.lookahead().encToken} ${inp.lookahead().type} ${inp.lookahead().value}] |")
        if (t.encToken == Kwd.endOfInput)
            break
    }
}

/** main function */
fun main(args: Array<String>) {
    println("TINSEL(c) compiler v4.0 December 2022, Copyright M.Pappas")
    initCompiler(args)
    println("Target architecture: ${if (cpuArchitecture==CPUArch.x86) "x86-64" else "Arm-32"}\n")
    if (debugMode) {
        debugCompiler()
        exit("end of debug run")
    }
    val elapsedTime = measureTimeMillis { parseProgram() }
    println("Successful compilation, $inFile: ${inp.currentLineNumber-1} source lines, $outFile: ${code.outputLines} assembly lines")
                                        // -1 is needed as an extra new line was added when the input was read
    println("Completed in: $elapsedTime milliseconds")
}
