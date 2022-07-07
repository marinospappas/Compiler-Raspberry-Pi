package mpdev.compilerv3.chapter_xa_01

import java.io.File
import java.io.PrintStream
import java.lang.System.err
import java.lang.System.out
import java.util.Date

/** this class implements all the instructions for the target machine */
class Arm_32Instructions(outFile: String = ""): CodeModule {

    private val CODE_ID = "Arm-32 Assembly Code - Raspberry Pi"
    override val COMMENT = "@"
    private val MAIN_ENTRYPOINT = "main"
    private val MAIN_EXITPOINT = "${MAIN_BLOCK}_exit_"

    private var outStream: PrintStream = out

    private var outputLines = 0

    // the offset from base pointer for the next local variable (in the stack)
    override var stackVarOffset = 0

    // flag to include the string buffer in the assembly code
    override var includeStringBuffer = false

    // sizes of various types
    override val INT_SIZE = 4    // 64-bit integers
    override val STRPTR_SIZE = 4     // string pointer 64 bit

    // global vars list - need for entering the global var addresses in the .text section
    private val globalVarsList = mutableListOf<String>()
    private val GLOBAL_VARS_ADDR_SUFFIX = "_addr"

    // various string constants
    val TINSEL_MSG = "tinsel_msg"
    val NEWLINE = "newline"
    val INT_FMT = "int_fmt"

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
    override val funInpParamsCpuRegisters = arrayOf("r0", "r1", "r2", "r3")
    // during the assignment of the parameters, their values are saved temporarily here,
    // so that they are not corrupted by function calls executed during the assignment of the parameters
    override val funTempParamsCpuRegisters = arrayOf("r4", "r5", "r6", "r7")
    // 4 params maximum allowed
    override val MAX_FUN_PARAMS = funInpParamsCpuRegisters.size

    /** output lines */
    override fun getOutputLines() = outputLines

    /** output code */
    override fun outputCode(s: String) {
        outStream.print(s)
        outputLines += s.count { it == '\n' }
    }
    /** output code with newline */
    override fun outputCodeNl(s: String) = outputCode("$s\n")
    /** output code with tab */
    override fun outputCodeTab(s: String) = outputCode("\t$s")
    /** output code with tab and newline */
    override fun outputCodeTabNl(s: String) = outputCodeTab("$s\n")
    /** output comment */
    override fun outputComment(s: String) = outputCode("$COMMENT $s")
    /** output comment with newline*/
    override fun outputCommentNl(s: String) = outputComment("$s\n")
    /** output a label */
    override fun outputLabel(s: String) = outputCodeNl("$s:")

    /** initialisation code for assembler */
    override fun progInit(progName: String) {
        outputCommentNl(CODE_ID)
        outputCommentNl("program $progName")
        outputCommentNl("compiled on ${Date()}")
        outputCodeNl(".data")
        outputCodeNl(".align 2")
        // copyright message
        outputCodeTabNl("$TINSEL_MSG: .asciz \"TINSEL version 3.0 for Arm-32 (Raspberry Pi) July 2022 (c) M.Pappas\\n\"")
        // newline string
        outputCodeTabNl("$NEWLINE: .asciz \"\\n\"")
        // int format for printf
        outputCodeTabNl("$INT_FMT: .asciz \"%d\"")
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
        outputCodeNl(".align 2")
        outputCodeNl(".global $MAIN_ENTRYPOINT")
    }

    /** declare function */
    override fun declareAsmFun(name: String) {
        outputCodeNl()
        outputCodeNl(".type $name %function")
        outputCommentNl("function $name")
        outputLabel(name)
        outputCodeTab("push\t{fp, lr}\t\t")
        outputCommentNl("save registers")
        newStackFrame()
    }

    /** transfer a function parameter to stack variable */
    override fun storeFunParamToStack(paramIndx: Int, stackOffset: Int) {
        outputCodeTabNl("movq\t${funInpParamsCpuRegisters[paramIndx]}, $stackOffset(%rbp)")
    }

    /** end of function - tidy up stack */
    private fun funEnd() {
        restoreStackFrame()
        outputCodeTab("pop\t{fp, pc}\t\t")
        outputCommentNl("restore registers - lr goes into pc to return to caller")
    }

    /** set a temporary function param register to the value of %rax (the result of the last expression) */
    override fun setIntTempFunParam(paramIndx: Int) {
        if (funTempParamsCpuRegisters[paramIndx] == "%rax")
            return
        outputCodeTabNl("pushq\t${funTempParamsCpuRegisters[paramIndx]}")
        outputCodeTabNl("movq\t%rax, ${funTempParamsCpuRegisters[paramIndx]}")
    }

