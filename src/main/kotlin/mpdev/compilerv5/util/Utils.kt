package mpdev.compilerv5.util

import mpdev.compilerv5.CPUArch
import mpdev.compilerv5.config.Config
import java.lang.System.err
import kotlin.system.exitProcess

class Utils {

    companion object {
        private const val USAGE = "usage: \nCompilerMain \n" +
                "    [-debug] activates debug output \n" +
                "    [-maxstring nnnn] defines maximum allowed size for strings (default 1024) \n" +
                "    [-x86 | -arm] selects output mode (default x86) \n" +
                "    [-start] generates _start entrypoint when no C library is to be used (default false)\n" +
                "    [-o output_file_name] \n" +
                "    input_file_name"

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
        fun processCmdLineArgs(args: Array<String>, context: mpdev.compilerv5.config.CompilerContext) {
            var argIndx = -1
            while (++argIndx < args.size) {
                val arg = getNextArg(args, argIndx)
                if (arg[0] == '-')
                    when (arg) {
                        "-?", "-h", "-H" -> exit(USAGE)
                        "-debug" -> context.debugMode = true
                        "-maxstring" -> { Config.STR_BUF_SIZE = getNextArg(args, ++argIndx, "max_string").toInt(); continue }
                        "-o", "-O" -> { context.outFile = getNextArg(args, ++argIndx, "output_file"); continue }
                        "-x86" -> context.cpuArchitecture = CPUArch.x86
                        "-arm" -> context.cpuArchitecture = CPUArch.arm
                        "-start" -> context.implementStart = true
                        else -> exit("invalid option [$arg]\n$USAGE")
                    }
                else
                    context.inFile = arg
            }
            if (context.inFile == "")
                exit("missing argument input_file, $USAGE")
            if (context.inFile == context.outFile)
                exit("input and output files are identical ($context.inFile) - aborting\n$USAGE")
        }
    }
}