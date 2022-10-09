package mpdev.compilerv3.chapter_xa_02

import java.io.File
import java.io.PrintStream
import java.lang.System.err
import java.lang.System.out
import java.util.Date
import javax.print.attribute.IntegerSyntax

/** this class implements all the instructions for the target machine */
class Arm_32Instructions(outFile: String = ""): CodeModule {

    private val CODE_ID = "Arm-32 Assembly Code - Raspberry Pi"
    override val COMMENT = "@"
    override var outputLines: Int = 0
    override var outStream: PrintStream = out

    private val MAIN_ENTRYPOINT = "main"
    private val MAIN_EXITPOINT = "${MAIN_BLOCK}_exit_"

    // the offset from frame pointer for the next local variable (in the stack)
    override var stackVarOffset = -4

    // architecture word size
    val WORD_SIZE = 4  // 32-bit architecture

    // sizes of various types
    override val INT_SIZE = WORD_SIZE   // 32-bit integers
    override val PTR_SIZE = WORD_SIZE   // pointer 32 bit

    // global vars list - need for entering the global var addresses in the .text section
    private val globalVarsList = mutableListOf<String>()
    private val GLOBAL_VARS_ADDR_SUFFIX = "_addr"

    // various string constants
    val TINSEL_MSG = "tinsel_msg"
    val NEWLINE = "newline"
    val INT_FMT = "int_fmt"

    // need a map of int constants due to limitation in loading const value to register
    val intConstants = mutableMapOf<String,String>()
    val INT_CONST_NAME = "INTCONST_"

    /** initialisation code - class InputProgramScanner */
    init {
        if (outFile != "") {
            try {
                outStream = PrintStream(File(outFile))
            } catch (e: Exception) {
                err.println("could not create output file - $e")
                err.println("output code will be sent to stdout")
            }
        }
    }

    /** register names for the function params - in order 1-4 (arm architecture specific) */
    // these registers hold the fun params at the time of the call
    // parameters 5 and 6 go to the stack and the offset from fp is noted here
    override val funInpParamsCpuRegisters = arrayOf("r0", "r1", "r2", "r3", "r4:stack:4", "r5:stack:8")
    // during the assignment of the parameters, their values are saved temporarily here,
    // so that they are not corrupted by function calls executed during the assignment of the parameters
    override val funTempParamsCpuRegisters = arrayOf("r6", "r7", "r8", "r9", "r10", "r11")
    // 6 params maximum allowed
    override val MAX_FUN_PARAMS = funInpParamsCpuRegisters.size

    override fun outputComment(s: String) = outputCode("$COMMENT $s")

    /////////////////////////// initialisation and termination //////////////////////////////7

    /** initialisation code for assembler */
    override fun progInit(progOrLib: String, progName: String) {
        outputCommentNl(CODE_ID)
        outputCommentNl("$progOrLib $progName")
        outputCommentNl("compiled on ${Date()}")
        outputCodeNl("")
        outputCommentNl("define the Raspberry Pi CPU")
        outputCodeNl(".cpu\tcortex-a53")
        outputCodeNl(".fpu\tneon-fp-armv8")
        outputCodeNl(".syntax\tunified")
        outputCodeNl("")
        outputCodeNl(".data")
        outputCodeNl(".align 4")
        // copyright message
        outputCodeTabNl("$TINSEL_MSG: .asciz \"TINSEL version 3.0 for Arm-32 (Raspberry Pi) July 2022 (c) M.Pappas\\n\"")
        // newline string
        outputCodeTabNl("$NEWLINE: .asciz \"\\n\"")
        // int format for printf
        outputCodeTabNl("$INT_FMT: .asciz \"%d\"")
        outputCodeNl(".align 4")
    }

    /** declare int variable (32bit) */
    override fun declareInt(varName: String, initValue: String) {
        if (initValue == "")
            outputCodeTabNl("$varName:\t.word 0")       // uninitialised global int vars default to 0
        else
            outputCodeTabNl("$varName:\t.word $initValue")
        globalVarsList.add(varName)      // add var to the list
    }

    /** initial code for functions */
    override fun funInit() {
        outputCodeNl()
        outputCodeNl(".text")
        outputCodeNl(".align 4")
    }

