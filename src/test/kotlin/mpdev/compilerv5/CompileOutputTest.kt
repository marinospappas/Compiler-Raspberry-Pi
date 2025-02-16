package mpdev.compilerv5

import org.junit.jupiter.api.Test

class CompileOutputTest {

    @Test
    fun `Compile Tinsel`() {
        main(arrayOf("tinsel/compiler_test/prog_test_1.tnsl", "-o", "test.s"))
        main(arrayOf("tinsel/compiler_test/prog_test_2.tnsl", "-o", "test.s"))
        main(arrayOf("tinsel/compiler_test/prog_test_3.tnsl", "-o", "test.s"))
        main(arrayOf("tinsel/compiler_test/prog_test_4.tnsl", "-o", "test.s"))
        main(arrayOf("tinsel/compiler_test/prog_test_5.tnsl", "-o", "test.s"))
        assert(true)
    }
}