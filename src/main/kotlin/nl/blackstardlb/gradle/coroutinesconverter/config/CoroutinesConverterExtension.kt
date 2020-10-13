package nl.blackstardlb.gradle.coroutinesconverter.config

import nl.blackstardlb.gradle.coroutinesconverter.CoroutinesConverterPlugin
import nl.blackstardlb.gradle.coroutinesconverter.main
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

open class CoroutinesConverterExtension(project: Project) {
    var wrapperTypes: MutableList<String> = mutableListOf()
    var classes: MutableList<String> = mutableListOf()
    val outputDir = project.buildDir.resolve("${CoroutinesConverterPlugin.EXTENSION_NAME}/generated")
    var jvmTarget: KotlinJvmTarget? = null
    var coroutinesVersion = "1.3.9"
    val configuration: FileCollection?
        get() = jvmTarget?.compilations?.main?.compileDependencyFiles
}


internal typealias KtorClientGenConfigurationContainer =
        NamedDomainObjectContainer<KtorClientGenConfiguration>
