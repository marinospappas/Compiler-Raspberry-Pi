package mpdev.compilerv3._legacy.chapter_xa_02

import java.io.PrintStream

/** code module interface - defines the functions needed to generate assembly code */
interface CodeModule {
    val COMMENT: String
    var stackVarOffset: Int
    var outStream: PrintStream
    var outputLines: Int

    val funInpParamsCpuRegisters: Array<String>
    val funTempParamsCpuRegisters: Array<String>
    val MAX_FUN_PARAMS: Int

    val INT_SIZE: Int
    val PTR_SIZE: Int

    val DEF_INT_FMT: String
    val INT_FMT: String

    /** output code */
    fun outputCode(s: String) {
        outStream.print(s)
        outputLines += s.count { it == '\n' }
    }
    /** output code with newline */
    fun outputCodeNl(s: String = "") = outputCode("$s\n")
    /** output code with tab */
    fun outputCodeTab(s: String) = outputCode("\t$s")
    /** output code with tab and newline */
    fun outputCodeTabNl(s: String) = outputCodeTab("$s\n")
    /** output comment */
    fun outputComment(s: String) = outputCode("$COMMENT $s")
    /** output comment with newline*/
    fun outputCommentNl(s: String) = outputComment("$s\n")
    /** output a label */
    fun outputLabel(s: String) = outputCodeNl("$s:")

    /** initialisation code for assembler */
    fun progInit(progOrLib: String, progName: String)
    /** declare int variable */
    fun declareInt(varName: String, initValue: String)
    /** initial code for functions */
    fun funInit()
    /** declare function */
    fun declareAsmFun(name: String)
    /** transfer a function parameter to stack variable */
    fun storeFunParamToStack(paramIndx: Int, stackOffset: Int)
    /** set a temporary function param register to the value of %rax (the result of the last expression) */
    fun setIntTempFunParam(paramIndx: Int)
    /** set a function input param register from the temporary register */
    fun setFunParamRegFromTempReg(paramIndx: Int)
    /** set a function input param register from accumulator */
    fun setFunParamRegFromAcc(paramIndx: Int)
    /** restore a function temporary param register */
    fun restoreFunTempParamReg(paramIndx: Int)
    /** check whether a function parameter is in stack
     *  returns the frame offset if yes otherwise -1
     */
    fun isFunParamInStack(paramIndx: Int): Int { return -1 }
    /** restore the stack space used a function stack param */
    fun restoreFunStackParam(paramIndx: Int) {}
    /** initial code for main */
    fun globalSymbol(name: String)
    /** initial code for main */
    fun mainInit()
    /** termination code for assembler */
    fun mainEnd()
    /** create relative addresses for global vars */
    fun createRelativeAddresses()
    /** allocate variable space in the stack - returns the new stack offset for this new variable */
    fun allocateStackVar(size: Int): Int
    /** release variable space in the stack */
    fun releaseStackVar(size: Int)
    /** initiliase an int stack var */
    fun initStackVarInt(stackOffset : Int, initValue: String)
    /** exit the program */
    fun exitProgram()

    /** set accumulator to a value */
    fun setAccumulator(value: String)
    /** clear accumulator */
    fun clearAccumulator()
    /** increment accumulator */
    fun incAccumulator()
    /** decrement accumulator */
    fun decAccumulator()
    /** push accumulator to the stack */
    fun saveAccumulator()
    /** add top of stack to accumulator */
    fun addToAccumulator()
    /** subtract top of stack from accumulator */
    fun subFromAccumulator()
    /** negate accumulator */
    fun negateAccumulator()
    /** multiply accumulator by top of stack */
    fun multiplyAccumulator()
    /** divide accumulator by top of stack */
    fun divideAccumulator()
    /** modulo - divide accumulator by top of stack */
    fun moduloAccumulator()
    /** bitwise not accumulator */
    fun notAccumulator()
    /** bitwise or top of stack with accumulator */
    fun orAccumulator()
    /** bitwise exclusive or top of stack with accumulator */
    fun xorAccumulator()
    /** bitwise and top of stack with accumulator */
    fun andAccumulator()
    /** shift accumulator left */
    fun shiftAccumulatorLeft()
    /** shift accumulator right */
    fun shiftAccumulatorRight()
    /** set accumulator to global variable value */
    fun setAccumulatorToVar(identifier: String)
    /** set accumulator to global variable address */
    fun setAccumulatorToVarAddress(identifier: String)
    /** save pointer value (currently in accumulator) for later use */
    fun savePtrValue()
    /** assignment to previously saved ptr */
    fun pointerAssignment()
    /** get value from pointer to accumulator */
    fun setAccumulatorToPointerVar()
    /** set accumulator to local variable value */
    fun setAccumulatorToLocalVar(offset: Int)
    /** set accumulator to local variable address */
    fun setAccumulatorToLocalVarAddress(offset: Int)
    /** call a function */
    fun callFunction(subroutine: String)
    /** return from function */
    fun returnFromCall()
    /** set variable to accumulator */
    fun assignment(identifier: String)
    /** set stack variable to accumulator */
    fun assignmentLocalVar(offset: Int)

    /** branch if false */
    fun jumpIfFalse(label: String)
    /** branch */
    fun jump(label: String)

    /** boolean not accumulator */
    fun booleanNotAccumulator()
    /** boolean or top of stack with accumulator */
    fun booleanOrAccumulator()
    /** boolean and top of stack with accumulator */
    fun booleanAndAccumulator()

    /** compare and set accumulator and flags - is equal to */
    fun compareEquals()
    /** compare and set accumulator and flags - is not equal to */
    fun compareNotEquals()
    /** compare and set accumulator and flags - is less than */
    fun compareLess()
    /** compare and set accumulator and flags - is less than */
    fun compareLessEqual()
    /** compare and set accumulator and flags - is greater than */
    fun compareGreater()
    /** compare and set accumulator and flags - is greater than */
    fun compareGreaterEqual()

    /** print a newline */
    fun printNewline()
    /** print accumulator as integer */
    fun printInt(fmt: String)
    /** read global int var into variable */
    fun readInt(identifier: String)
    /** read local int var into variable */
    fun readIntLocal(stackOffset: Int)

    /** end of program */
    fun progEnd(libOrProg: String)

    ////////// string operations ///////////////////////
    /** declare string global variable */
    fun declareString(varName: String, initValue: String, length: Int = 0)
    /** initialise a str stack var */
    fun initStackVarString(stackOffset: Int, stringDataOffset: Int, constStrAddress: String)
    /** get address of string variable in accumulator */
    fun getStringVarAddress(identifier: String)
    /** save acc string to buffer and address in stack - acc is pointer */
    fun saveString()
    /** add acc string to buf string - both are pointers*/
    fun addString()
    /** set string variable from accumulator (var and acc are pointers */
    fun assignmentString(identifier: String)
    /** set string variable from accumulator (var and acc are pointers */
    fun assignmentStringLocalVar(stackOffset: Int)
    /** print string - address in accumulator */
    fun printStr()
    /** read string into global variable - address in accumulator*/
    fun readString(identifier: String, length: Int)
    /** read string into local variable - address in accumulator*/
    fun readStringLocal(stackOffset: Int, length: Int)
    /** compare 2 strings for equality */
    fun compareStringEquals()
    /** compare 2 strings for non-equality */
    fun compareStringNotEquals()
    /** string constants */
    fun stringConstantsDataSpace()
    //////////////////////////////////////////////////////////
    /** dummy instruction */
    fun dummyInstr(cmd: String)
}
