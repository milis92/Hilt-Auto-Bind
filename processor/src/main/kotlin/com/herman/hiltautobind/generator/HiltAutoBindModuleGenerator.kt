package com.herman.hiltautobind.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.herman.hiltautobind.model.HiltAutoBindSchema
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.writeTo
import dagger.hilt.InstallIn

abstract class HiltAutoBindModuleGenerator<T : HiltAutoBindSchema> {
    fun generate(codeGenerator: CodeGenerator, schema: T) {
        FileSpec.builder(className = schema.hiltModuleName)
            .addType(buildHiltModule(schema))
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
                originatingKSFiles = listOf(schema.containingFile)
            )
    }

    private fun buildHiltModule(schema: T): TypeSpec = when (schema.hiltModuleType) {
        HiltAutoBindSchema.HiltModuleType.OBJECT ->
            TypeSpec.objectBuilder(className = schema.hiltModuleName)
        HiltAutoBindSchema.HiltModuleType.INTERFACE ->
            TypeSpec.interfaceBuilder(className = schema.hiltModuleName)
    }.addAnnotation(daggerModuleClassName)
        .addAnnotation(getInstallInAnnotationSpec(schema))
        .addFunction(buildHiltProvideFunction(schema))
        .addModifiers(schema.hiltModuleVisibility.toKModifier() ?: KModifier.PUBLIC)
        .build()

    private fun getInstallInAnnotationSpec(schema: T): AnnotationSpec =
        if (schema.isTestModule) {
            AnnotationSpec.builder(testInstallInClassName)
                .addMember("components = [%T::class]", schema.hiltComponent)
                .addMember("replaces = [%T::class]", schema.hiltReplacesModuleName)
                .build()
        } else {
            AnnotationSpec.builder(installInClassName)
                .addMember("%T::class", schema.hiltComponent)
                .build()
        }

    abstract fun buildHiltProvideFunction(schema: T): FunSpec

    companion object {
        private val daggerModuleClassName = dagger.Module::class.asClassName()
        private val installInClassName = InstallIn::class.asClassName()
        private val testInstallInClassName = ClassName(packageName = "dagger.hilt.testing", "TestInstallIn")
    }
}
