package nl.blackstardlb.gradle.coroutinesconverter

import org.junit.Test
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths

class LoadClasses {

    @Test
    fun loadClasses() {
        val urls: Array<URL> =
            arrayOf(Paths.get("/home/dbrison/Projects/HHSServer/build/classes/kotlin/jvm/main/").toUri().toURL())
        val urlClassLoader = URLClassLoader(urls, Thread.currentThread().contextClassLoader)
        val loadClass = urlClassLoader.loadClass("nl.hhsserver.service.AuthenticationService")
        val loadClass2 = urlClassLoader.loadClass("nl.hhsserver.service.HHSServer")
    }
}
