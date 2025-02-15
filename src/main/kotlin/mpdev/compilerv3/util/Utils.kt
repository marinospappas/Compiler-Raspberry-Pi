package mpdev.compilerv3.util

import mpdev.compilerv3.*
import mpdev.compilerv3.scanner.STR_BUF_SIZE
import java.lang.System.err
import kotlin.system.exitProcess

class Utils {

    companion object {
        private const val USAGE = "usage: CompilerMain [-debug] [-maxstring nnnn] [-x86 | -arm] [-o output_file] input_file"

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
        private fun getNextArg(args: Array<String>, index: Int, argName: String = ""): String {
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
    }
}