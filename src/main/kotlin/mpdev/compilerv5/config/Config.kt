package mpdev.compilerv5.config

import mpdev.compilerv5.CPUArch
import mpdev.compilerv5.code_module.Arm_32Instructions
import mpdev.compilerv5.code_module.AsmInstructions
import mpdev.compilerv5.code_module.X86_64Instructions
import mpdev.compilerv5.debugger.CompilerDebugger
import mpdev.compilerv5.parser.MainProgramParser
import mpdev.compilerv5.parser.control_structures.ControlStructureParser
import mpdev.compilerv5.parser.control_structures.ForLoopParser
import mpdev.compilerv5.parser.control_structures.LoopParser
import mpdev.compilerv5.parser.declarations.FunctionParser
import mpdev.compilerv5.parser.declarations.VariablesParser
import mpdev.compilerv5.parser.labels.LabelHandler
import mpdev.compilerv5.scanner.InputProgramScanner

class Config {

    companion object {

        var STR_BUF_SIZE = 1024

        lateinit var scanner: InputProgramScanner
        lateinit var codeModule: AsmInstructions
        lateinit var debugger: CompilerDebugger
        lateinit var programParser: MainProgramParser
        lateinit var controlStructureParser: ControlStructureParser
        lateinit var variablesParser: VariablesParser
        lateinit var functionParser: FunctionParser
        lateinit var loopParser: LoopParser
        lateinit var forLoopParser: ForLoopParser
        lateinit var labelHandler: LabelHandler

        fun setCompilerModules(context: mpdev.compilerv5.config.CompilerContext) {
            scanner = InputProgramScanner(context)
            codeModule = when (context.cpuArchitecture) {
                CPUArch.x86 -> X86_64Instructions(context.outFile)
                CPUArch.arm -> Arm_32Instructions(context.outFile)
            }
            debugger = CompilerDebugger(context)
            programParser = MainProgramParser(context)
            controlStructureParser = ControlStructureParser(context)
            variablesParser = VariablesParser(context)
            functionParser = FunctionParser(context)
            loopParser = LoopParser(context)
            forLoopParser = ForLoopParser(context)
            labelHandler = LabelHandler()
        }
    }
}