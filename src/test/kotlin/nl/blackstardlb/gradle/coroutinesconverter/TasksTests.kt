package nl.blackstardlb.gradle.coroutinesconverter

import nl.blackstardlb.gradle.coroutinesconverter.TestUtils.PLUGIN_ID
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TasksTests {
    @get:Rule
    var testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File
    private lateinit var gradleRunner: GradleRunner

    @Before
    fun setup() {
        buildFile = testProjectDir.newFile("build.gradle")
        buildFile.appendText(
            """
            plugins {
                id '$PLUGIN_ID'
            }
            
        """.trimIndent()
        )
        gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())
    }

    @Test
    fun checkHasTasks() {
        buildFile.appendText(
            """
            coroutinesConverter {
                wrapperTypes = ["REACTOR"]
            }
        """.trimIndent()
        )
        println(buildFile.readText())
        val result = gradleRunner
            .withArguments("tasks")
            .build()
        println(result.output)
    }
}
