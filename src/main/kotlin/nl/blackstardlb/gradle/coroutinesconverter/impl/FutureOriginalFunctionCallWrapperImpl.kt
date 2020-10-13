package nl.blackstardlb.gradle.coroutinesconverter.impl

import com.squareup.kotlinpoet.*
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.CompletableFuture

class FutureOriginalFunctionCallWrapperImpl(coroutinesVersion: String) : OriginalFunctionCallWrapper {
    companion object {
        const val COROUTINE_SCOPE_PROPERTY_NAME = "coroutineScope"
        val coroutineScopeType = CoroutineScope::class.asTypeName()
    }

    override val prefix: String = "Future"
    override val wrapperReturnType: ClassName = CompletableFuture::class.asClassName()
    override val imports: Map<String, List<String>> = mapOf(
        "kotlinx.coroutines.future" to listOf("future", "await"),
        "kotlinx.coroutines" to listOf("GlobalScope"),
    )
    override val extraProperties: Map<String, TypeName> = mapOf(
        COROUTINE_SCOPE_PROPERTY_NAME to coroutineScopeType
    )
    override val dependencies: List<String> = listOf("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    override fun wrapOutput(originalCall: CodeBlock): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("return %L.future", COROUTINE_SCOPE_PROPERTY_NAME)
            .add(originalCall)
            .endControlFlow()
            .build()
    }

    override fun wrapInput(originalCall: CodeBlock): CodeBlock {
        return CodeBlock.builder()
            .add("return %L.await()", originalCall)
            .build()
    }
}
