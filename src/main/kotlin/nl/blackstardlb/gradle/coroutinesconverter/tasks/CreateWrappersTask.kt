package nl.blackstardlb.gradle.coroutinesconverter.tasks

import nl.blackstardlb.gradle.coroutinesconverter.CoroutinesConverterPlugin
import nl.blackstardlb.gradle.coroutinesconverter.coroutinesConverterConfig
import nl.blackstardlb.gradle.coroutinesconverter.impl.OriginalFunctionCallWrapper
import nl.blackstardlb.gradle.coroutinesconverter.impl.WrapperFileGenerator
import nl.blackstardlb.gradle.coroutinesconverter.main
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URLClassLoader
import javax.inject.Inject

open class CreateWrappersTask @Inject constructor(private val functionCallWrapper: OriginalFunctionCallWrapper) :
    DefaultTask() {
    init {
        group = CoroutinesConverterPlugin.TASK_GROUP
    }

    @TaskAction
    fun doAction() {
        val coroutinesConverterConfig = project.coroutinesConverterConfig()
        val classPath = coroutinesConverterConfig.configuration?.files ?: emptySet()
        val kotlinJvmTarget = coroutinesConverterConfig.jvmTarget ?: error("no jvm target")
        val classLoader = classLoader(classPath, kotlinJvmTarget.compilations.main.output.classesDirs)
        val wrapperFileGenerator = WrapperFileGenerator(functionCallWrapper, classLoader)
        val fileSpecs = coroutinesConverterConfig.classes.flatMap {
            val kClass = classLoader.loadClass(it).kotlin
            wrapperFileGenerator.wrapClassPropertiesAndInstanceMethod(kClass)
        }.toSet()
        fileSpecs.forEach {
            it.writeTo(coroutinesConverterConfig.outputDir)
        }
    }

    private fun classLoader(classPath: Set<File>, classesDirs: ConfigurableFileCollection): ClassLoader {
        val urlsList = classPath.map {
            it.toURI().toURL()
        } + classesDirs.map { it.absoluteFile.toURI().toURL() }
        val urls = urlsList.toTypedArray()
        return URLClassLoader(urls, Thread.currentThread().contextClassLoader)
    }
}