    /** declare function */
    override fun declareAsmFun(name: String) {
        outputCodeNl(".type $name %function")
        outputCommentNl("function $name")
        outputLabel(name)
        outputCodeTab("stmdb\tsp!, {fp, lr}\t\t")
        outputCommentNl("save registers")
        newStackFrame()
    }

    /** check whether a function parameter is in stack
     *  returns the frame offset if yes otherwise -1
     */
    override fun isFunParamInStack(paramIndx: Int): Int {
        val reg = funInpParamsCpuRegisters[paramIndx]
        val regParts = reg.split(":")
        if (regParts.size == 1) // parameter is in register
            return -1
        else
            return regParts[2].toInt()
    }

    /** transfer a function parameter to stack variable */
    override fun storeFunParamToStack(paramIndx: Int, stackOffset: Int) {
        if (isFunParamInStack(paramIndx) < 0) // parameter is in register
            outputCodeTabNl("str\t${funInpParamsCpuRegisters[paramIndx]}, [fp, #$stackOffset]")
    }

    /** end of function - tidy up stack */
    private fun funEnd() {
        outputCodeTabNl("mov\tr0, r3")      // return value in r0
        outputCodeTab("sub\tsp, fp, #4\t\t")
        outputCommentNl("restore stack pointer")
        outputCodeTab("ldmia\tsp!, {fp, pc}\t\t")
        outputCommentNl("restore registers - lr goes into pc to return to caller")
    }

    /** set a temporary function param register to the value of r3 (the result of the last expression) */
    override fun setIntTempFunParam(paramIndx: Int) {
        outputCodeTabNl("str\t${funTempParamsCpuRegisters[paramIndx]}, [sp, #-4]!")
        outputCodeTabNl("mov\t${funTempParamsCpuRegisters[paramIndx]}, r3")
    }

    /** set a function input param register from the temporary register */
    override fun setFunParamReg(paramIndx: Int) {
        if (isFunParamInStack(paramIndx) < 0)     // parameter goes in register
            outputCodeTabNl("mov\t${funInpParamsCpuRegisters[paramIndx]}, ${funTempParamsCpuRegisters[paramIndx]}")
        else                            // parameter goes in the stack
            outputCodeTabNl("str\t${funTempParamsCpuRegisters[paramIndx]}, [sp, #-4]!")
    }

    /** restore a function temporary param register */
    override fun restoreFunTempParamReg(paramIndx: Int) {
        if (isFunParamInStack(paramIndx) > 0) // parameter is in stack
            outputCodeTabNl("ldr\t${funTempParamsCpuRegisters[paramIndx]}, [sp], #4")
    }

    /** restore the stack space used a function stack param */
    override fun restoreFunStackParam(paramIndx: Int) {
        if (isFunParamInStack(paramIndx) > 0) // parameter is in stack
            outputCodeTabNl("add\tsp, sp,#4")
    }

    override fun globalSymbol(name: String) {
        outputCodeNl(".global $name")
    }

