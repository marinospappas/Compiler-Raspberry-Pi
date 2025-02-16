package mpdev.compilerv5.parser

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.config.Constants.Companion.MAIN_BLOCK
import mpdev.compilerv5.parser.declarations.FunctionDeclParser
import mpdev.compilerv5.parser.declarations.VariablesDeclParser
import mpdev.compilerv5.scanner.*

/**
 * Program parsing - module 0
 * Top Level - program structure
 */
class MainProgramParser(val context: CompilerContext) {

    private val scanner = Config.scanner
    private val code = Config.codeModule
    private val controlStructParser = Config.controlStructureParser
    private val labelHandler = Config.labelHandler

    /** program / library flag */
    private var isLibrary = false

    // the next level of parsers
    private val variablesDeclParser = VariablesDeclParser(context)
    private val functionsParser = FunctionDeclParser(context)

    /**
     * parse a program
     * <program> ::= <prog header> [ <var declarations> ] [ <fun declarations> ] <main block> <prog end>
     */
    fun parse() {
        parseProgHeader()
        if (scanner.lookahead().encToken == Kwd.varDecl)
            variablesDeclParser.parse()
        code.funInit()
        if (scanner.lookahead().encToken == Kwd.funDecl)
            functionsParser.parse()
        parseMainBlock()
        generatePostMainCode()
        parseProgEnd()
    }

    /**
     * parse program header
     * <program header> ::= program <identifier>
     */
    private fun parseProgHeader() {
        when (scanner.lookahead().encToken) {
            Kwd.startOfProgram -> isLibrary = false
            Kwd.startOfLibrary -> isLibrary = true
            else -> scanner.expected("program or library")
        }
        code.progInit(scanner.match().value, scanner.match(Kwd.identifier).value)
    }

    /**
     * parse main block
     * <main block> ::= main <block>
     */
    private fun parseMainBlock() {
        if (isLibrary) return   // no main block for libraries
        labelHandler.labelPrefix = MAIN_BLOCK        // set label prefix and label index for main block
        labelHandler.labelIndx = 0
        scanner.match(Kwd.mainToken)
        code.mainInit()
        controlStructParser.parseBlock()
        code.mainEnd()
    }

    /**
     * parse program end
     * <program end> ::= endprogram
     */
    private fun parseProgEnd() {
        val endModule: Token
        if (isLibrary)
            endModule = scanner.match(Kwd.endOfLibrary)
        else
            endModule = scanner.match(Kwd.endOfProgram)
        code.progEnd(endModule.value)
        scanner.match(Kwd.endOfInput)
    }

    /** add any string constants at the end of the assembler output */
    private fun generatePostMainCode() {
        code.createRelativeAddresses()
        code.stringConstantsDataSpace()
        if (stringConstants.isEmpty())
            return
        code.outputCommentNl("constant string values go here")
        for (s in stringConstants.keys) {
            stringConstants[s]?.let {
                if (it.isNotEmpty() && it[0] >= ' ')
                    code.declareString(s, it, 0)
                else
                    code.declareString(s, "", it.length)
            }
        }
    }
}
