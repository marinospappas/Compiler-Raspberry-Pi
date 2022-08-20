package mpdev.compilerv3.chapter_xa_01

import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class CompileOutputTest {

    @Test
    fun `Compile Tinsel`() {
        main(arrayOf("tinsel/compiler_test/prog_full_test.tnsl", "-o", "test.s"))
        assert(true)
    }
}