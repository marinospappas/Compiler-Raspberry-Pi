package mpdev.compilerv3.chapter_xa_01

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestReporter
import java.io.File
import java.lang.System.err
import java.nio.file.Files
import java.util.stream.Stream

var PROJECT_DIR: String = "."

class TestHelper(var testDir: String = "") {

    init {
        PROJECT_DIR = File("./.idea").canonicalPath.toString().replace("/.idea","")
    }

    companion object {
        // directories
        val TEST_RESOURCES = "src/test/resources"
        val COMPILER_OUTPUT = "_compiler.out"
        // the pass and fail strings
        const val PASS_STRING = "\u001B[32mPASS\u001B[39m"
        const val FAIL_STRING = "\u001B[31mFAIL\u001B[39m"
    }

    // the list of threads and test results
    private val threadList = mutableListOf<Thread>()
    private val resultMap = mutableMapOf<Long,AssertionError>()

    /** run all tests in testDir */
    fun runAllTests(testReporter: TestReporter, multiThreaded: Boolean = false) {
        if (!multiThreaded) {
            // run all tests in the main thread one after the other
            for (test in getFilesList())
                runTest(test, testReporter)
        }
        else
            // multithreaded version
            runAllTestsMultiThreaded(testReporter)
    }

    /** run a specific test single thread */
    fun runTest(testName: String, testReporter: TestReporter) {
        val expError = getExpectedErr(testName)
        val actualError = getActualError(testName)
        assertEquals(expError, actualError, "Compiler Error Check [$testName]")
        val asmOut = checkAsmOutput(testName)
        assertEquals("", asmOut, "Compiler Output Check [$testName]")
        testReporter.publishEntry("$testDir:${testName.substring(4)}: $PASS_STRING, threadId=${Thread.currentThread().id}")
    }

    /** run all tests in testDir - multithreaded */
    private fun runAllTestsMultiThreaded(testReporter: TestReporter) {
        // execute all tests each in a different thread
        for (test in getFilesList()) {
            runTestMultiThread(test, testReporter)
        }
        // wait until all threads are done and check if any of them raised an exception
        var failed = false
        for (t in threadList) {
            t.join()
            val exc = resultMap[t.id]
            if (exc != null) {
                err.println(exc.toString())
                failed = true
            }
        }
        if (failed)
            throw AssertionError("[$testDir] FAILED")
    }

    /** get the list of files */
    fun getFilesList(): Stream<String> {
        val filesList = mutableListOf<String>()
        File("$TEST_RESOURCES/$testDir").walk().forEach { file ->
            if (file.isFile)
                filesList.add(file.nameWithoutExtension)
        }
        return filesList.stream().sorted()
    }

    /** run a specific test in a new thread */
    private fun runTestMultiThread(testName: String, testReporter: TestReporter) {
        val t = Thread {
            try {
                val expError = getExpectedErr(testName)
                val actualError = getActualError(testName)
                assertEquals(expError, actualError, "Compiler Error Check [$testName]")
                val asmOut = checkAsmOutput(testName)
                assertEquals("", asmOut, "Compiler Output Check [$testName]")
                testReporter.publishEntry("$testDir:${testName.substring(4)}: $PASS_STRING, threadId=${Thread.currentThread().id}")
            }
            catch (e: AssertionError) {
                testReporter.publishEntry("$testDir:${testName.substring(4)}: $FAIL_STRING, threadId=${Thread.currentThread().id}")
                // store the assertion exception so that the main thread can check the outcome
                resultMap[Thread.currentThread().id] = e
            }
        }
        t.start()
        // keep the thread in the list so that it can be checked at the end
        threadList.add(t)
    }

    /** get expected result - compiler errors */
    fun getExpectedErr(testName: String): String {
        var expResult = ""
        val testDirName = "$TEST_RESOURCES/$testDir"
        val errFile = "$testDirName.results/$testName.err"
        try {
            expResult = File(errFile).readText()
        }
        catch (_: Exception) {}
        return expResult
    }

    /** run compiler and get error message */
    fun getActualError(testName: String): String {
        val srcFile = "$testDir/$testName.tnsl"
        val outFile = "$COMPILER_OUTPUT/$testName.out"
        return ExecuteCompiler().run(TEST_RESOURCES, srcFile, outFile)
    }

    /** check for assembler output */
    fun checkAsmOutput(testName: String): String {
        val expOutFile = "$TEST_RESOURCES/$testDir.results/$testName.out"
        val actualOutFile = "$TEST_RESOURCES/$COMPILER_OUTPUT/$testName.out"
        return if (File(expOutFile).exists())
            compareFiles(expOutFile, actualOutFile)
        else
            ""
    }

    /** compare two files line by line */
    private fun compareFiles(expFile: String, outFile: String): String {
        val expList = mutableListOf<String>()
        File(expFile).forEachLine { expList.add(it) }
        val outList = mutableListOf<String>()
        File(outFile).forEachLine { outList.add(it) }
        val upperLimit = Integer.min(expList.size, outList.size)
        for (i in 0 until upperLimit) {
            if (expList[i].startsWith("# compiled on"))
                continue
            if (expList[i].contains("tinsel version",true))
                continue
            if (expList[i] != outList[i]) {
                return "Assembly Line ${(i + 1)} - EXPECTED: ${expList[i]} - RECEIVED: ${outList[i]}"
            }
        }
        if (expList.size < outList.size) {
            return "Assembly Line ${upperLimit+1} - EXPECTED: end-of-file - RECEIVED: ${outList[upperLimit]}"
        }
        if (expList.size > outList.size) {
            return "Assembly Line ${upperLimit+1} - EXPECTED: ${expList[upperLimit]} - RECEIVED: end-of-file"
        }
        return ""
    }

}