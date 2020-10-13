package nl.blackstardlb.gradle.coroutinesconverter.config

import org.gradle.api.tasks.SourceSet
import java.io.File
import java.net.URI
import java.net.URL
import java.util.function.Supplier

class KtorClientGenConfiguration(
    val name: String,
    sourceSet: SourceSet,
    private val clientOutputFolderSupplier: Supplier<File>,
    private val clientPackageSupplier: Supplier<String>
) {
    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("Name must not be blank nor empty")
        }
    }

    var yamlFile: File = sourceSet.resources.srcDirs.first()

    var clientBaseURL: URL = URI.create("http://localhost/").toURL()

    val clientFile: File
        get() = clientOutputFolderSupplier.get().resolve(clientPackage.replace(".", "/")).resolve("${name}Client.kt")

    val clientPackage: String
        get() = clientPackageSupplier.get()
}
