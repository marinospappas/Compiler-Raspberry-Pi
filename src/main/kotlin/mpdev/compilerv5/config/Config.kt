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
import mpdev.compilerv5.parser.declarations.FunctionDeclParser
import mpdev.compilerv5.parser.declarations.VariablesDeclParser
import mpdev.compilerv5.parser.expressions.BooleanExpressionParser
import mpdev.compilerv5.parser.expressions.ExpressionParser
import mpdev.compilerv5.parser.function_calls.FunctionCallParser
import mpdev.compilerv5.parser.input_output.InputOutputParser
import mpdev.compilerv5.parser.labels.LabelHandler
import mpdev.compilerv5.parser.operations.NumericAssignementParser
import mpdev.compilerv5.parser.operations.OperationsParser
import mpdev.compilerv5.parser.operations.StringAssignmentParser
import mpdev.compilerv5.scanner.InputProgramScanner

class Config {

    companion object {

        var STR_BUF_SIZE = 1024

        lateinit var scanner: InputProgramScanner
        lateinit var codeModule: AsmInstructions
        lateinit var programParser: MainProgramParser
        lateinit var controlStructureParser: ControlStructureParser
        lateinit var variablesDeclParser: VariablesDeclParser
        lateinit var functionDeclParser: FunctionDeclParser
        lateinit var loopParser: LoopParser
        lateinit var forLoopParser: ForLoopParser
        lateinit var functionCallParser: FunctionCallParser
        lateinit var expressionParser: ExpressionParser
        lateinit var booleanExpressionParser: BooleanExpressionParser
        lateinit var operationsParser: OperationsParser
        lateinit var numericAssgnmtParser: NumericAssignementParser
        lateinit var stringAssgnmtParser: StringAssignmentParser
        lateinit var inputOutputParser: InputOutputParser
        lateinit var labelHandler: LabelHandler

        fun setCompilerModules(context: CompilerContext) {
            codeModule = when (context.cpuArchitecture) {
                CPUArch.x86 -> X86_64Instructions(context.outFile)
                CPUArch.arm -> Arm_32Instructions(context.outFile)
            }
            scanner = InputProgramScanner(context)
            programParser = MainProgramParser(context)
            controlStructureParser = ControlStructureParser(context)
            variablesDeclParser = VariablesDeclParser(context)
            functionDeclParser = FunctionDeclParser(context)
            loopParser = LoopParser(context)
            forLoopParser = ForLoopParser(context)
            functionCallParser = FunctionCallParser(context)
            expressionParser = ExpressionParser(context)
            booleanExpressionParser = BooleanExpressionParser(context)
            operationsParser = OperationsParser(context)
            numericAssgnmtParser = NumericAssignementParser(context)
            stringAssgnmtParser = StringAssignmentParser(context)
            inputOutputParser = InputOutputParser(context)
            labelHandler = LabelHandler()

            scanner.initialise()
            programParser.initialise()
            controlStructureParser.initialise()
            variablesDeclParser.initialise()
            functionDeclParser.initialise()
            loopParser.initialise()
            forLoopParser.initialise()
            functionCallParser.initialise()
            expressionParser.initialise()
            booleanExpressionParser.initialise()
            operationsParser.initialise()
            numericAssgnmtParser.initialise()
            stringAssgnmtParser.initialise()
            inputOutputParser.initialise()
            labelHandler.initialise()
        }
    }
}