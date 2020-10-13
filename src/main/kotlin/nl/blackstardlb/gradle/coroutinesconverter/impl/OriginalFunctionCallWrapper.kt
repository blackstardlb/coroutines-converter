package nl.blackstardlb.gradle.coroutinesconverter.impl

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

interface OriginalFunctionCallWrapper {
    val prefix: String
    val wrapperReturnType: ClassName
    val imports: Map<String, List<String>>
    val extraProperties: Map<String, TypeName>
    val dependencies: List<String>
    fun wrapOutput(originalCall: CodeBlock): CodeBlock
    fun wrapInput(originalCall: CodeBlock): CodeBlock
}