    /** set a function input param register from the temporary register */
    override fun setFunParamReg(paramIndx: Int) {
        outputCodeTabNl("movq\t${funTempParamsCpuRegisters[paramIndx]}, ${funInpParamsCpuRegisters[paramIndx]}")
    }

    /** restore a function input param register */
    override fun restoreFunTempParamReg(paramIndx: Int) {
        if (funTempParamsCpuRegisters[paramIndx] == "%rax")
            return
        outputCodeTabNl("popq\t${funTempParamsCpuRegisters[paramIndx]}")
    }

    /** initial code for main */
    override fun mainInit() {
        outputCodeNl()
        outputCodeNl(".type $MAIN_ENTRYPOINT %function")
        outputCommentNl("main program")
        outputLabel(MAIN_ENTRYPOINT)
        outputCodeTab("push\t{fp, lr}\t\t")
        outputCommentNl("save registers")
        //newStackFrame()
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
        //restoreStackFrame()
        outputCodeTab("mov\tr0, #0\t\t")
        outputCommentNl("exit code 0")
        outputCodeTabNl("pop\t{fp, lr}")
        outputCodeTabNl("bx\tlr")
        setGlobalVarAddresses()
    }

    /** set the addresses of the global vars in the .text section */
    private fun setGlobalVarAddresses() {
        val globalVarNamesList = globalVarsList + stringConstants.keys +
                listOf(TINSEL_MSG, NEWLINE, INT_FMT)
        outputCodeNl("")
        outputCodeNl(".align 2")
        outputCommentNl("global var addresses go here")
        globalVarNamesList.forEach{ varname ->
            outputCodeNl("${varname}${GLOBAL_VARS_ADDR_SUFFIX}:\t.word $varname")
        }
    }

    /** set new stack frame */
    private fun newStackFrame() {
        outputCodeTab("pushq\t%rbp\t\t")
        outputCommentNl("new stack frame")
        outputCodeTabNl("movq\t%rsp, %rbp")
        stackVarOffset = 0  // reset the offset for stack vars in this new frame
    }

    /** restore stack frame */
    private fun restoreStackFrame() {
        outputCodeTab("movq\t%rbp, %rsp\t\t")
        outputCommentNl("restore stack frame")
        outputCodeTabNl("popq\t%rbp")
    }

    /**
     * allocate variable space in the stack
     * returns the new stack offset for this new variable
     */
    override fun allocateStackVar(size: Int): Int {
        outputCodeTabNl("subq\t$${size}, %rsp")
        stackVarOffset -= size
        return stackVarOffset
    }

    /** release variable space in the stack */
    override fun releaseStackVar(size: Int) {
        outputCodeTabNl("addq\t$${size}, %rsp")
        stackVarOffset += size
    }

    /** initialise an int stack var */
    override fun initStackVarInt(stackOffset : Int, initValue: String) {
        outputCodeTab("movq\t$$initValue, ")
        if (stackOffset != 0)
            outputCode("$stackOffset")
        outputCodeNl("(%rbp)")
    }

    /** exit the program */
    override fun exitProgram() {
        jump(MAIN_EXITPOINT)
    }
    //////////////////////////////////////////////////////////////

    /** set accumulator to a value */
    override fun setAccumulator(value: String) {
        outputCodeTabNl("mov\tr3, #${value}")
        outputCodeTabNl("tst\tr3, r3")    // also set flags - Z flag set = FALSE
    }

    /** clear accumulator */
    override fun clearAccumulator() = outputCodeTabNl("xor\tr3, r3")

    /** increment accumulator */
    override fun incAccumulator() = outputCodeTabNl("add\tr3, r3, #1")

    /** decrement accumulator */
    override fun decAccumulator() = outputCodeTabNl("sub\tr3, r3, #1")

    /** push accumulator to the stack */
    override fun saveAccumulator() = outputCodeTabNl("push\t{r3}")

    /** add top of stack to accumulator */
    override fun addToAccumulator() {
        outputCodeTabNl("pop\t{r2}")
        outputCodeTabNl("add\tr3, r3, r2")
    }

    /** subtract top of stack from accumulator */
    override fun subFromAccumulator() {
        outputCodeTabNl("pop\t{r2}")
        outputCodeTabNl("sub\tr3, r3, r2")
    }

    /** negate accumulator */
    override fun negateAccumulator() = outputCodeTabNl("sub\tr3, #0, r3")

