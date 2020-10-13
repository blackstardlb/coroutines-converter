package nl.blackstardlb.gradle.coroutinesconverter.impl

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties

class WrapperFileGenerator(
    private val originalFunctionCallWrapper: OriginalFunctionCallWrapper,
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) {
    private val outputWrapperClassGenerator: OutputWrapperClassGenerator =
        OutputWrapperClassGenerator(originalFunctionCallWrapper)
    private val inputWrapperClassGenerator: InputWrapperClassGenerator =
        InputWrapperClassGenerator(originalFunctionCallWrapper)

    fun wrapClassPropertiesAndInstanceMethod(kClass: KClass<*>): List<FileSpec> {
        val inputClasses = kClass.declaredMemberProperties.map { it.returnType.toString() }
        val outputClasses = inputParameters(kClass)
            .map { it.type.toString() }

        val wrapInputClasses = wrapInputClasses(inputClasses)
        val wrapOutputClasses = wrapOutputClasses(outputClasses)
        return wrapInputClasses + wrapOutputClasses + wrapperClass(kClass, wrapInputClasses, wrapOutputClasses)
    }

    private fun inputParameters(kClass: KClass<*>): List<KParameter> {
        return kClass.companionObject?.declaredMemberFunctions.orEmpty()
            .firstOrNull { it.name == "instance" }
            ?.parameters.orEmpty()
            .filter { it.kind == KParameter.Kind.VALUE }
    }

    private fun wrapperClass(
        kClass: KClass<*>,
        inputFileSpecs: List<FileSpec>,
        outputFileSpecs: List<FileSpec>
    ): FileSpec {
        val prefix = originalFunctionCallWrapper.prefix
        val instanceName = kClass.simpleName!!.decapitalize()
        val wrapperClass = TypeSpec.classBuilder(prefix + kClass.simpleName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(KModifier.INTERNAL)
                    .addParameter(instanceName, kClass.asClassName())
                    .additionConstructorProperties()
                    .build()
            )
            .addProperties(properties(kClass, instanceName, inputFileSpecs))
            .addType(compainion(kClass, outputFileSpecs))
            .build()
        return FileSpec.builder(kClass.java.packageName, wrapperClass.name!!)
            .addType(wrapperClass)
            .build()
    }

    private fun FunSpec.Builder.additionConstructorProperties(): FunSpec.Builder {
        return originalFunctionCallWrapper.extraProperties.entries.fold(this) { builder, property ->
            builder.addParameter(property.key, property.value)
        }
    }

    private fun properties(
        kClass: KClass<*>,
        instanceName: String,
        inputFileSpecs: List<FileSpec>
    ): List<PropertySpec> {
        fun typeNameFor(param: KProperty<*>): ClassName {
            val first =
                inputFileSpecs.first { it.name.contains((param.returnType.asTypeName() as ClassName).simpleName) }
            return ClassName.bestGuess(first.packageName + "." + first.name)
        }
        return kClass.declaredMemberProperties.map { it }
            .map {
                val type = typeNameFor(it)
                val paramString =
                    originalFunctionCallWrapper.extraProperties.keys.fold("${instanceName}.${it.name}") { s, key ->
                        "$s, $key"
                    }
                PropertySpec.builder(it.name, type)
                    .initializer("%L(%L)", type.simpleName, paramString)
                    .build()
            }
    }

    private fun compainion(kClass: KClass<*>, outputFileSpecs: List<FileSpec>): TypeSpec {
        fun typeNameFor(param: KParameter): TypeName {
            return param.type.asTypeName()
        }

        val params = inputParameters(kClass).map {
            ParameterSpec.builder(
                it.name!!,
                typeNameFor(it)
            ).build()
        }
        val paramsString = "${kClass.simpleName}.instance(${params.joinToString(",") { it.name }})"
        val paramsStringWithAdditionProperties =
            originalFunctionCallWrapper.extraProperties.keys.fold(paramsString) { s, key -> "$s, $key" }
        return TypeSpec.companionObjectBuilder()
            .addFunction(
                FunSpec.builder("instance")
                    .addAnnotation(JvmStatic::class)
                    .addStatement(
                        "return %L(%L)",
                        originalFunctionCallWrapper.prefix + kClass.simpleName,
                        paramsStringWithAdditionProperties
                    )
                    .addParameters(params)
                    .additionConstructorProperties()
                    .build()
            )
            .build()
    }

    private fun <T : Any> List<T>?.orEmpty(): List<T> {
        return this ?: emptyList()
    }

    @JvmName("wrapInputClassStrings")
    private fun wrapInputClasses(list: List<String>): List<FileSpec> {
        return wrapInputClasses(list.map { classLoader.loadClass(it).kotlin })
    }

    private fun wrapInputClasses(list: List<KClass<*>>): List<FileSpec> {
        return list.map { wrapInputClass(it) }
    }

    @JvmName("wrapOutputClassStrings")
    private fun wrapOutputClasses(list: List<String>): List<FileSpec> {
        return wrapOutputClasses(list.map { classLoader.loadClass(it).kotlin })
    }

    private fun wrapOutputClasses(list: List<KClass<*>>): List<FileSpec> {
        return list.map { wrapOutputClass(it) }
    }

    private fun wrapInputClass(kClass: KClass<*>): FileSpec {
        val typeSpec = outputWrapperClassGenerator.wrapClass(kClass)
        return FileSpec.builder(kClass.java.packageName, typeSpec.name!!)
            .addType(typeSpec)
            .addImports(originalFunctionCallWrapper.imports)
            .build()
    }

    private fun wrapOutputClass(kClass: KClass<*>): FileSpec {
        val typeSpec = inputWrapperClassGenerator.wrapClass(kClass)
        return FileSpec.builder(kClass.java.packageName, typeSpec.name!!)
            .addType(typeSpec)
            .addImports(originalFunctionCallWrapper.imports)
            .build()
    }

    private fun FileSpec.Builder.addImports(map: Map<String, List<String>>): FileSpec.Builder {
        return map.entries.fold(this) { builder, entry -> builder.addImport(entry.key, entry.value) }
    }

    private fun FileSpec.Builder.addImports(imports: List<Import>): FileSpec.Builder {
        return imports.fold(this) { builder, import -> builder.addImport(import) }
    }
}
