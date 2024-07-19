package com.herman.hiltautobind.generator

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.herman.hiltautobind.model.AutoFactorySchema
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier

class AutoFactoryModuleGenerator : HiltAutoBindModuleGenerator<AutoFactorySchema>() {
    override fun buildHiltProvideFunction(schema: AutoFactorySchema): FunSpec = with(schema) {
        FunSpec.builder(name = hiltFunctionName)
            .addAnnotations(hiltFunctionAnnotations)
            .addFactoryParameterIfClass(this)
            .addParameters(annotatedFunctionParameters)
            .returns(annotatedFunctionReturnType)
            .addCode(buildProvideMethodCode(this))
            .addModifiers(schema.hiltModuleVisibility.toKModifier() ?: KModifier.PUBLIC)
            .build()
    }

    private fun FunSpec.Builder.addFactoryParameterIfClass(schema: AutoFactorySchema): FunSpec.Builder =
        if (schema.enclosingElementKind?.classKind?.isClass == true) {
            schema.enclosingClassName?.let {
                addParameter(ParameterSpec.builder(FACTORY_PARAMETER_NAME, it).build())
            } ?: this
        } else {
            this
        }

    private fun buildProvideMethodCode(schema: AutoFactorySchema): CodeBlock = when {
        schema.enclosingElementKind?.classKind?.isClass == true ->
            CodeBlock.of(
                PROVIDES_RETURN_FORMAT_INSTANCE,
                schema.annotatedFunctionName,
                schema.formattedCallParameters
            )

        schema.enclosingElementKind?.classKind == ClassKind.OBJECT ->
            CodeBlock.of(
                PROVIDES_RETURN_FORMAT_OBJECT,
                schema.enclosingClassName,
                schema.annotatedFunctionName,
                schema.formattedCallParameters
            )

        else ->
            CodeBlock.of(
                PROVIDES_RETURN_FORMAT_FILE,
                schema.annotatedFunctionName,
                schema.formattedCallParameters
            )
    }

    private val AutoFactorySchema.enclosingElementKind
        get() = (annotatedFunction.parentDeclaration as? KSClassDeclaration)

    private val AutoFactorySchema.enclosingClassName
        get() = enclosingElementKind?.toClassName()

    private val ClassKind.isClass
        get() = this in listOf(
            ClassKind.INTERFACE,
            ClassKind.CLASS,
            ClassKind.ENUM_CLASS,
            ClassKind.ENUM_ENTRY
        )

    companion object {
        private const val FACTORY_PARAMETER_NAME = "factory"
        private const val PROVIDES_RETURN_FORMAT_INSTANCE = "return factory.%N(%L);"
        private const val PROVIDES_RETURN_FORMAT_OBJECT = "return %T.%N(%L);"
        private const val PROVIDES_RETURN_FORMAT_FILE = "return %N(%L);"
    }
}
