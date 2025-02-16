package mpdev.compilerv5.parser.input_output

import mpdev.compilerv5.code_module.AsmInstructions
import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.parser.expressions.BooleanExpressionParser
import mpdev.compilerv5.scanner.*
import mpdev.compilerv5.util.Utils.Companion.abort

/**
 * parse read statement
 * <read> :: = read <identifier> [ , <identifier> ] *
 */
class InputOutputParser(val context: CompilerContext) {

    private lateinit var scanner: InputProgramScanner
    private lateinit var code: AsmInstructions
    private lateinit var booleanExprParser: BooleanExpressionParser

    fun initialise() {
        scanner = Config.scanner
        code = Config.codeModule
        booleanExprParser = Config.booleanExpressionParser
    }

    fun parseRead() {
        var varToken: Token
        do {
            scanner.match()
            varToken = scanner.match(Kwd.identifier)
            if (varToken.type == TokType.none)
                abort("line ${scanner.currentLineNumber}: identifier ${varToken.value} not declared")
            if (varToken.type != TokType.variable)
                abort("line ${scanner.currentLineNumber}: identifier ${varToken.value} is not a variable")
            val identName = varToken.value
            val strLen = identifiersMap[identName]?.size!!
            when (getType(identName)) {
                DataType.int -> parseReadInt(identName)
                DataType.string -> parseReadString(identName, strLen)
                else -> {}
            }
        } while (scanner.lookahead().encToken == Kwd.commaToken)
    }

    /** parse a read int instruction */
    private fun parseReadInt(identName: String) {
        if (identifiersMap[identName]?.isStackVar!!) {
            code.readIntLocal(identifiersMap[identName]?.stackOffset!!)
            code.assignmentLocalVar(identifiersMap[identName]?.stackOffset!!)
        } else {
            code.readInt(identName)
            code.assignment(identName)
        }
    }

    /** parse a read string instruction */
    private fun parseReadString(identName: String, strLen: Int) {
        if (identifiersMap[identName]?.isStackVar!!)
            code.readStringLocal(identifiersMap[identName]?.stackOffset!!, strLen)
        else
            code.readString(identName, strLen)
    }

    /**
     * parse print statement
     * <print> ::= print <b-expression> [ , <b-expression> ] *
     */
    fun parsePrint() {
        scanner.match()
        printExpressions()
    }

    fun parsePrintLn() {
        scanner.match()
        if (scanner.lookahead().encToken != Kwd.semiColonToken)
            printExpressions()
        code.printNewline()
    }

    private fun printExpressions() {
        do {
            var decFmt = code.DEF_INT_FMT
            if (scanner.lookahead().encToken == Kwd.commaToken)
                scanner.match() // skip the comma
            val exprType = booleanExprParser.parse()
            checkOperandTypeCompatibility(exprType, DataType.none, PRINT)
            if (scanner.lookahead().encToken == Kwd.colonToken) {
                scanner.match()
                decFmt = getPrintFormat()
            }
            when (exprType) {
                DataType.int, DataType.memptr, DataType.byte -> code.printInt(decFmt)
                DataType.string -> code.printStr()
                else -> {}
            }
        } while (scanner.lookahead().encToken == Kwd.commaToken)
    }

    fun getPrintFormat(): String {
        var fmt = ""
        var fmtLen = ""
        var fmtType = ""
        if (scanner.lookahead().encToken == Kwd.number) {
            fmtLen = scanner.lookahead().value
            scanner.match()
        }
        if (scanner.lookahead().encToken == Kwd.identifier) {
            fmtType = scanner.lookahead().value
            scanner.match()
        }
        fmt = "%$fmtLen$fmtType"
        // fmt must be added to the map of constant strings
        var fmtStringName = ""
        stringConstants.forEach { (k, v) -> if (v == fmt) fmtStringName = k }
        if (fmtStringName == "") {  // if not found
            // save the string in the map of constant strings
            fmtStringName = "${code.INT_FMT}_${fmt.substring(1)}"
            stringConstants[fmtStringName] = fmt
        }
        return fmtStringName
    }
}