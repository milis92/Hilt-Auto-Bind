package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.herman.hiltautobind.model.AutoBindSchema
import com.herman.hiltautobind.model.AutoFactorySchema
import com.herman.hiltautobind.model.HiltAutoBindSchema

class HiltAutoBindSymbolVisitor(private val logger: KSPLogger) : KSDefaultVisitor<Unit, HiltAutoBindSchema?>() {

    fun collect(resolver: Resolver) = (AutoFactorySchema.autoFactoryAnnotations + AutoBindSchema.bindAnnotations)
        .asSequence()
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

    override fun defaultHandler(node: KSNode, data: Unit): HiltAutoBindSchema? = null
}