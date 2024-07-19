package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.herman.hiltautobind.model.AutoFactorySchema
import com.squareup.kotlinpoet.ClassName

class AutoFactoryVisitor(
    private val logger: KSPLogger
) : HiltAutoBindSymbolVisitor<AutoFactorySchema>() {
    override fun collect(
        resolver: Resolver
    ): Map<ClassName, List<AutoFactorySchema>> = sequenceOf(
        AutoFactorySchema.AUTO_FACTORY_ANNOTATION,
        AutoFactorySchema.TEST_AUTO_FACTORY_ANNOTATION
    ).map { annotationType -> annotationType.canonicalName }
        .flatMap { annotation -> resolver.getSymbolsWithAnnotation(annotation) }
        .distinct()
        .map { symbol -> symbol.accept(this, Unit) }
        .filterNotNull()
        .groupBy { schema -> schema.hiltModuleName }

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
