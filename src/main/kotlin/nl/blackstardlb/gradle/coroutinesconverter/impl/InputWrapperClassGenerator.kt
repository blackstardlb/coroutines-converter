package nl.blackstardlb.gradle.coroutinesconverter.impl

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions

class InputWrapperClassGenerator(
    private val originalFunctionCallWrapper: OriginalFunctionCallWrapper,
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) {
    fun wrapClass(className: String): TypeSpec {
        return wrapClass(classLoader.loadClass(className).kotlin)
    }

    fun wrapClass(kClass: KClass<*>): TypeSpec {
        val functions = kClass.memberFunctions
            .filter { it.isSuspend && it.visibility == KVisibility.PUBLIC }
            .map { wrapFunction(it) }

        val abstractFunctions = functions.map { abstractFunction(it) }
        return TypeSpec.classBuilder(originalFunctionCallWrapper.prefix + kClass.simpleName)
            .addSuperinterface(kClass.asTypeName())
            .addModifiers(KModifier.ABSTRACT)
            .addFunctions(abstractFunctions)
            .addFunctions(functions)
            .build()
    }

    private fun abstractFunction(funSpec: FunSpec): FunSpec {
        return FunSpec.builder(funSpec.name + originalFunctionCallWrapper.prefix)
            .addModifiers(KModifier.ABSTRACT)
            .returns(originalFunctionCallWrapper.wrapperReturnType.parameterizedBy(funSpec.returnType as TypeName))
            .addParameters(funSpec.parameters)
            .build()
    }

    fun wrapFunction(kFunction: KFunction<*>): FunSpec {
        return FunSpec.builder(kFunction.name)
            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            .returns(kFunction.returnType.asTypeName())
            .addParameters(kFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }.map { wrapParameter(it) })
            .addCode(originalFunctionCallWrapper.wrapInput(codeBlockFromOriginFunction(kFunction)))
            .build()
    }

    fun codeBlockFromOriginFunction(kFunction: KFunction<*>): CodeBlock {
        val paramString =
            kFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }.joinToString(",") { it.name ?: "" }
        return CodeBlock.builder()
            .addStatement("%L%L(%L)", kFunction.name, originalFunctionCallWrapper.prefix, paramString)
            .build()
    }

    fun wrapParameter(kParameter: KParameter): ParameterSpec {
        return ParameterSpec.builder(kParameter.name!!, kParameter.type.asTypeName())
            .build()
    }
}
