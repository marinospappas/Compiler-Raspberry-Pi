package mpdev.compilerv3.chapter_xa_04

import java.io.File
import java.io.PrintStream
import java.lang.System.err
import java.lang.System.out
import java.util.Date

/** this class implements all the instructions for the target machine */
class X86_64Instructions(outFile: String = ""): CodeModule {

    private val CODE_ID = "x86-64 Assembly Code - AT&T format"
    override val COMMENT = "#"
    override var outputLines: Int = 0
    override var outStream: PrintStream = out

    private val MAIN_ENTRYPOINT = "main"
    private val MAIN_EXITPOINT = "${MAIN_BLOCK}_exit_"


    // the offset from base pointer for the next local variable (in the stack)
    override var stackVarOffset = 0

    // flag to include the string buffer in the assembly code
    var includeStringBuffer = false

    // architecture word size.
    val WORD_SIZE = 8  // 64-bit architecture

    // sizes of various types
    override val INT_SIZE = WORD_SIZE    // 64-bit integers
    override val PTR_SIZE = WORD_SIZE    // pointer 64 bit

    override val DEF_INT_FMT = "def_int_fmt"
    override val INT_FMT = "int_fmt"

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

    /** register names for the function params - in order 1-6 (x86-64 architecture specific) */
    // these registers hold the fun params at the time of the call
    override val funInpParamsCpuRegisters = arrayOf("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
    // during the assignment of the parameters, their values are saved temporarily here,
    // so that they are not corrupted by function calls executed during the assignment of the parameters
    override val funTempParamsCpuRegisters = arrayOf("%rbx", "%r12", "%r13", "%r14", "%r15", "%rax")
    // 6 params maximum allowed
    override val MAX_FUN_PARAMS = funInpParamsCpuRegisters.size

    override fun outputComment(s: String) = outputCode("$COMMENT $s")

    /** initialisation code for assembler */
    override fun progInit(progOrLib: String, progName: String) {
        outputCommentNl(CODE_ID)
        outputCommentNl("$progOrLib $progName")
        outputCommentNl("compiled on ${Date()}")
        outputCodeNl(".data")
        outputCodeNl(".align 8")
        // copyright message
        outputCodeTabNl("tinsel_msg_: .string \"TINSEL version 3.2 for x86-84 (Linux) November 2022 (c) M.Pappas\\n\"")
        // newline string
        outputCodeTabNl("newline_: .string \"\\n\"")
        outputCodeNl(".align 8")
    }

    /** declare int variable (64bit) */
    override fun declareInt(varName: String, initValue: String) {
        if (initValue == "")
            outputCodeTabNl("$varName:\t.quad 0")       // uninitialised global int vars default to 0
        else
            outputCodeTabNl("$varName:\t.quad $initValue")
    }

    /** declare int array variable (64bit) */
    override fun declareIntArray(varName: String, length: String) {
        outputCodeTabNl("$varName:\t.space ${length.toInt()*INT_SIZE}")
    }

    /** initial code for functions */
    override fun funInit() {
        outputCodeNl()
        outputCodeNl(".text")
        outputCodeNl(".align 8")
    }

    /** declare function */
    override fun declareAsmFun(name: String) {
        outputCommentNl("function $name")
        outputLabel(name)
        outputCodeTab("pushq\t%rbx\t\t")
        outputCommentNl("save \"callee\"-save registers")
        newStackFrame()
    }

    /** transfer a function parameter to stack variable */
    override fun storeFunParamToStack(paramIndx: Int, stackOffset: Int) {
        outputCodeTabNl("movq\t${funInpParamsCpuRegisters[paramIndx]}, $stackOffset(%rbp)")
    }

    /** end of function - tidy up stack */
    private fun funEnd() {
        restoreStackFrame()
        outputCodeTab("popq\t%rbx\t\t")
        outputCommentNl("restore \"callee\"-save registers")
    }

    /** set a temporary function param register to the value of %rax (the result of the last expression) */
    override fun setIntTempFunParam(paramIndx: Int) {
        outputCodeTab("pushq\t${funTempParamsCpuRegisters[paramIndx]}\t")
        outputCommentNl("save temp param register ${funTempParamsCpuRegisters[paramIndx]} to stack")
        outputCodeTabNl("movq\t%rax, ${funTempParamsCpuRegisters[paramIndx]}")
    }

    /** set a function input param register from the temporary register */
    override fun setFunParamRegFromTempReg(paramIndx: Int) {
        outputCodeTabNl("movq\t${funTempParamsCpuRegisters[paramIndx]}, ${funInpParamsCpuRegisters[paramIndx]}")
    }

    /** set a function input param register from accumulator */
    override fun setFunParamRegFromAcc(paramIndx: Int) {
        outputCodeTabNl("movq\t%rax, ${funInpParamsCpuRegisters[paramIndx]}")
    }

    /** restore a function input param register */
    override fun restoreFunTempParamReg(paramIndx: Int) {
        if (funTempParamsCpuRegisters[paramIndx] == "%rax")
            return
        outputCodeTab("popq\t${funTempParamsCpuRegisters[paramIndx]}\t")
        outputCommentNl("restore temp param register ${funTempParamsCpuRegisters[paramIndx]} from stack")
    }

    override fun globalSymbol(name: String) {
        outputCodeNl(".global $name")
    }

    /** initial code for main */
    override fun mainInit() {
        outputCodeNl()
        globalSymbol(MAIN_ENTRYPOINT)
        outputCommentNl("main program")
        outputLabel(MAIN_ENTRYPOINT)
        outputCodeTab("pushq\t%rbx\t\t")
        outputCommentNl("save \"callee\"-save registers")
        newStackFrame()
        outputCommentNl("print hello message")
        outputCodeTabNl("lea\ttinsel_msg_(%rip), %rdi")
        outputCodeTabNl("call\twrite_s_")
        outputCodeNl()
    }

    /** termination code for assembler */
    override fun mainEnd() {
        outputCodeNl()
        outputCommentNl("end of main")
        outputLabel(MAIN_EXITPOINT)
        restoreStackFrame()
        outputCodeTab("popq\t%rbx\t\t")
        outputCommentNl("restore \"callee\"-save registers")
        outputCommentNl("exit system call")
        outputCodeTab("xorq\t%rax, %rax\t\t")
        outputCommentNl("exit code 0")
        outputCodeTabNl("ret")
    }

    /** create relative addresses for global vars */
    override fun createRelativeAddresses() {}

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

    /** initiliase an int stack var */
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
        outputCodeTabNl("movq\t$${value}, %rax")
        outputCodeTabNl("testq\t%rax, %rax")    // also set flags - Z flag set = FALSE
    }

