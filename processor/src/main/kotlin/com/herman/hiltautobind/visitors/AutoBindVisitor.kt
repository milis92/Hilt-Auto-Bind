package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.herman.hiltautobind.model.AutoBindSchema
import com.squareup.kotlinpoet.ClassName

/**
 * A visitor implementation for processing declarations annotated with specific binding annotations
 * in the context of Hilt dependency injection. This visitor generates instances of [AutoBindSchema],
 * which represent metadata required for automatic binding in Hilt modules.
 *
 * To add support for additional declarations, override the visit method for the desired declaration type
 * and map it to the AutoBindSchema.
 */
class AutoBindVisitor(
    private val logger: KSPLogger
) : HiltAutoBindSymbolVisitor<AutoBindSchema>() {

    private val bindAnnotations = listOf(
        AutoBindSchema.BIND_ANNOTATION,
        AutoBindSchema.TEST_BIND_ANNOTATION
    )

    /**
     * Visits a class declaration and attempts to generate an instance of [AutoBindSchema]
     * based on the provided declaration.
     *
     * @param classDeclaration The class declaration being visited.
     * @param data Additional data passed to the visitor (unused in this implementation).
     * @return An instance of [AutoBindSchema] if the class declaration is valid, or `null` if an error occurs.
     */
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

    override fun collect(
        resolver: Resolver
    ): List<AutoBindSchema> =
        bindAnnotations.asSequence()
            .flatMap { annotation: ClassName ->
                resolver.getSymbolsWithAnnotation(annotation.canonicalName)
            }
            .distinct()
            .mapNotNull { symbol: KSAnnotated ->
                symbol.accept(this, Unit)
            }.toList()
}
