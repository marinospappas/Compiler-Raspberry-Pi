package mpdev.compilerv5.parser.operations

import mpdev.compilerv5.config.Config

class OpsStrings {

    private val code = Config.codeModule

    /** add strings */
    fun add() {
        code.addString()
    }
}