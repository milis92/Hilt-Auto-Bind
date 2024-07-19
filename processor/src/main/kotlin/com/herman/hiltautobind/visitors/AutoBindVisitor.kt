package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.herman.hiltautobind.model.AutoBindSchema
import com.squareup.kotlinpoet.ClassName

class AutoBindVisitor(
    private val logger: KSPLogger
) : HiltAutoBindSymbolVisitor<AutoBindSchema>() {
    override fun collect(
        resolver: Resolver
    ): Map<ClassName, List<AutoBindSchema>> = sequenceOf(
        AutoBindSchema.BIND_ANNOTATION,
        AutoBindSchema.TEST_BIND_ANNOTATION
    ).map { annotationType -> annotationType.canonicalName }
        .flatMap { annotation -> resolver.getSymbolsWithAnnotation(annotation) }
        .distinct()
        .map { symbol -> symbol.accept(this, Unit) }
        .filterNotNull()
        .groupBy { schema -> schema.hiltModuleName }

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
