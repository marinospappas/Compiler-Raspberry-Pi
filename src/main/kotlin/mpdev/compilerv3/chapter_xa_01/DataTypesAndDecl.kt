package mpdev.compilerv3.chapter_xa_01

//TODO: int arrays
//TODO: short and byte types

// global vars

/** our variable types */
enum class DataType { int, string, void, none }

/** our variable scope */
enum class VarScope { global, local }

/** the declaration space (variables and functions) */
class IdentifierDecl(var fv: TokType, var type: DataType, var initialised: Boolean = false, var size: Int = 0,
                     var isStackVar: Boolean = false, var stackOffset: Int = 0, var canAssign: Boolean = true)

// the identifiers space map
val identifiersMap = mutableMapOf<String, IdentifierDecl>()

/** the function parameter class */
class FunctionParameter(var name: String, var type: DataType)

// the function parameters map - ley is the functionName
val funParamsMap = mutableMapOf<String,List<FunctionParameter>>()

// the local variables map - key is the blockName
val localVarsMap = mutableMapOf<String,MutableList<String>>()

// the string constants (will be included in the output file at the end of the compilation)
val stringConstants = mutableMapOf<String,String>()
var stringCnstIndx = 0
const val STRING_CONST_PREFIX = "STRCNST_"

// the buffer for string operations
const val STRING_BUFFER = "string_buffer_"
var STR_BUF_SIZE = 1024

/////////// support for variables and functions declarations /////////

var hasReturn: Boolean = false
var funName: String = ""

/** declare a variable */
fun declareVar(name: String, type: DataType, initValue: String, length: Int, scope: VarScope) {
    // check for duplicate var declaration
    if (identifiersMap[name] != null)
        abort ("line ${inp.currentLineNumber}: identifier $name already declared")
    when (scope) {
        VarScope.global -> declareGlobalVar(name, type, initValue, length)
        VarScope.local -> declareLocalVar(name, type, initValue, length)
    }
}

/** declare a global variable */
fun declareGlobalVar(name: String, type: DataType, initValue: String, length: Int) {
    identifiersMap[name] = IdentifierDecl(TokType.variable, type, initValue!="", length)
    when (type) {
        DataType.int -> code.declareInt(name, initValue)
        DataType.string -> code.declareString(name, initValue, length)
        else -> return
    }
}

/** declare a local variable */
fun declareLocalVar(name: String, type: DataType, initValue: String, length: Int) {
    val stackOffset: Int
    val lengthRoundedToWord = (length / code.INT_SIZE + 1) * code.INT_SIZE
    when (type) {
        DataType.int -> {
            stackOffset = code.allocateStackVar(code.INT_SIZE)
            initLocalIntVar(stackOffset, initValue)
        }
        DataType.string -> {
            stackOffset = code.allocateStackVar(code.STRPTR_SIZE)
            initLocalStringVar(name, stackOffset, initValue, lengthRoundedToWord)
        }
        else -> return
    }
    identifiersMap[name] = IdentifierDecl(
        TokType.variable, type, initialised = true, size = lengthRoundedToWord, isStackVar = true, stackOffset = stackOffset
    )
}

/** initialise a local int var */
fun initLocalIntVar(stackOffset: Int, initValue: String) {
    if (initValue.isEmpty())
        return
    code.initStackVarInt(stackOffset, initValue)
}

/** initialise a local string var */
fun initLocalStringVar(name: String, stackOffset: Int, initValue: String, length: Int) {
    if (initValue.isEmpty() && length == 0)
        abort ("line ${inp.currentLineNumber}: local variable $name is not initialised")
    var constStringAddress = ""
    // check for the constant string init value
    stringConstants.forEach { (k, v) -> if (v == initValue) constStringAddress = k }
    if (constStringAddress == "") {  // if not found
        // save the string in the map of constant strings
        constStringAddress = STRING_CONST_PREFIX + (++stringCnstIndx).toString()
        stringConstants[constStringAddress] = initValue
    }
    val stringDataOffset = code.allocateStackVar(length)
    code.initStackVarString(stackOffset, stringDataOffset, constStringAddress)
}

/** process a function declaration */
fun declareFun(name: String, type: DataType) {
    if (identifiersMap[name] != null)
        abort ("line ${inp.currentLineNumber}: identifier $name already declared")
    identifiersMap[name] = IdentifierDecl(TokType.function, type)
    code.declareAsmFun(name)
}

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
const val SIGNED = "signed"
const val BOOLEAN_NOT = "boolean not"
const val OR = "or"
const val XOR = "xor"
const val AND = "and"
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
    // all other combinations forbidden unless set here
)

/**
 * check for compatible data types for the specific operation
 * if the specific operation is not defined in the compatibility map
 * check also the specific types against the ALL_OPS keyword
 */
fun checkOperandTypeCompatibility(t1: DataType, t2: DataType, operation: String) {
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