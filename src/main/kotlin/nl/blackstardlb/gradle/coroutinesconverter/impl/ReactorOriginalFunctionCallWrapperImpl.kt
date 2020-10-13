package nl.blackstardlb.gradle.coroutinesconverter.impl

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import reactor.core.publisher.Mono

class ReactorOriginalFunctionCallWrapperImpl(coroutinesVersion: String) : OriginalFunctionCallWrapper {
    override val prefix: String = "Reactor"
    override val wrapperReturnType: ClassName = Mono::class.asClassName()
    override val imports: Map<String, List<String>> = mapOf(
        "kotlinx.coroutines.reactive" to listOf("awaitFirst"),
        "kotlinx.coroutines.reactor" to listOf("mono")
    )
    override val extraProperties: Map<String, TypeName> = emptyMap()

    override fun wrapOutput(originalCall: CodeBlock): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("return mono")
            .add(originalCall)
            .endControlFlow()
            .build()
    }

    override fun wrapInput(originalCall: CodeBlock): CodeBlock {
        return CodeBlock.builder()
            .add("return %L.awaitFirst()", originalCall)
            .build()
    }

    override val dependencies: List<String> = listOf("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
}
