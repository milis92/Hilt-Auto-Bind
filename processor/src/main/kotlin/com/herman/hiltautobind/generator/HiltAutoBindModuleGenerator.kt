package com.herman.hiltautobind.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.Visibility
import com.herman.hiltautobind.model.HiltAutoBindSchema
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import dagger.hilt.InstallIn

abstract class HiltAutoBindModuleGenerator<in T : HiltAutoBindSchema> {
    fun generate(
        codeGenerator: CodeGenerator,
        schema: T,
    ) {
        FileSpec.builder(className = schema.hiltModuleClassName)
            .addType(buildHiltModuleClass(schema))
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
                originatingKSFiles = listOf(schema.containingFile)
            )
    }

    private fun buildHiltModuleClass(
        schema: T
    ): TypeSpec = when (schema.hiltModuleType) {
        HiltAutoBindSchema.HiltModuleType.OBJECT ->
            TypeSpec.objectBuilder(className = schema.hiltModuleClassName)

        HiltAutoBindSchema.HiltModuleType.INTERFACE ->
            TypeSpec.interfaceBuilder(className = schema.hiltModuleClassName)
    }
        .addAnnotation(DAGGER_MODULE)
        .addAnnotation(buildInstallInAnnotation(schema))
        .addModifiers(
            if (schema.hiltModuleVisibility == Visibility.INTERNAL) {
                KModifier.INTERNAL
            } else {
                KModifier.PUBLIC
            }
        )
        .addFunction(buildHiltProvideFunction(schema))
        .build()

    private fun buildInstallInAnnotation(schema: T): AnnotationSpec {
        val replaceModule = schema.hiltReplacesModuleName
        return if (replaceModule == null) {
            AnnotationSpec.builder(INSTALL_IN)
                .addMember("%T::class", schema.hiltComponent)
                .build()
        } else {
            AnnotationSpec.builder(TEST_INSTALL_IN)
                .addMember("components = [%T::class]", schema.hiltComponent)
                .addMember("replaces = [%T::class]", replaceModule)
                .build()
        }
    }

    protected abstract fun buildHiltProvideFunction(schema: T): FunSpec

    companion object {
        private val DAGGER_MODULE =
            dagger.Module::class.asClassName()
        private val INSTALL_IN =
            InstallIn::class.asClassName()
        private val TEST_INSTALL_IN =
            ClassName(packageName = "dagger.hilt.testing", "TestInstallIn")
    }
}
