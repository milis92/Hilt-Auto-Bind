package com.herman.hiltautobind.generator

import com.google.devtools.ksp.symbol.ClassKind
import com.herman.hiltautobind.model.AutoFactorySchema
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier

class AutoFactoryModuleGenerator : HiltAutoBindModuleGenerator<AutoFactorySchema>() {

    override fun buildHiltProvideFunction(
        schema: AutoFactorySchema
    ): FunSpec = with(schema) {
        FunSpec.builder(name = hiltFunctionName)
            .addAnnotations(hiltFunctionAnnotations)
            .addParameters(factoryFunctionParameters + factoryProviderParameterIfContainerIsClass(schema))
            .returns(factoryFunctionReturnType)
            .addCode(buildProvideFunction(this))
            .addModifiers(schema.hiltModuleVisibility.toKModifier() ?: KModifier.PUBLIC)
            .build()
    }

    /**
     * If an annotated factory function is in a class, that same class needs to be part of the dependency graph
     * and available in the component that the factory function is going to be bound to.
     *
     * For example:
     * ```
     * class Container @Inject constructor() {
     *   @AutoFactory
     *   fun create(name: String): Something = object : Something {}
     * }
     * ```
     * will generate:
     * ```
     * @Module
     * @InstallIn(SingletonComponent::class)
     * public object Something_SingletonComponent_AutoFactoryModule {
     *   @Provides
     *   public fun provideCreate(name: String, factory: Container): Something = factory.create(name);
     * }
     * ```
     */
    private fun factoryProviderParameterIfContainerIsClass(
        schema: AutoFactorySchema
    ): List<ParameterSpec> =
        if (schema.parentClass?.classKind?.isClass == true) {
            listOf(
                ParameterSpec.builder(
                    FACTORY_PARAMETER_NAME,
                    schema.parentClass.toClassName()
                ).build()
            )
        } else {
            emptyList()
        }

    private fun buildProvideFunction(
        schema: AutoFactorySchema
    ): CodeBlock = when {
        schema.parentClass?.classKind?.isClass == true ->
            CodeBlock.of(
                PROVIDES_RETURN_FORMAT_INSTANCE,
                schema.factorFunctionName,
                schema.factoryFunctionParameters.joinToString(", ") { it.name }
            )

        schema.parentClass?.classKind == ClassKind.OBJECT ->
            CodeBlock.of(
                PROVIDES_RETURN_FORMAT_OBJECT,
                schema.parentClass.toClassName(),
                schema.factorFunctionName,
                schema.factoryFunctionParameters.joinToString(", ") { it.name }
            )

        else ->
            CodeBlock.of(
                PROVIDES_RETURN_FORMAT_FILE,
                schema.factorFunctionName,
                schema.factoryFunctionParameters.joinToString(", ") { it.name }
            )
    }

    private val ClassKind.isClass
        get() = this in listOf(
            ClassKind.INTERFACE,
            ClassKind.CLASS,
            ClassKind.ENUM_CLASS,
            ClassKind.ENUM_ENTRY
        )

    companion object {
        private const val FACTORY_PARAMETER_NAME =
            "factory"
        private const val PROVIDES_RETURN_FORMAT_INSTANCE =
            "return factory.%N(%L);"
        private const val PROVIDES_RETURN_FORMAT_OBJECT =
            "return %T.%N(%L);"
        private const val PROVIDES_RETURN_FORMAT_FILE =
            "return %N(%L);"
    }
}
