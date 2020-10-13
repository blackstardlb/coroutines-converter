package nl.blackstardlb.gradle.coroutinesconverter

import nl.blackstardlb.gradle.coroutinesconverter.config.CoroutinesConverterExtension
import nl.blackstardlb.gradle.coroutinesconverter.tasks.CreateWrappersTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget


class CoroutinesConverterPlugin : Plugin<Project> {
    companion object {
        const val EXTENSION_NAME = "coroutinesConverter"
        const val TASK_GROUP = "coroutines converter"
        const val MODULE_NAME = "jvmWrapped"
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create(
            EXTENSION_NAME,
            CoroutinesConverterExtension::class.java,
            target
        )

        val delete = target.tasks.create("cleanWrappers", Delete::class.java)
        delete.setDelete(extension.outputDir)
        delete.group = TASK_GROUP

        target.gradle.projectsEvaluated {
            val createWrappers = target.task("createWrappers")
            createWrappers.group = TASK_GROUP
            createWrappers.dependsOn(delete)

            val functionCallWrappers =
                extension.wrapperTypes.map { it.toWrapperType().originalFunctionCallWrapper.invoke(extension.coroutinesVersion) }

            functionCallWrappers.forEach {
                val task = target.tasks.create(
                    "create${it.prefix}Wrappers",
                    CreateWrappersTask::class.java,
                    it
                )
                createWrappers.dependsOn(task)
            }

            extension.jvmTarget?.let { kotlinJvmTarget ->
                val mainCompilation = kotlinJvmTarget.compilations.main

                val jvmWrapped = kotlinJvmTarget.compilations.create(MODULE_NAME) {
                    it.defaultSourceSet {
                        this.kotlin.srcDir(extension.outputDir)
                        this.dependsOn(mainCompilation.defaultSourceSet)
                        it.dependencies {
                            val fileCollection =
                                mainCompilation.compileDependencyFiles + mainCompilation.output.classesDirs
                            this.implementation(fileCollection)
                            functionCallWrappers.flatMap { it.dependencies }.toSet().forEach {
                                this.implementation(it)
                            }
                        }
                    }
                }

                target.tasks.getByName(jvmWrapped.compileKotlinTaskName).dependsOn(createWrappers)

                createWrappers.dependsOn(mainCompilation.compileKotlinTask)

                val jvmWrappedJar = target.tasks.create("${MODULE_NAME}Jar", Jar::class.java)
                jvmWrappedJar.group = TASK_GROUP
                jvmWrappedJar.archiveBaseName.set("${target.name}-${MODULE_NAME}")
                jvmWrappedJar.from(jvmWrapped.output)
                jvmWrappedJar.dependsOn(createWrappers)

                setupPublication(target, jvmWrapped, jvmWrappedJar, kotlinJvmTarget)
            }
        }
    }

    private fun setupPublication(
        target: Project,
        jvmCompilation: KotlinJvmCompilation,
        jar: Jar,
        kotlinJvmTarget: KotlinJvmTarget
    ) {
        target.publishing().publications {
            it.create(MODULE_NAME, MavenPublication::class.java) {
                it.groupId = target.group.toString()
                it.version = target.version.toString()
                it.artifactId = "${target.name}-${MODULE_NAME}"
                it.artifact(jar.archiveFile.get())
                it.pom.withXml {
                    val dependencies = it.asNode().appendNode("dependencies")

                    target.configurations
                        .getByName(jvmCompilation.compileDependencyConfigurationName)
                        .allDependencies
                        .filterIsInstance<DefaultExternalModuleDependency>()
                        .forEach {
                            val node = dependencies.appendNode("dependency")
                            node.appendNode("groupId", it.group)
                            node.appendNode("artifactId", it.name)
                            node.appendNode("version", it.version)
                        }

                    val node = dependencies.appendNode("dependency")
                    node.appendNode("groupId", target.group)
                    node.appendNode("artifactId", "${target.name}-${kotlinJvmTarget.name}")
                    node.appendNode("version", target.version)
                }
            }
        }

        target.tasks.getByName("generatePomFileFor${MODULE_NAME.capitalize()}Publication").dependsOn(jar)
    }
}
