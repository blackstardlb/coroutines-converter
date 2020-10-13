package nl.blackstardlb.gradle.coroutinesconverter.impl

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions

class OutputWrapperClassGenerator(
    private val originalFunctionCallWrapper: OriginalFunctionCallWrapper,
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) {
    fun wrapClass(className: String): TypeSpec {
        return wrapClass(classLoader.loadClass(className).kotlin)
    }

    fun wrapClass(kClass: KClass<*>): TypeSpec {
        val instanceName = kClass.simpleName!!.decapitalize()
        val functions = kClass.memberFunctions
            .filter { it.isSuspend && it.visibility == KVisibility.PUBLIC }
            .map { wrapFunction(it, instanceName) }
        return TypeSpec.classBuilder(originalFunctionCallWrapper.prefix + kClass.simpleName)
            .primaryConstructor(primaryConstructor(kClass, instanceName))
            .addProperties(properties(kClass, instanceName))
            .addFunctions(functions)
            .build()
    }

    private fun properties(kClass: KClass<*>, instanceName: String): Iterable<PropertySpec> {
        return listOf(
            PropertySpec.builder(instanceName, kClass.asTypeName(), KModifier.PRIVATE)
                .initializer(instanceName)
                .build()
        ) + this.originalFunctionCallWrapper.extraProperties.map {
            PropertySpec.builder(it.key, it.value, KModifier.PRIVATE)
                .initializer(it.key)
                .build()
        }
    }

    fun primaryConstructor(kClass: KClass<*>, instanceName: String): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter(instanceName, kClass.asTypeName())
            .addParameters(this.originalFunctionCallWrapper.extraProperties.map {
                ParameterSpec(it.key, it.value)
            })
            .build()
    }

    fun wrapFunction(kFunction: KFunction<*>, instanceName: String): FunSpec {
        return FunSpec.builder(kFunction.name)
            .returns(originalFunctionCallWrapper.wrapperReturnType.parameterizedBy(kFunction.returnType.asTypeName()))
            .addParameters(kFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }.map { wrapParameter(it) })
            .addCode(originalFunctionCallWrapper.wrapOutput(codeBlockFromOriginFunction(kFunction, instanceName)))
            .build()
    }

    fun codeBlockFromOriginFunction(kFunction: KFunction<*>, originClassInstanceName: String): CodeBlock {
        val paramString =
            kFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }.joinToString(",") { it.name ?: "" }
        return CodeBlock.builder()
            .addStatement("%L.%L(%L)", originClassInstanceName, kFunction.name, paramString)
            .build()
    }

    fun wrapParameter(kParameter: KParameter): ParameterSpec {
        return ParameterSpec.builder(kParameter.name!!, kParameter.type.asTypeName())
            .build()
    }
}
