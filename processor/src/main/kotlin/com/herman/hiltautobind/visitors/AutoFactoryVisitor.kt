package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.herman.hiltautobind.model.AutoFactorySchema

class AutoFactoryVisitor(
    private val logger: KSPLogger
) : HiltAutoBindSymbolVisitor<AutoFactorySchema>() {
    override fun collect(
        resolver: Resolver
    ): List<AutoFactorySchema> = AutoFactorySchema.autoFactoryAnnotations.asSequence()
        .mapNotNull { annotationType -> annotationType.qualifiedName }
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
