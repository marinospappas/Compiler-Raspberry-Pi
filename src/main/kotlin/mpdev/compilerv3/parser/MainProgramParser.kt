package mpdev.compilerv3.parser

import mpdev.compilerv3.CompilerContext
import mpdev.compilerv3.parser.control_structures.labelIndx
import mpdev.compilerv3.parser.control_structures.labelPrefix
import mpdev.compilerv3.parser.control_structures.parseBlock
import mpdev.compilerv3.parser.declarations.FunctionParser
import mpdev.compilerv3.parser.declarations.VariablesParser
import mpdev.compilerv3.scanner.*

/**
 * Program parsing - module 0
 * Top Level - program structure
 */
class MainProgramParser(context: CompilerContext) {

    val scanner = context.scanner
    val code = context.codeModule

    /** program / library flag */
    private var isLibrary = false

    // the next level of parsers
    private val variablesParser = VariablesParser(context)
    private val functionsParser = FunctionParser(context)

    /**
     * parse a program
     * <program> ::= <prog header> [ <var declarations> ] [ <fun declarations> ] <main block> <prog end>
     */
    fun parse() {
        parseProgHeader()
        if (scanner.lookahead().encToken == Kwd.varDecl)
            variablesParser.parse()
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
        labelPrefix = MAIN_BLOCK        // set label prefix and label index
        labelIndx = 0
        scanner.match(Kwd.mainToken)
        code.mainInit()
        parseBlock()
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
