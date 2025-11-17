package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.herman.hiltautobind.visitors.HiltAutoBindSymbolVisitor
import com.herman.hiltautobind.model.AutoFactorySchema

class AutoFactoryVisitor(
    private val logger: KSPLogger
) : HiltAutoBindSymbolVisitor<AutoFactorySchema>() {
    override fun collect(
        resolver: Resolver
    ): List<AutoFactorySchema> = sequenceOf(
        AutoFactorySchema.AUTO_FACTORY_ANNOTATION,
        AutoFactorySchema.TEST_AUTO_FACTORY_ANNOTATION
    ).map { annotationType -> annotationType.canonicalName }
        .flatMap { annotation -> resolver.getSymbolsWithAnnotation(annotation) }
        .distinct()
        .map { symbol -> symbol.accept(this, Unit) }
        .filterNotNull()
        .toList()

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
}