    /** initial code for main */
    override fun mainInit() {
        outputCodeNl()
        globalSymbol(MAIN_ENTRYPOINT)
        outputCodeNl(".type $MAIN_ENTRYPOINT %function")
        outputCommentNl("main program")
        outputLabel(MAIN_ENTRYPOINT)
        outputCodeTab("stmdb\tsp!, {fp, lr}\t\t")
        outputCommentNl("save registers")
        newStackFrame()
        outputCommentNl("print hello message")
        outputCodeTabNl("ldr\tr0, ${TINSEL_MSG}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("bl\tprintf")
        outputCodeNl()
    }

    /** termination code for assembler */
    override fun mainEnd() {
        outputCodeNl()
        outputCommentNl("end of main")
        outputLabel(MAIN_EXITPOINT)
        outputCodeTab("mov\tr0, #0\t\t")
        outputCommentNl("exit code 0")
        outputCodeTab("sub\tsp, fp, #4\t\t")
        outputCommentNl("restore stack pointer")
        outputCodeTabNl("ldmia\tsp!, {fp, lr}")
        outputCodeTabNl("bx\tlr")
    }

    /** create relative addresses for global vars */
    override fun createRelativeAddresses() {
        setGlobalVarAddresses()
        setIntConstants()
    }

    /** set the addresses of the global vars in the .text section */
    private fun setGlobalVarAddresses() {
        //TODO: this has to be done after each function for those references used in that function
        val globalVarNamesList = globalVarsList + stringConstants.keys +
                listOf(TINSEL_MSG, NEWLINE, INT_FMT, STRING_BUFFER)
        outputCodeNl("")
        outputCodeNl(".align 4")
        outputCommentNl("global var addresses go here")
        globalVarNamesList.forEach{ varname ->
            outputCodeNl("${varname}${GLOBAL_VARS_ADDR_SUFFIX}:\t.word $varname")
        }
    }

    /** set the values of the int constants */
    private fun setIntConstants() {
        //TODO: same as with global variables, this has to be done after the end of each function
        intConstants.forEach{ constName, value ->
            outputCodeNl("${constName}:\t.word $value")
        }
    }

    /** set new stack frame */
    private fun newStackFrame() {
        outputCodeTab("add\tfp, sp, #4\t\t")
        outputCommentNl("new stack frame")
        stackVarOffset = -4  // reset the offset for stack vars in this new frame
    }

    /**
     * allocate variable space in the stack
     * returns the new stack offset for this new variable
     */
    override fun allocateStackVar(size: Int): Int {
        outputCodeTabNl("sub\tsp, sp, #${size}")
        stackVarOffset -= size
        return stackVarOffset
    }

    /** release variable space in the stack */
    override fun releaseStackVar(size: Int) {
        outputCodeTabNl("add\tsp, sp, $${size}")
        stackVarOffset += size
    }

    /** initialise an int stack var */
    override fun initStackVarInt(stackOffset : Int, initValue: String) {
        val value: Int
        if (initValue.startsWith("0x"))
            value = initValue.substring(2).toInt(16)
        else if (initValue.startsWith("0b"))
            value = initValue.substring(2).toInt(2)
        else
            value = initValue.toInt()
        if (value in 0..255)
            outputCodeTabNl("mov\tr3, #${initValue}")
        else {
            val intConstantAddr = createIntConst(initValue)
            outputCodeTabNl("ldr\tr3, ${intConstantAddr}")
        }
        outputCodeTabNl("str\tr3, [fp, #${stackOffset}]")
    }

    /** exit the program */
    override fun exitProgram() {
        jump(MAIN_EXITPOINT)
    }

    /** end of program */
    override fun progEnd(libOrProg: String) {
        outputCodeNl()
        outputCommentNl("end $libOrProg")
    }

    /////////////////////////// integer assignments and arithmetic //////////////////////////////7

    /** set accumulator to a value */
    override fun setAccumulator(value: String) {
        val intValue: Int
        if (value.startsWith("0x"))
            intValue = value.substring(2).toInt(16)
        else if (value.startsWith("0b"))
            intValue = value.substring(2).toInt(2)
        else
            intValue = value.toInt()
        if (intValue in 0..255)
            outputCodeTabNl("movs\tr3, #${value}")
        else {
            val intConstantAddr = createIntConst(value)
            outputCodeTabNl("ldr\tr3, ${intConstantAddr}")
            outputCodeTabNl("tst\tr3, r3")    // also set flags - Z flag set = FALSE
        }
    }

    private fun createIntConst(value: String): String {
        val key = INT_CONST_NAME +
                if (value.startsWith("-"))
                    "_" + value.substring(1)
                else
                    value
        if (intConstants[key] == null)
            intConstants[key] = value
        return key
    }

    /** clear accumulator */
    override fun clearAccumulator() = outputCodeTabNl("eors\tr3, r3, r3")

    /** increment accumulator */
    override fun incAccumulator() = outputCodeTabNl("adds\tr3, r3, #1")

    /** decrement accumulator */
    override fun decAccumulator() = outputCodeTabNl("subs\tr3, r3, #1")

    /** push accumulator to the stack */
    override fun saveAccumulator() = outputCodeTabNl("str\tr3, [sp, #-4]!")

    /** add top of stack to accumulator */
    override fun addToAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("adds\tr3, r3, r2")
    }

