package mpdev.compilerv3.chapter_xa_01

import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation

/**
 * Full Compiler Test Version 2
 * Each section of the compiler is again tested in an inner class marked @Nested
 * Each group of tests (test dir) is run under one unit test and each test in that group is run in a different thread
 * Achieves reduction by more than 75% in execution time compared parameterized tests
 * where each individual test is run as a separate unit test sequentially
 */
@DisplayName("Full Compiler Test V2")
@TestInstance(Lifecycle.PER_CLASS)
class FullCompilerTestV2 {

    // multithreaded flag - set to true to activate mutlithreading (each test will be run in different thread)
    val multiThreaded = true

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Program Structure Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser0Test {
        @Test
        @Order(1)
        fun `Test Overall Program`(testReporter: TestReporter) {
            TestHelper("programtest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(2)
        fun `Test Variables Declarations`(testReporter: TestReporter) {
            TestHelper("vartest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(3)
        fun `Test Functions Declarations`(testReporter: TestReporter) {
            TestHelper("funtest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(4)
        fun `Test Main Block`(testReporter: TestReporter) {
            TestHelper("maintest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(5)
        fun `Test Functions Parameters`(testReporter: TestReporter) {
            TestHelper("funparamtest").runAllTests(testReporter, multiThreaded)
        }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Control Structures Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser1Test {
        @Test
        @Order(1)
        fun `Test Block Structure`(testReporter: TestReporter) {
            TestHelper("blocktest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(2)
        fun `Test If Structure`(testReporter: TestReporter) {
            TestHelper("iftest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(3)
        fun `Test While Structure`(testReporter: TestReporter) {
            TestHelper("whiletest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(4)
        fun `Test Repeat Structure`(testReporter: TestReporter) {
            TestHelper("repeattest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(5)
        fun `Test Break Statement`(testReporter: TestReporter) {
            TestHelper("breaktest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(6)
        fun `Test Continue Statement`(testReporter: TestReporter) {
            TestHelper("continuetest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(7)
        fun `Test Read Statement`(testReporter: TestReporter) {
            TestHelper("readtest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(8)
        fun `Test Print Statement`(testReporter: TestReporter) {
            TestHelper("printtest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(9)
        fun `Test Return Statement`(testReporter: TestReporter) {
            TestHelper("returntest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(10)
        fun `Test For Structure`(testReporter: TestReporter) {
            TestHelper("fortest").runAllTests(testReporter, multiThreaded)
        }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Numerical Expressions Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser2Test {
        @Test
        @Order(1)
        fun `Test Integer Numbers`(testReporter: TestReporter) {
            TestHelper("intnumtest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(2)
        fun `Test Integer Variables`(testReporter: TestReporter) {
            TestHelper("intvartest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(3)
        fun `Test Integer Functions`(testReporter: TestReporter) {
            TestHelper("intfuntest").runAllTests(testReporter, multiThreaded)
        }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("String Expressions Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser4Test {
        @Test
        @Order(1)
        fun `Test String Variables`(testReporter: TestReporter) {
            TestHelper("stringvartest").runAllTests(testReporter, multiThreaded)
        }
        @Test
        @Order(2)
        fun `Test String Expressions`(testReporter: TestReporter) {
            TestHelper("stringexprtest").runAllTests(testReporter, multiThreaded)
        }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Boolean Expressions Tests")
    @TestMethodOrder(OrderAnnotation::class)
    inner class ProgParser3Test {
        @Test
        @Order(1)
        fun `Test Boolean Expressions`(testReporter: TestReporter) {
            TestHelper("boolexprtest").runAllTests(testReporter, multiThreaded)
        }
    }
}
