package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.herman.hiltautobind.model.AutoBindSchema
import com.herman.hiltautobind.visitors.HiltAutoBindSymbolVisitor
import com.herman.hiltautobind.model.AutoFactorySchema


class AutoFactoryVisitor(
    private val logger: KSPLogger
) : HiltAutoBindSymbolVisitor<AutoFactorySchema>() {

    // Annotations that should be picked up by this Visitor
    private val factoryAnnotations = listOf(
        AutoFactorySchema.AUTO_FACTORY_ANNOTATION,
        AutoFactorySchema.TEST_AUTO_FACTORY_ANNOTATION
    )

    /**
     * Visits a function declaration and attempts to generate an instance of [AutoFactorySchema]
     * based on the provided declaration.
     *
     * @param function The function declaration being visited.
     * @param data Additional data passed to the visitor (unused in this implementation).
     * @return An instance of [AutoFactorySchema] if the function declaration is valid or `null` if an error occurs.
     */
    override fun visitFunctionDeclaration(
        function: KSFunctionDeclaration,
        data: Unit
    ): AutoFactorySchema? = try {
        AutoFactorySchema(function)
    } catch (ignored: Exception) {
        logger.error(ignored.message.orEmpty(), function)
        logger.exception(ignored)
        null
    }

    override fun collect(
        resolver: Resolver
    ): List<AutoFactorySchema> =
        factoryAnnotations.asSequence()
            .flatMap { annotation ->
                resolver.getSymbolsWithAnnotation(annotation.canonicalName)
            }
            .distinct()
            .mapNotNull { symbol ->
                symbol.accept(this, Unit)
            }.toList()
}
