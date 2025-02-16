package mpdev.compilerv5.config

class Constants {

    companion object {
        // the buffer for string operations
        const val STRING_BUFFER = "string_buffer_"

        // string constants section in output file
        const val STRING_CONST_PREFIX = "STRCNST_"

        ///////////////// Tokens

        // the null char is used as end of input mark
        val NULL_CHAR = 0

        val NO_TOKEN = "No Token"
        val END_OF_INPUT = "End of Input"

        // boolean literals
        val BOOLEAN_TRUE = "true"
        val BOOLEAN_FALSE = "false"

        // main function name
        val MAIN_BLOCK = "main"
    }
}