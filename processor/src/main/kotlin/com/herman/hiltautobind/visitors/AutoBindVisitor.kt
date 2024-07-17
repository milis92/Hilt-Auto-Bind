package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.herman.hiltautobind.model.AutoBindSchema

class AutoBindVisitor(
    private val logger: KSPLogger
) : HiltAutoBindSymbolVisitor<AutoBindSchema>() {
    override fun collect(
        resolver: Resolver
    ): List<AutoBindSchema> = AutoBindSchema.bindAnnotations.asSequence()
        .mapNotNull { annotationType -> annotationType.qualifiedName }
        .flatMap { annotation -> resolver.getSymbolsWithAnnotation(annotation) }
        .distinct()
        .map { symbol -> symbol.accept(this, Unit) }
        .filterNotNull()
        .toList()

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ): AutoBindSchema? = try {
        AutoBindSchema(classDeclaration)
    } catch (ignored: Exception) {
        logger.error(ignored.message.orEmpty(), classDeclaration)
        logger.exception(ignored)
        null
    }
}