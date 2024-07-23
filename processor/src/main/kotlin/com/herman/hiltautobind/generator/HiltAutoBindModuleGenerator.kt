package com.herman.hiltautobind.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.Visibility
import com.herman.hiltautobind.model.HiltAutoBindSchema
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import dagger.hilt.InstallIn

abstract class HiltAutoBindModuleGenerator<T : HiltAutoBindSchema> {
    fun generate(
        codeGenerator: CodeGenerator,
        className: ClassName,
        schemas: List<T>
    ) = FileSpec.builder(className = className)
        .addType(buildHiltModuleClass(className, schemas))
        .addImportIfTest(schemas)
        .build()
        .writeTo(
            codeGenerator = codeGenerator,
            aggregating = true,
            originatingKSFiles = schemas.map {
                it.containingFile
            }
        )

    private fun buildHiltModuleClass(
        className: ClassName,
        schemas: List<T>
    ): TypeSpec = when (schemas.first().hiltModuleType) {
        HiltAutoBindSchema.HiltModuleType.OBJECT ->
            TypeSpec.objectBuilder(className = className)

        HiltAutoBindSchema.HiltModuleType.INTERFACE ->
            TypeSpec.interfaceBuilder(className = className)
    }.addAnnotation(daggerModuleClassName)
        .addAnnotation(getInstallInAnnotationSpec(schemas.first()))
        .addModifiers(
            schemas.any { it.hiltModuleVisibility == Visibility.INTERNAL }.let {
                if (it) KModifier.INTERNAL else KModifier.PUBLIC
            }
        )
        .addFunctions(schemas.map { buildHiltProvideFunction(it) })
        .build()

    private fun FileSpec.Builder.addImportIfTest(schemas: List<T>): FileSpec.Builder {
        schemas.forEach { schema ->
            if (schema.isTestModule) {
                addImport(schema.hiltReplacesModuleName.packageName, schema.hiltReplacesModuleName.simpleNames)
            }
        }
        return this
    }

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
