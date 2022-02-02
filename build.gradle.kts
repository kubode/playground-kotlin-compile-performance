import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
abstract class CompareKotlinCompileTask : DefaultTask() {

    private val codeRepeats = 4000
    private val warmUpRepeats = 4
    private val measureRepeats = 10

    @org.gradle.api.tasks.TaskAction
    fun doAction() {
        project.exec {
            val stdout = commandLine("kotlinc", "-version").standardOutput.writer().use { it.toString() }
            println("Kotlin compiler version: $stdout")
        }
        compile("implicit") { "val a$it = mutableListOf<Int>()" }
        compile("explicit") { "val a$it: MutableList<Int> = mutableListOf()" }
    }

    private fun compile(
        name: String,
        prefix: String = "",
        postfix: String = "\n",
        transform: (index: Int) -> String,
    ): kotlin.time.Duration {
        val code = (0 until codeRepeats).joinToString(
            prefix = prefix,
            transform = transform,
            separator = "\n",
            postfix = postfix,
        )
        val outputDir = File("${project.buildDir}/compare/")
        val ktFile = File("$outputDir/test.kt")

        // Warm up
        println("Warming up... $name")
        repeat(warmUpRepeats) {
            doCompile(name, outputDir, ktFile, code, it)
        }

        // Measure
        println()
        println("Measure... $name")
        val durations = mutableListOf<kotlin.time.Duration>()
        repeat(measureRepeats) {
            val duration = doCompile(name, outputDir, ktFile, code, it)
            durations += duration
        }
        println()
        println("$name result")
        println("min: ${durations.minOf { it }}")
        println("max: ${durations.maxOf { it }}")
        println("avg: ${durations.average()}")
        println("median: ${durations.median()}")
        println()
        return durations.reduce { acc, duration -> acc + duration }
    }

    private fun List<kotlin.time.Duration>.average(): kotlin.time.Duration {
        return reduce { acc, duration -> acc + duration } / size
    }

    private fun List<kotlin.time.Duration>.median(): kotlin.time.Duration {
        val sorted = this.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[middle]
        } else {
            (sorted[middle - 1] + sorted[middle]) / 2
        }
    }

    private fun doCompile(
        name: String,
        outputDir: File,
        ktFile: File,
        code: String,
        attempts: Int
    ): kotlin.time.Duration {
        outputDir.deleteRecursively()
        outputDir.mkdirs()
        ktFile.writeText(code)
        val duration = measureTime {
            project.exec {
                workingDir = outputDir
                commandLine("kotlinc", ktFile.absolutePath)
            }
        }
        println("$name #$attempts: $duration")
        return duration
    }
}

tasks.register<CompareKotlinCompileTask>("compareKotlinCompile") {

}
