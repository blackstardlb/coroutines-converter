package nl.blackstardlb.gradle.coroutinesconverter

import nl.blackstardlb.gradle.coroutinesconverter.impl.ReactorOriginalFunctionCallWrapperImpl
import nl.blackstardlb.gradle.coroutinesconverter.impl.WrapperFileGenerator
import java.nio.file.Paths
import kotlin.test.Test

internal class WrapperFileGeneratorTest {
    private val originalFunctionCallWrapper = ReactorOriginalFunctionCallWrapperImpl()
    private val wrapperFileGenerator = WrapperFileGenerator(originalFunctionCallWrapper)

//    @Test
//    fun wrapFiles() {
//        val wrapClassPropertiesAndInstanceMethod =
//            wrapperFileGenerator.wrapClassPropertiesAndInstanceMethod(HHSServer::class)
//
//        wrapClassPropertiesAndInstanceMethod.forEach {
//            println(it)
//        }
//        wrapClassPropertiesAndInstanceMethod.forEach {
//            it.writeTo(Paths.get("/home/dbrison/Projects/coroutines-converter/src/test/kotlin"))
//        }
//    }
}