    /** clear accumulator */
    override fun clearAccumulator() = outputCodeTabNl("xorq\t%rax, %rax")

    /** increment accumulator */
    override fun incAccumulator() = outputCodeTabNl("incq\t%rax")

    /** decrement accumulator */
    override fun decAccumulator() = outputCodeTabNl("decq\t%rax")

    /** push accumulator to the stack */
    override fun saveAccumulator() = outputCodeTabNl("pushq\t%rax")

    /** add top of stack to accumulator */
    override fun addToAccumulator() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("addq\t%rbx, %rax")
    }

    /** subtract top of stack from accumulator */
    override fun subFromAccumulator() {
        outputCodeTabNl("movq\t%rax, %rbx")
        outputCodeTabNl("popq\t%rax")
        outputCodeTabNl("subq\t%rbx, %rax")
    }

    /** negate accumulator */
    override fun negateAccumulator() = outputCodeTabNl("negq\t%rax")

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

    /** modulo after divide accumulator by top of stack */
    override fun moduloAccumulator() {
        divideAccumulator()
        outputCodeTabNl("movq\t%rdx, %rax")     // modulo in rdx
    }

    /** bitwise not accumulator */
    override fun notAccumulator() {
        outputCodeTabNl("xorq\t%rax, -1")
    }

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

    /** shift accumulator left */
    override fun shiftAccumulatorLeft() {
        outputCodeTabNl("movq\t%rax, %rcx")
        outputCodeTabNl("popq\t%rax")
        outputCodeTabNl("shlq\t%cl, %rax")
    }

    /** shift accumulator right */
    override fun shiftAccumulatorRight() {
        outputCodeTabNl("movq\t%rax, %rcx")
        outputCodeTabNl("popq\t%rax")
        outputCodeTabNl("shrq\t%cl, %rax")
    }

    /** set accumulator to global variable value */
    override fun setAccumulatorToVar(identifier: String) {
        outputCodeTabNl("movq\t${identifier}(%rip), %rax")
        outputCodeTabNl("testq\t%rax, %rax")    // also set flags - Z flag set = FALSE
    }

    override fun setAccumulatorToArrayVar(identifier: String) {
        // index already in %rcx
        outputCodeTabNl("lea\t${identifier}(%rip), %rax")  // array address in %rax
        outputCodeTabNl("movq\t(%rax, %rcx, $INT_SIZE), %rax")  // get array element
        outputCodeTabNl("testq\t%rax, %rax")    // also set flags - Z flag set = FALSE
    }

    /** set accumulator to global variable address */
    override fun setAccumulatorToVarAddress(identifier: String) {
        outputCodeTabNl("lea\t${identifier}(%rip), %rax")
    }

    override fun saveAccToTempReg() {
        outputCodeTabNl("movq\t%rax, %rcx")
    }

    override fun pointerAssignment() {
        outputCodeTabNl("movq\t%rax, (%rcx)")
    }

    override fun setAccumulatorToPointerVar() {
        outputCodeTabNl("movq\t(%rcx), %rax")
    }

    /** set accumulator to local variable */
    override fun setAccumulatorToLocalVar(offset: Int) {
        outputCodeTab("movq\t")
        if (offset != 0)
            outputCode("$offset")
        outputCodeNl("(%rbp), %rax")
        outputCodeTabNl("testq\t%rax, %rax")    // also set flags - Z flag set = FALSE
    }

    override fun setAccumulatorToLocalArrayVar(offset: Int) {
        // index already in %rcx
        outputCodeTab("movq\t")
        if (offset != 0)
            outputCode("$offset")
        outputCodeNl("(%rbp), %rax")            // array address in %rax
        outputCodeTabNl("movq\t(%rax, %rcx, $INT_SIZE), %rax")  // get array element
        outputCodeTabNl("testq\t%rax, %rax")    // also set flags - Z flag set = FALSE
    }

    /** set accumulator to local variable address */
    override fun setAccumulatorToLocalVarAddress(offset: Int) {
        outputCodeTab("lea\t")
        if (offset != 0)
            outputCode("$offset")
        outputCodeNl("(%rbp), %rax")
    }

    /** call a function */
    override fun callFunction(subroutine: String) = outputCodeTabNl("call\t${subroutine}")

    /** return from function */
    override fun returnFromCall() {
        funEnd()
        outputCodeTabNl("ret")
    }

    /** set variable to accumulator */
    override fun assignment(identifier: String) = outputCodeTabNl("movq\t%rax, ${identifier}(%rip)")

    /** set stack variable to accumulator */
    override fun assignmentLocalVar(offset: Int) {
        outputCodeTab("movq\t%rax, ")
        if (offset != 0)
            outputCode("$offset")
        outputCodeNl("(%rbp)")
    }

    /** set array element to accumulator */
    override fun arrayAssignment(identifier: String) {
        // index already in %rcx
        outputCodeTabNl("movq\t%rax, %rbx")     // save value in %rbx
        outputCodeTabNl("lea\t${identifier}(%rip), %rax")  // array start address in %rax
        outputCodeTabNl("movq\t%rbx, (%rax, %rcx, $INT_SIZE)")  // save array element
    }

    /** set stack array element to accumulator */
    override fun assignmentLocalArrayVar(offset: Int) {
        // index already in %rcx
        outputCodeTabNl("movq\t%rax, %rbx")     // save value in %rbx
        outputCodeTab("movq\t")
        if (offset != 0)
            outputCode("$offset")
        outputCodeNl("(%rbp), %rax")            // array start address in %rax
        outputCodeTabNl("movq\t%rbx, (%rax, %rcx, $INT_SIZE)")  // save array element
    }

    /** branch if false */
    override fun jumpIfFalse(label: String) = outputCodeTabNl("jz\t$label")    // Z flag set = FALSE

    /** branch */
    override fun jump(label: String) = outputCodeTabNl("jmp\t$label")

    /** boolean not accumulator */
    override fun booleanNotAccumulator() {
        outputCodeTabNl("testq\t%rax, %rax")
        outputCodeTabNl("sete\t%al")        // set AL to 1 if rax is 0
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE

    }

    override fun booleanOrAccumulator() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("testq\t%rbx, %rbx") // convert op1 to 0-1
        outputCodeTabNl("setne\t%bl")        // set BL to 1 if rbx is not 0
        outputCodeTabNl("andq\t$1, %rbx")   // zero the rest of rbx and set flags

        outputCodeTabNl("testq\t%rax, %rax")  // convert op2 to 0-1
        outputCodeTabNl("setne\t%al")        // set AL to 1 if rax is 0
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE

        outputCodeTabNl("orq\t%rbx, %rax")
    }

    override fun booleanAndAccumulator() {
        outputCodeTabNl("popq\t%rbx")
        outputCodeTabNl("testq\t%rbx, %rbx") // convert op1 to 0-1
        outputCodeTabNl("setne\t%bl")        // set BL to 1 if rbx is not 0
        outputCodeTabNl("andq\t$1, %rbx")   // zero the rest of rbx and set flags

        outputCodeTabNl("testq\t%rax, %rax")  // convert op2 to 0-1
        outputCodeTabNl("setne\t%al")        // set AL to 1 if rax is 0
        outputCodeTabNl("andq\t$1, %rax")   // zero the rest of rax and set flags - Z flag set = FALSE

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
        outputCodeTabNl("lea\tnewline_(%rip), %rdi")
        outputCodeTabNl("call\twrite_s_")
    }

    /** print accumulator as integer */
    override fun printInt(fmt: String) {
        //TODO: support various dec formats
        outputCodeTabNl("movq\t%rax, %rdi\t\t# value to be printed in rdi")
        outputCodeTabNl("call\twrite_i_")
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
    override fun progEnd(libOrProg: String) {
        outputCodeNl()
        outputCommentNl("end $libOrProg")
    }

    ////////// string operations ///////////////////////

    /** declare string global variable */
    override fun declareString(varName: String, initValue: String, length: Int) {
        if (length == 0 || initValue != "")
            outputCodeTabNl("$varName:\t.string \"$initValue\"")
        else
            outputCodeTabNl("$varName:\t.space $length") // uninitialised string vars must have length
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
    override fun getStringVarAddress(identifier: String) = outputCodeTabNl("lea\t${identifier}(%rip), %rax")

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
        outputCodeTabNl("movq\t%rax, %rdi\t\t# string pointer to be printed in rdi")
        outputCodeTabNl("call\twrite_s_")
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
        code.outputCodeTabNl(".align 8")
        if (includeStringBuffer) {
            code.outputCommentNl("buffer for string operations - max str length limit")
            code.outputCodeTabNl("$STRING_BUFFER:\t.space $STR_BUF_SIZE")
        }
    }

    //////////////////////////////////////////////////////////
    /** dummy instruction */
    override fun dummyInstr(cmd: String) = outputCodeTabNl(cmd)

}
