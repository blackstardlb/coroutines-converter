package nl.blackstardlb.gradle.coroutinesconverter

import java.net.URL

object ResourceLoader {
    fun loadString(name: String): String {
        return this::class.java.getResourceAsStream("/$name").bufferedReader().readText()
    }

    fun loadPath(name: String): URL {
        return this::class.java.getResource("/$name")
    }
}
