package mpdev.compilerv5.scanner

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

// definitions of operations
enum class Operation {
    ALL_OPS, ASSIGN, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, SIGNED,
    SHIFT_LEFT, SHIFT_RIGHT, NOT, OR, XOR, AND,
    BOOL_NOT, BOOL_OR, BOOL_AND,
    COMPARE_EQ, COMPARE_NE, COMPARE_GT, COMPARE_GE, COMPARE_LT, COMPARE_LE,
    PRINT
}

// type compatibility between different types for each operation
class TypesAndOpsCombi(var type1: DataType, var type2: DataType, var operation: Operation) {
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

