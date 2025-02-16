package mpdev.compilerv5

import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * Full Compiler Test Version 1
 * Each section of the compiler is tested in an inner class marked @Nested
 * Each group of tests (test dir) is orgnised as a Parameterized unit test
 * The function that provides the argument stream for each test returns the files names in the test dir
 */
@DisplayName("Full Compiler Test")
@TestInstance(Lifecycle.PER_CLASS)

class FullCompilerTest {

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Program Structure Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser0Test {

        private lateinit var h1: TestHelper
        @ParameterizedTest
        @MethodSource("programTestFileProvider")
        @Order(1)
        fun `Test Overall Program`(testName: String, testReporter: TestReporter) {
            h1.runTest(testName, testReporter)
        }

        private lateinit var h2: TestHelper
        @ParameterizedTest
        @MethodSource("varTestFileProvider")
        @Order(2)
        fun `Test Variables Declarations`(testName: String, testReporter: TestReporter) {
            h2.runTest(testName, testReporter)
        }

        private lateinit var h3: TestHelper
        @ParameterizedTest
        @MethodSource("funTestFileProvider")
        @Order(3)
        fun `Test Functions Declarations`(testName: String, testReporter: TestReporter) {
            h3.runTest(testName, testReporter)
        }

        private lateinit var h4: TestHelper
        @ParameterizedTest
        @MethodSource("mainTestFileProvider")
        @Order(4)
        fun `Test Main Block`(testName: String, testReporter: TestReporter) {
            h4.runTest(testName, testReporter)
        }

        // parameter provider functions
        private fun programTestFileProvider(): Stream<String> {
            h1 = TestHelper("programtest")
            return h1.getFilesList()
        }
        private fun varTestFileProvider(): Stream<String> {
            h2 = TestHelper("vartest")
            return h2.getFilesList()
        }
        private fun funTestFileProvider(): Stream<String> {
            h3 = TestHelper("funtest")
            return h3.getFilesList()
        }
        private fun mainTestFileProvider(): Stream<String> {
            h4 = TestHelper("maintest")
            return h4.getFilesList()
        }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Control Structures Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser1Test {

        private lateinit var h1: TestHelper
        @ParameterizedTest
        @MethodSource("blockTestFileProvider")
        @Order(1)
        fun `Test Block Structure`(testName: String, testReporter: TestReporter) {
            h1.runTest(testName, testReporter)
        }

        private lateinit var h2: TestHelper
        @ParameterizedTest
        @MethodSource("ifTestFileProvider")
        @Order(2)
        fun `Test If Structure`(testName: String, testReporter: TestReporter) {
            h2.runTest(testName, testReporter)
        }

        private lateinit var h3: TestHelper
        @ParameterizedTest
        @MethodSource("whileTestFileProvider")
        @Order(3)
        fun `Test While Structure`(testName: String, testReporter: TestReporter) {
            h3.runTest(testName, testReporter)
        }

        private lateinit var h4: TestHelper
        @ParameterizedTest
        @MethodSource("repeatTestFileProvider")
        @Order(4)
        fun `Test Repeat Structure`(testName: String, testReporter: TestReporter) {
            h4.runTest(testName, testReporter)
        }

        private lateinit var h5: TestHelper
        @ParameterizedTest
        @MethodSource("breakTestFileProvider")
        @Order(5)
        fun `Test Break Statement`(testName: String, testReporter: TestReporter) {
            h5.runTest(testName, testReporter)
        }

        private lateinit var h6: TestHelper
        @ParameterizedTest
        @MethodSource("readTestFileProvider")
        @Order(6)
        fun `Test Read Statement`(testName: String, testReporter: TestReporter) {
            h6.runTest(testName, testReporter)
        }

        private lateinit var h7: TestHelper
        @ParameterizedTest
        @MethodSource("printTestFileProvider")
        @Order(7)
        fun `Test Print Statement`(testName: String, testReporter: TestReporter) {
            h7.runTest(testName, testReporter)
        }

        private lateinit var h8: TestHelper
        @ParameterizedTest
        @MethodSource("returnTestFileProvider")
        @Order(8)
        fun `Test Return Statement`(testName: String, testReporter: TestReporter) {
            h8.runTest(testName, testReporter)
        }

        private lateinit var h9: TestHelper
        @ParameterizedTest
        @MethodSource("forTestFileProvider")
        @Order(9)
        fun `Test For Structure`(testName: String, testReporter: TestReporter) {
            h9.runTest(testName, testReporter)
        }

        // parameter provider functions
        private fun blockTestFileProvider(): Stream<String> {
            h1 = TestHelper("blocktest")
            return h1.getFilesList()
        }
        private fun ifTestFileProvider(): Stream<String> {
            h2 = TestHelper("iftest")
            return h2.getFilesList()
        }
        private fun whileTestFileProvider(): Stream<String> {
            h3 = TestHelper("whiletest")
            return h3.getFilesList()
        }
        private fun repeatTestFileProvider(): Stream<String> {
            h4 = TestHelper("repeattest")
            return h4.getFilesList()
        }
        private fun breakTestFileProvider(): Stream<String> {
            h5 = TestHelper("breaktest")
            return h5.getFilesList()
        }
        private fun readTestFileProvider(): Stream<String> {
            h6 = TestHelper("readtest")
            return h6.getFilesList()
        }
        private fun printTestFileProvider(): Stream<String> {
            h7 = TestHelper("printtest")
            return h7.getFilesList()
        }
        private fun returnTestFileProvider(): Stream<String> {
            h8 = TestHelper("returntest")
            return h8.getFilesList()
        }
        private fun forTestFileProvider(): Stream<String> {
            h9 = TestHelper("fortest")
            return h9.getFilesList()
        }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Numerical Expressions Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser2Test {

        private lateinit var h1: TestHelper
        @ParameterizedTest
        @MethodSource("integerNumTestFileProvider")
        @Order(1)
        fun `Test Integer Numbers`(testName: String, testReporter: TestReporter) {
            h1.runTest(testName, testReporter)
        }

        private lateinit var h2: TestHelper
        @ParameterizedTest
        @MethodSource("integerVarTestFileProvider")
        @Order(2)
        fun `Test Integer Variables`(testName: String, testReporter: TestReporter) {
            h2.runTest(testName, testReporter)
        }

        private lateinit var h3: TestHelper
        @ParameterizedTest
        @MethodSource("integerFunTestFileProvider")
        @Order(3)
        fun `Test Integer Functions`(testName: String, testReporter: TestReporter) {
            h3.runTest(testName, testReporter)
        }

        // parameter provider functions
        private fun integerNumTestFileProvider(): Stream<String> {
            h1 = TestHelper("intnumtest")
            return h1.getFilesList()
        }
        private fun integerVarTestFileProvider(): Stream<String> {
            h2 = TestHelper("intvartest")
            return h2.getFilesList()
        }
        private fun integerFunTestFileProvider(): Stream<String> {
            h3 = TestHelper("intfuntest")
            return h3.getFilesList()
        }
    }

    @Disabled
    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Boolean Expressions Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser3Test {

        private lateinit var h1: TestHelper
        @ParameterizedTest
        @MethodSource("programTestFileProvider")
        @Order(1)
        fun `Test Overall Program`(testName: String, testReporter: TestReporter) {
            h1.runTest(testName, testReporter)
        }

        // parameter provider functions
        private fun programTestFileProvider(): Stream<String> {
            h1 = TestHelper("programtest")
            return h1.getFilesList()
        }
    }

    @Disabled
    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    //@Order(5)
    @DisplayName("String Expressions Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser4Test {

        private lateinit var h1: TestHelper
        @ParameterizedTest
        @MethodSource("programTestFileProvider")
        @Order(1)
        fun `Test Overall Program`(testName: String, testReporter: TestReporter) {
            h1.runTest(testName, testReporter)
        }

        // parameter provider functions
        private fun programTestFileProvider(): Stream<String> {
            h1 = TestHelper("programtest")
            return h1.getFilesList()
        }
    }

}



