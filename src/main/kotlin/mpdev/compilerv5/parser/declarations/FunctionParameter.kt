package mpdev.compilerv5.parser.declarations

import mpdev.compilerv5.scanner.DataType

data class FunctionParameter(var name: String, var type: DataType)