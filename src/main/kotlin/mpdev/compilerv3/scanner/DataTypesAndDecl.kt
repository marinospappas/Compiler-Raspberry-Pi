package mpdev.compilerv3.scanner

import mpdev.compilerv3.util.Utils.Companion.abort

//TODO: implement sizeof

/** our variable types */
enum class DataType { int, string, memptr, intarray, byte, bytearray, void, none }

/** our variable scope
 *  packageGlobal: scope across program and all libraries in the package
 *  global: scope within the program only
 *  local: scope within the block only
 */
enum class VarScope { packageGlobal, global, local, external }

/** the declaration space (variables and functions) */
class IdentifierDecl(var fv: TokType, var type: DataType, var initialised: Boolean = false, var size: Int = 0,
                     var isStackVar: Boolean = false, var stackOffset: Int = 0, var canAssign: Boolean = true)

// the identifiers space map
val identifiersMap = mutableMapOf<String, IdentifierDecl>()

/** the function parameter class */
class FunctionParameter(var name: String, var type: DataType)

// the function parameters map - key is the functionName
val funParamsMap = mutableMapOf<String, List<FunctionParameter>>()

// the local variables map - key is the blockName
val localVarsMap = mutableMapOf<String, MutableList<String>>()

// the string constants (will be included in the output file at the end of the compilation)
val stringConstants = mutableMapOf<String, String>()
var stringCnstIndx = 0
const val STRING_CONST_PREFIX = "STRCNST_"

// the buffer for string operations
const val STRING_BUFFER = "string_buffer_"
var STR_BUF_SIZE = 1024

/** return the type of var/fun */
fun getType(identifier: String): DataType = identifiersMap[identifier]?.type?: DataType.none

/** return the canAssign flag */
fun getCanAssign(identifier: String): Boolean = identifiersMap[identifier]?.canAssign?:false

// type compatibility between different types for each operation

class TypesAndOpsCombi(var type1: DataType, var type2: DataType, var operation: String) {
    override fun toString(): String {
        return type1.toString() + type2.toString() + operation
    }
    override fun equals(other: Any?): Boolean {
        if (other is TypesAndOpsCombi)
            return other.type1 == this.type1 && other.type2 == this.type2 && other.operation == this.operation
        else
            return false
    }
    override fun hashCode() = this.toString().hashCode()
}

// definitions of operations
const val ALL_OPS = "all ops"
const val ASSIGN = "assign"
const val ADD = "add"
const val SUBTRACT = "subtract"
const val MULTIPLY = "multiply"
const val DIVIDE = "divide"
const val MODULO = "modulo"
const val SIGNED = "signed"
const val SHIFT_LEFT = "shift left"
const val SHIFT_RIGHT = "shift right"
const val NOT = "not"
const val OR = "or"
const val XOR = "xor"
const val AND = "and"
const val BOOL_NOT = "boolean not"
const val BOOL_OR = "bool or"
const val BOOL_AND = "and"
const val COMPARE_EQ = "compare eq"
const val COMPARE_NE = "compare ne"
const val COMPARE_GT = "compare gt"
const val COMPARE_GE = "compare ge"
const val COMPARE_LT = "compare lt"
const val COMPARE_LE = "compare le"
const val PRINT = "print"

val typesCompatibility = mapOf(
    // int with int allowed for all operations
    TypesAndOpsCombi(DataType.int, DataType.int, ALL_OPS) to true,
    TypesAndOpsCombi(DataType.int, DataType.none, ALL_OPS) to true,
    // string with string allowed only for assign, add, compare_eq compare_ne
    TypesAndOpsCombi(DataType.string, DataType.string, ASSIGN) to true,
    TypesAndOpsCombi(DataType.string, DataType.string, ADD) to true,
    TypesAndOpsCombi(DataType.string, DataType.string, COMPARE_EQ) to true,
    TypesAndOpsCombi(DataType.string, DataType.string, COMPARE_NE) to true,
    TypesAndOpsCombi(DataType.string, DataType.none, PRINT) to true,
    // pointer with int allowed only for assign add, subtract and comparisons
    TypesAndOpsCombi(DataType.memptr, DataType.int, ADD) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, SUBTRACT) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, ASSIGN) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.string, ASSIGN) to true,
    TypesAndOpsCombi(DataType.int, DataType.memptr, ASSIGN) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, COMPARE_EQ) to true,
    TypesAndOpsCombi(DataType.int, DataType.memptr, COMPARE_EQ) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, COMPARE_NE) to true,
    TypesAndOpsCombi(DataType.int, DataType.memptr, COMPARE_NE) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, COMPARE_LT) to true,
    TypesAndOpsCombi(DataType.int, DataType.memptr, COMPARE_LT) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, COMPARE_GT) to true,
    TypesAndOpsCombi(DataType.int, DataType.memptr, COMPARE_GT) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, COMPARE_LE) to true,
    TypesAndOpsCombi(DataType.int, DataType.memptr, COMPARE_LE) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.int, COMPARE_GE) to true,
    TypesAndOpsCombi(DataType.int, DataType.memptr, COMPARE_GE) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.none, PRINT) to true,
    // pointer with pointer allowed only for subtract, assign and compare
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, SUBTRACT) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, ASSIGN) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, COMPARE_EQ) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, COMPARE_NE) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, COMPARE_LT) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, COMPARE_GT) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, COMPARE_LE) to true,
    TypesAndOpsCombi(DataType.memptr, DataType.memptr, COMPARE_GE) to true,
    // all other combinations forbidden unless set here
)

/**
 * check for compatible data types for the specific operation
 * if the specific operation is not defined in the compatibility map
 * check also the specific types against the ALL_OPS keyword
 */
fun checkOperandTypeCompatibility(type1: DataType, type2: DataType, operation: String) {
    val t1 = if (setOf(DataType.byte, DataType.bytearray, DataType.intarray).contains(type1))
        DataType.int
    else
        type1
    val t2 = if (setOf(DataType.byte, DataType.bytearray, DataType.intarray).contains(type2))
        DataType.int
    else
        type2
    var typesAreCompatible = typesCompatibility[TypesAndOpsCombi(t1, t2, operation)] ?: false
    if (!typesAreCompatible)
        typesAreCompatible = typesCompatibility[TypesAndOpsCombi(t1, t2, ALL_OPS)] ?: false
    if (!typesAreCompatible) {
        var message = "line ${inp.currentLineNumber}: $operation $t1 "
        if (t2 != DataType.none)
            message += "with $t2 "
        message += "not supported"
        abort(message)
    }
}