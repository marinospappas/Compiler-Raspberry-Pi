package mpdev.compilerv5

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import java.io.File
import java.util.stream.Stream

@DisplayName("Compiler Full Test")
@TestMethodOrder(OrderAnnotation::class)

class CompilerDynamicTest {

    private lateinit var h: TestHelper

    @TestFactory
    fun `Overall Program Test`(): Stream<DynamicTest> {
        val tests = mutableListOf<DynamicTest>()
        h = TestHelper("programtest")
        val filesList = getFilesList()
        for (file in filesList)
            tests.add(
                dynamicTest(file) {
                    val expError = h.getExpectedErr(file)
                    val actualError = h.getActualError(file)
                    assertEquals(expError, actualError, "Compiler Error Check")
                    val asmOut = h.checkAsmOutput(file)
                    assertEquals("", asmOut, "Compiler Output Check")
                }
            )
        return tests.stream()
    }

    private fun getFilesList(): Stream<String> {
        val filesList = mutableListOf<String>()
        File("src/test/resources/${h.testDir}").walk().forEach { file ->
            if (file.isFile)
                filesList.add(file.nameWithoutExtension)
        }
        return filesList.stream()
    }

}
