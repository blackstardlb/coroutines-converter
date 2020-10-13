package nl.blackstardlb.gradle.coroutinesconverter

import nl.blackstardlb.gradle.coroutinesconverter.CoroutinesConverterPlugin.Companion.EXTENSION_NAME
import nl.blackstardlb.gradle.coroutinesconverter.config.CoroutinesConverterExtension
import nl.blackstardlb.gradle.coroutinesconverter.impl.WrapperType
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GUtil
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation

internal fun Project.sourceSets(): SourceSetContainer {
    return this.extensions.getByName("sourceSets") as SourceSetContainer
}

internal fun Project.coroutinesConverterConfig(): CoroutinesConverterExtension =
    extensions.getByName(EXTENSION_NAME) as? CoroutinesConverterExtension
        ?: throw IllegalStateException("$EXTENSION_NAME is not of the correct type")

internal fun Project.kotlin(): KotlinMultiplatformExtension =
    extensions.getByName("kotlin") as? KotlinMultiplatformExtension
        ?: throw IllegalStateException("kotlin is not of the correct type")

internal fun Project.publishing(): PublishingExtension =
    extensions.getByName("publishing") as? PublishingExtension
        ?: throw IllegalStateException("publishing is not of the correct type")

internal fun String.toTaskName(): String {
    return this.toLowerCase()
        .map(::toValidTaskNameCharacters)
        .joinToString(separator = "")
        .toCamelCase()
}

internal fun String.toWrapperType(): WrapperType = WrapperType.valueOf(this)

internal fun toValidTaskNameCharacters(char: Char): Char {
    return if (char != '_' && Character.isJavaIdentifierPart(char)) {
        char
    } else {
        ' '
    }
}

internal fun String.toCamelCase() = GUtil.toCamelCase(this)

internal val NamedDomainObjectContainer<KotlinJvmCompilation>.main
    get() = this.getByName("main")
