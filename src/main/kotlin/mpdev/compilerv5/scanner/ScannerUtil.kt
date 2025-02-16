package mpdev.compilerv5.scanner

import mpdev.compilerv5.config.CompilerContext
import mpdev.compilerv5.config.Config
import mpdev.compilerv5.scanner.Operation.*
import mpdev.compilerv5.util.Utils.Companion.abort

class ScannerUtil(val context: CompilerContext) {

    /** return the type of var/fun */
    fun getType(identifier: String): DataType = context.identifiersMap[identifier]?.type?: DataType.none

    /** return the canAssign flag */
    fun getCanAssign(identifier: String): Boolean = context.identifiersMap[identifier]?.canAssign?:false

    /**
     * check for compatible data types for the specific operation
     * if the specific operation is not defined in the compatibility map
     * check also the specific types against the ALL_OPS keyword
     */
    fun checkOperandTypeCompatibility(type1: DataType, type2: DataType, operation: Operation) {
        val scanner = Config.scanner
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
            var message = "line ${scanner.currentLineNumber}: $operation $t1 "
            if (t2 != DataType.none)
                message += "with $t2 "
            message += "not supported"
            abort(message)
        }
    }

    companion object {
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
    }
}