    /** multiply accumulator by top of stack */
    override fun multiplyAccumulator() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("imulq\t%rbx, %rax")
    }

    /** divide accumulator by top of stack */
    override fun divideAccumulator() {
        outputCodeTabNl("movq\t%rax, %rbx")
        outputCodeTabNl("popq\t%rax")
        outputCodeTab("cqto\t\t")
        outputCommentNl("sign extend to rdx")
        outputCodeTabNl("idivq\t%rbx, %rax")
    }

    /** set accumulator to variable */
    override fun setAccumulatorToVar(identifier: String) {
        outputCodeTabNl("ldr\tr2, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("ldr\tr3, [r2]")
        outputCodeTabNl("tst\tr3, r3")    // also set flags - Z flag set = FALSE
    }

    /** set accumulator to local variable */
    override fun setAccumulatorToLocalVar(offset: Int) {
        outputCodeTab("movq\t")
        if (offset != 0)
            outputCode("$offset")
        outputCodeNl("(%rbp), %rax")
        outputCodeTabNl("testq\t%rax, %rax")    // also set flags - Z flag set = FALSE
    }

    /** call a function */
    override fun callFunction(subroutine: String) = outputCodeTabNl("bl\t${subroutine}")

    /** return from function */
    override fun returnFromCall() {
        funEnd()
    }

    /** set variable to accumulator */
    override fun assignment(identifier: String) {
        outputCodeTabNl("ldr\tr2, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
        outputCodeTabNl("str\tr3, [r2]")
    }

    /** set stack variable to accumulator */
    override fun assignmentLocalVar(offset: Int) {
        outputCodeTab("movq\t%rax, ")
        if (offset != 0)
            outputCode("$offset")
        outputCodeNl("(%rbp)")
    }

    /** branch if false */
    override fun jumpIfFalse(label: String) = outputCodeTabNl("jz\t$label")    // Z flag set = FALSE

    /** branch */
    override fun jump(label: String) = outputCodeTabNl("b\t$label")

    /** boolean not accumulator */
    override fun booleanNotAccumulator() = outputCodeTabNl("xorq\t$1, %rax")

    /** or top of stack with accumulator */
    override fun orAccumulator() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("orq\t%rbx, %rax")
    }

    /** exclusive or top of stack with accumulator */
    override fun xorAccumulator() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("xorq\t%rbx, %rax")
    }

    /** and top of stack with accumulator */
    override fun andAccumulator() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("andq\t%rbx, %rax")
    }

    /** compare and set accumulator and flags - is equal to */
    override fun compareEquals() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("cmp\t%rax, %rbx")
        outputCodeTabNl("sete\t%al")        // set AL to 1 if comparison is ==
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is not equal to */
    override fun compareNotEquals() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("cmp\t%rax, %rbx")
        outputCodeTabNl("setne\t%al")       // set AL to 1 if comparison is !=
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is less than */
    override fun compareLess() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("cmp\t%rax, %rbx")
        outputCodeTabNl("setl\t%al")        // set AL to 1 if comparison is rbx < rax
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is less than */
    override fun compareLessEqual() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("cmp\t%rax, %rbx")
        outputCodeTabNl("setle\t%al")       // set AL to 1 if comparison is <=
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is greater than */
    override fun compareGreater() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("cmp\t%rax, %rbx")
        outputCodeTabNl("setg\t%al")       // set AL to 1 if comparison is >
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE
    }

    /** compare and set accumulator and flags - is greater than */
    override fun compareGreaterEqual() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("cmp\t%rax, %rbx")
        outputCodeTabNl("setge\t%al")       // set AL to 1 if comparison is >=
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE
    }

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
    }

    /** read global int var into variable */
    override fun readInt(identifier: String) {
        outputCodeTabNl("lea\t$identifier(%rip), %rdi\t\t# address of the variable to be read")
        outputCodeTabNl("call\tread_i_")
    }

    /** read local int var into variable */
    override fun readIntLocal(stackOffset: Int) {
        outputCodeTab("movq\t")
        if (stackOffset != 0)
            outputCode("$stackOffset")
        outputCodeNl("(%rbp), %rdi\t\t# address of the variable to be read")
        outputCodeTabNl("call\tread_i_")
    }

    /** end of program */
    override fun progEnd() {
        outputCodeNl()
        outputCommentNl("end program")
    }

    ////////// string operations ///////////////////////

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
        outputCodeTabNl("lea\t$stringDataOffset(%rbp), %rax")
        outputCodeTab("movq\t%rax, $stackOffset(%rbp)\t\t")
        outputCommentNl("initialise local var string address")
        if (constStrAddress.isNotEmpty()) {
            outputCodeTabNl("lea\t$constStrAddress(%rip), %rsi")
            outputCodeTabNl("movq\t$stackOffset(%rbp), %rdi")
            outputCodeTab("call\tstrcpy_\t\t")
            outputCommentNl("initialise local var string")
        }
    }

    /** get address of string variable in accumulator */
    override fun getStringVarAddress(identifier: String) {
        outputCodeTabNl("ldr\tr3, ${identifier}${GLOBAL_VARS_ADDR_SUFFIX}")
    }

    /** save acc string to buffer and address in stack - acc is pointer */
    override fun saveString() {
        outputCodeTab("movq\t%rax, %rsi\t\t")
        outputCommentNl("save string - strcpy_(string_buffer, %rax)")
        outputCodeTabNl("lea\t$STRING_BUFFER(%rip), %rdi")
        outputCodeTabNl("call\tstrcpy_")
        outputCodeTabNl("pushq\t%rax")
        includeStringBuffer = true
    }

    /** add acc string to buf string - both are pointers*/
    override fun addString() {
        outputCodeTab("popq\t%rdi\t\t")
        outputCommentNl("add string - strcat_(top-of-stack, %rax)")
        outputCodeTabNl("movq\t%rax, %rsi")
        outputCodeTabNl("call\tstrcat_")
    }

    /** set string variable from accumulator (var and acc are pointers */
    override fun assignmentString(identifier: String) {
        outputCodeTab("movq\t%rax, %rsi\t\t")
        outputCommentNl("assign string - strcpy_(identifier, %rax)")
        outputCodeTabNl("lea\t${identifier}(%rip), %rdi")
        outputCodeTabNl("call\tstrcpy_")
    }

    /** set string variable from accumulator (var and acc are pointers */
    override fun assignmentStringLocalVar(stackOffset: Int) {
        outputCodeTab("movq\t%rax, %rsi\t\t")
        outputCommentNl("assign string - strcpy_(offset(%rbp), %rax)")
        outputCodeTab("movq\t")
        if (stackOffset != 0)
            outputCode("$stackOffset")
        outputCodeNl("(%rbp), %rdi")
        outputCodeTabNl("call\tstrcpy_")
    }

    /** print string - address in accumulator */
    override fun printStr() {
        outputCodeTab("mov\tr0, r3\t\t")
        outputCommentNl("string pointer to be printed in r0")
        outputCodeTabNl("bl\tprintf")
    }

    /** read string into global variable - address in accumulator*/
    override fun readString(identifier: String, length: Int) {
        outputCodeTabNl("lea\t$identifier(%rip), %rdi\t\t# address of the string to be read")
        outputCodeTabNl("movq\t$${length}, %rsi\t\t# max number of bytes to read")
        outputCodeTabNl("call\tread_s_")
    }

    /** read string into local variable - address in accumulator*/
    override fun readStringLocal(stackOffset: Int, length: Int) {
        outputCodeTab("movq\t")
        if (stackOffset != 0)
            outputCode("$stackOffset")
        outputCodeNl("(%rbp), %rdi\t\t# address of the string to be read")
        outputCodeTabNl("movq\t$${length}, %rsi\t\t# max number of bytes to read")
        outputCodeTabNl("call\tread_s_")
    }

    /** compare 2 strings for equality */
    override fun compareStringEquals() {
        outputCodeTab("popq\t%rdi\t\t")
        outputCommentNl("compare strings - streq_(top-of-stack, %rax)")
        outputCodeTabNl("movq\t%rax, %rsi")
        outputCodeTabNl("call\tstreq_")
        outputCodeTabNl("andq\t$1, %rax")   // set flags - Z flag set = FALSE
    }

    /** compare 2 strings for non-equality */
    override fun compareStringNotEquals() {
        outputCodeTab("popq\t%rdi\t\t")
        outputCommentNl("compare strings - streq_(top-of-stack, %rax)")
        outputCodeTabNl("movq\t%rax, %rsi")
        outputCodeTabNl("call\tstreq_")
        outputCodeTabNl("xorq\t$1, %rax")   // boolean not rax and set flags - Z flag set = FALSE
    }

    /** string constants */
    override fun stringConstantsDataSpace() {
        code.outputCodeNl()
        code.outputCodeNl(".data")
        code.outputCodeTabNl(".align 2")
        if (includeStringBuffer) {
            code.outputCommentNl("buffer for string operations - max str length limit")
            code.outputCodeTabNl("$STRING_BUFFER:\t.space $STR_BUF_SIZE")
        }
    }

    //////////////////////////////////////////////////////////
    /** dummy instruction */
    override fun dummyInstr(cmd: String) = outputCodeTabNl(cmd)

}