    /** subtract top of stack from accumulator */
    override fun subFromAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("subs\tr3, r2, r3")
    }

    /** negate accumulator */
    override fun negateAccumulator() = outputCodeTabNl("rsb\tr3, r3, #0")

    /** multiply accumulator by top of stack */
    override fun multiplyAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("muls\tr3, r3, r2")
    }

    /** divide accumulator by top of stack */
    override fun divideAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("sdiv\tr3, r2, r3")
        outputCodeTabNl("tst\tr3, r3")    // also set flags - Z flag set = FALSE
    }

    override fun moduloAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("sdiv\tr0, r2, r3")
        outputCodeTabNl("mul\tr1, r0, r3")
        outputCodeTabNl("sub\tr3, r2, r1")
        outputCodeTabNl("tst\tr3, r3")    // also set flags - Z flag set = FALSE
    }

    override fun notAccumulator() {
        TODO("Not yet implemented")
    }

    /** or top of stack with accumulator */
    override fun orAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("orrs\tr3, r2, r3")
    }

    /** exclusive or top of stack with accumulator */
    override fun xorAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("eors\tr3, r2, r3")
    }

    /** and top of stack with accumulator */
    override fun andAccumulator() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("ands\tr3, r2, r3")
    }

    override fun shiftAccumulatorLeft() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("lsl\tr3, r2, r3")
        outputCodeTabNl("tst\tr3, r3")
   }

    override fun shiftAccumulatorRight() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("lsr\tr3, r2, r3")
        outputCodeTabNl("tst\tr3, r3")    }

    /** set accumulator to global variable */
    override fun setAccumulatorToVar(identifier: String) {
        outputCodeTabNl("ldr\tr2, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("ldr\tr3, [r2]")
        outputCodeTabNl("tst\tr3, r3")    // also set flags - Z flag set = FALSE
    }

    /** set accumulator to global variable address */
    override fun setAccumulatorToVarAddress(identifier: String) {
        outputCodeTabNl("ldr\tr3, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
    }

    /** save address a pointer is pointing to for later use */
    override fun savePtrValue() {
        // the address the pointer points to is saved in r1
        outputCodeTabNl("mov\tr1, r3")
    }

    /** save accumulator to the previously saved address the pointer is pointing to */
    override fun pointerAssignment() {
        // the pointer address was previously saved in r1
        outputCodeTabNl("str\tr3, [r1]")
    }

    /** set accumulator to the contents of the address already in accumulator */
    override fun setAccumulatorToPointerVar() {
        outputCodeTabNl("ldr\tr3, [r1]")    }

    /** set accumulator to local variable */
    override fun setAccumulatorToLocalVar(offset: Int) {
        outputCodeTabNl("ldr\tr3, [fp, #${offset}]")
        outputCodeTabNl("tst\tr3, r3")    // also set flags - Z flag set = FALSE
    }

    /** set accumulator to local variable  address */
    override fun setAccumulatorToLocalVarAddress(offset: Int) {
        outputCodeTabNl("add\tr3, fp, #${offset}")
    }

    /** set variable to accumulator */
    override fun assignment(identifier: String) {
        outputCodeTabNl("ldr\tr2, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("str\tr3, [r2]")
    }

    /** set stack variable to accumulator */
    override fun assignmentLocalVar(offset: Int) {
        outputCodeTabNl("str\tr3, [fp, #${offset}]")
    }

    //////////////////////////////////// function calls ///////////////////////////////////

    /** call a function */
    override fun callFunction(subroutine: String) {
        outputCodeTabNl("bl\t${subroutine}")
        // function return to accumulator (r3)
        outputCodeTabNl("mov\tr3, r0")
    }

    /** return from function */
    override fun returnFromCall() {
        funEnd()
    }

    //////////////////////////////////// branch ///////////////////////////////////

    /** branch if false */
    override fun jumpIfFalse(label: String) = outputCodeTabNl("beq\t$label")    // Z flag set = FALSE

    /** branch */
    override fun jump(label: String) = outputCodeTabNl("b\t$label")

    //////////////////////////////////// boolean arithmetic ///////////////////////////////////

    /** boolean not accumulator */
    override fun booleanNotAccumulator() {
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("moveq\tr3, #1")     // set r3 to 1 if zero flag is set
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    override fun booleanOrAccumulator() {
        TODO("Not yet implemented")
    }

    override fun booleanAndAccumulator() {
        TODO("Not yet implemented")
    }

    //////////////////////////////////// comparisons ///////////////////////////////////

    /** compare and set accumulator and flags - is equal to */
    override fun compareEquals() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("cmp\tr2, r3")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("moveq\tr3, #1")     // set r3 to 1 if comparison is ==
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is not equal to */
    override fun compareNotEquals() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("cmp\tr2, r3")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("movne\tr3, #1")      // set r3 to 1 if comparison is !=
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is less than */
    override fun compareLess() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("cmp\tr2, r3")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("movlt\tr3, #1")        // set r3 to 1 if comparison is r2 < r3
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is less than */
    override fun compareLessEqual() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("cmp\tr2, r3")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("movle\tr3, #1")        // set r3 to 1 if comparison is <=
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is greater than */
    override fun compareGreater() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("cmp\tr2, r3")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("movgt\tr3, #1")        // set r3 to 1 if comparison is >
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is greater than */
    override fun compareGreaterEqual() {
        outputCodeTabNl("ldr\tr2, [sp], #4")
        outputCodeTabNl("cmp\tr2, r3")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("movge\tr3, #1")        // set r3 to 1 if comparison is >=
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    //////////////////////////////////// read and print integer ///////////////////////////////////

    /** print a newline */
    override fun printNewline() {
        outputCodeTabNl("ldr\tr0, ${NEWLINE}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("bl\tprintf")
    }

    /** print accumulator as integer */
    override fun printInt() {
        outputCodeTabNl("ldr\tr0, ${INT_FMT}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTab("mov\tr1, r3\t\t")
        outputCommentNl("integer to be printed in r1")
        outputCodeTabNl("bl\tprintf")
        outputCodeTabNl("mov\tr0, #0")
        outputCodeTabNl("bl\tfflush")
    }

    /** read global int var into variable */
    override fun readInt(identifier: String) {
        readStringAndConvertToInt()
    }

    private fun readStringAndConvertToInt() {
        outputCodeTab("mov\tr0, #0\t\t")
        outputCommentNl("read string")
        outputCodeTabNl("ldr\tr1, ${STRING_BUFFER}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("mov\tr2, #${STR_BUF_SIZE}\t\t")
        outputCodeTabNl("bl\tread")
        outputCodeTabNl("ldr\tr0, ${STRING_BUFFER}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTab("bl\tatoi\t\t")
        outputCommentNl("convert to int")
        outputCodeTabNl("movs\tr3, r0")     // also sets flags - Z flag set = FALSE
    }

    /** read local int var into variable */
    override fun readIntLocal(stackOffset: Int) {
        readStringAndConvertToInt()
    }

    ///////////////////////////// string operations ///////////////////////

    /** declare string global variable */
    override fun declareString(varName: String, initValue: String, length: Int) {
        if (length == 0 || initValue != "")
            outputCodeTabNl("$varName:\t.asciz \"$initValue\"")
        else
            outputCodeTabNl("$varName:\t.space $length") // uninitialised string vars must have length
        globalVarsList.add(varName)      // add var to the list
    }

    /** initialise a str stack var */
    override fun initStackVarString(stackOffset: Int, stringDataOffset: Int, constStrAddress: String) {
        outputCodeTabNl("sub\tr2, fp, #${-stringDataOffset}")
        outputCodeTab("str\tr2, [fp, #$stackOffset]\t\t")
        outputCommentNl("initialise local var string address")
        if (constStrAddress.isNotEmpty()) {
            outputCodeTabNl("ldr\tr1, $constStrAddress${GLOBAL_VARS_ADDR_SUFFIX}")
            outputCodeTabNl("mov\tr0, r2")
            outputCodeTab("bl\tstrcpy\t\t")
            outputCommentNl("initialise local var string")
        }
    }

    /** get address of string variable in accumulator */
    override fun getStringVarAddress(identifier: String) {
        outputCodeTabNl("ldr\tr3, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
    }

    /** save acc string to buffer and address in stack - acc is pointer */
    override fun saveString() {
        outputCodeTab("mov\tr1, r3\t\t")
        outputCommentNl("save string - strcpy(string_buffer, r3)")
        outputCodeTabNl("ldr\tr0, ${STRING_BUFFER}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("bl\tstrcpy")
        outputCodeTabNl("mov\tr3, r0")
        outputCodeTabNl("str\tr3, [sp, #-4]!")
    }

    /** add acc string to buf string - both are pointers*/
    override fun addString() {
        outputCodeTab("ldr\tr0, [sp], #4\t\t")
        outputCommentNl("add string - strcat(top-of-stack, r3)")
        outputCodeTabNl("mov\tr1, r3")
        outputCodeTabNl("bl\tstrcat")
        outputCodeTabNl("mov\tr3, r0")
    }

    /** set string variable from accumulator (var and acc are pointers */
    override fun assignmentString(identifier: String) {
        outputCodeTab("mov\tr1, r3\t\t")
        outputCommentNl("assign string - strcpy(identifier, r3)")
        outputCodeTabNl("ldr\tr0, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("bl\tstrcpy")
    }

    /** set string variable from accumulator (var and acc are pointers */
    override fun assignmentStringLocalVar(stackOffset: Int) {
        outputCodeTab("mov\tr1, r3\t\t")
        outputCommentNl("assign string - strcpy([fp-offset], r3)")
        outputCodeTabNl("ldr\tr0, [fp, #${stackOffset}]")
        outputCodeTabNl("bl\tstrcpy")
    }

    /** print string - address in accumulator */
    override fun printStr() {
        outputCodeTab("mov\tr0, r3\t\t")
        outputCommentNl("string pointer to be printed in r0")
        outputCodeTabNl("bl\tprintf")
        outputCodeTabNl("mov\tr0, #0")
        outputCodeTabNl("bl\tfflush")
    }

    /** read string into global variable - address in accumulator*/
    override fun readString(identifier: String, length: Int) {
        outputCodeTab("mov\tr0, #0\t\t")
        outputCommentNl("stdin")
        outputCodeTab("ldr\tr1, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}\t\t")
        outputCommentNl("address of the string to be read")
        outputCodeTab("mov\tr2, #${length}\t\t")
        outputCommentNl("max number of bytes to read")
        outputCodeTabNl("bl\tread")
        outputCodeTab("ldr\tr2, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}\t\t")
        outputCommentNl("get rid of the newline at the end of the string")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("sub\tr0, r0, #1")
        outputCodeTabNl("str\tr3, [r2, r0]")
        outputCodeTabNl("mov\tr3, r0")
    }

    /** read string into local variable - address in accumulator*/
    override fun readStringLocal(stackOffset: Int, length: Int) {
        outputCodeTab("mov\tr0, #0\t\t")
        outputCommentNl("stdin")
        outputCodeTab("ldr\tr1, [fp, #${stackOffset}]\t\t")
        outputCommentNl("address of the string to be read")
        outputCodeTab("mov\tr2, #${length}\t\t")
        outputCommentNl("max number of bytes to read")
        outputCodeTabNl("bl\tread")
        outputCodeTab("ldr\tr2, [fp, #${stackOffset}]\t\t")
        outputCommentNl("get rid of the newline at the end of the string")
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("sub\tr0, r0, #1")
        outputCodeTabNl("strb\tr3, [r2, r0]")
        outputCodeTabNl("mov\tr3, r0")
    }

    /** compare 2 strings for equality */
    override fun compareStringEquals() {
        outputCodeTab("ldr\tr0, [sp], #4\t\t")
        outputCommentNl("compare strings - strcmp(top-of-stack, r3)")
        outputCodeTabNl("mov\tr1, r3")
        outputCodeTabNl("bl\tstrcmp")
        outputCodeTabNl("and\tr3, r0, #1")    // r3 = 0 and Z flag set if equal
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("moveq\tr3, #1")     // set r3 to 1 if comparison is ==
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    /** compare 2 strings for non-equality */
    override fun compareStringNotEquals() {
        outputCodeTab("ldr\tr0, [sp], #4")
        outputCommentNl("compare strings - strcmp(top-of-stack, r3)")
        outputCodeTabNl("mov\tr1, r3")
        outputCodeTabNl("bl\tstrcmp")
        outputCodeTabNl("and\tr3, r0, #1")    // r3 = 0 and Z flag set if equal
        outputCodeTabNl("mov\tr3, #0")
        outputCodeTabNl("movne\tr3, #1")     // set r3 to 1 if comparison is !=
        outputCodeTabNl("ands\tr3, r3, #1")   // zero the rest of r3 and set flags - Z flag set = FALSE
    }

    /** string constants */
    override fun stringConstantsDataSpace() {
        code.outputCodeNl()
        code.outputCodeNl(".data")
        code.outputCodeTabNl(".align 4")
        code.outputCommentNl("buffer for string operations - max str length limit")
        code.outputCodeTabNl("$STRING_BUFFER:\t.space $STR_BUF_SIZE")
    }

    //////////////////////////////////////////////////////////////////////

    /** dummy instruction */
    override fun dummyInstr(cmd: String) = outputCodeTabNl(cmd)

}
