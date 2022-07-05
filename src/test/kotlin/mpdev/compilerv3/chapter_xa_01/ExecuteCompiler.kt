package mpdev.compilerv3.chapter_xa_01

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

val JDK_DIR = "/home/marinos/.jdks/openjdk-18.0.1.1"
val INTELLIJ_DIR = "/snap/intellij-idea-community/361"
val COMPILER = mutableListOf("$JDK_DIR/bin/java",
    "-Dfile.encoding=UTF-8",
    "-classpath", "$PROJECT_DIR/out/production/Lets-Build-a-Compiler:$INTELLIJ_DIR/plugins/Kotlin/kotlinc/lib/kotlin-stdlib.jar:$INTELLIJ_DIR/plugins/Kotlin/kotlinc/lib/kotlin-reflect.jar:$INTELLIJ_DIR/plugins/Kotlin/kotlinc/lib/kotlin-test.jar",
    "mpdev.compiler.chapter_16_xvi.CompilerMainKt",
    "-o")    // need to add outfile and sourcefile

class ExecuteCompiler {

    fun run(workingDir: String, srcFilename: String, outFilename: String): String {
        val pb = ProcessBuilder(COMPILER + outFilename + srcFilename)
        pb.directory(File(workingDir))
        val proc = pb.start()
        val reader = BufferedReader(InputStreamReader(proc.errorStream))
        val errMsg = reader.readLine()
        /*
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println(line)
        } */
        /*val exitCode =*/ proc.waitFor()
        return errMsg?:""
    }

}
