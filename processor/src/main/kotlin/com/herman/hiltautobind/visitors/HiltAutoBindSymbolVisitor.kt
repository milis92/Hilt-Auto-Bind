package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.herman.hiltautobind.model.HiltAutoBindSchema

/**
 * Abstract base class for symbol visitors that process HiltAutoBindSchema annotations.
 * Provides a framework for collecting schema objects used for code generation.
 */
abstract class HiltAutoBindSymbolVisitor<T : HiltAutoBindSchema> : KSDefaultVisitor<Unit, T?>() {
    /**
     * Collects a list of schema objects by analyzing symbols resolved with the provided resolver.
     * This method is used to scan and group specific annotated elements into modules,
     * facilitating further processing or code generation.
     *
     * @param resolver The instance used to resolve symbols and annotations
     *                 within the current context of Kotlin symbol processing.
     * @return A list of schema objects of a type [T]
     */
    abstract fun collect(resolver: Resolver): List<T>

    /**
     * Provides a default handler implementation for unknown or unhandled nodes encountered
     * during processing. This method is invoked when no specific visit method applies to
     * the provided node.
     *
     * @param node The [com.google.devtools.ksp.symbol.KSNode] being visited.
     *             Represents an element in the Kotlin symbol-processing model.
     * @param data An auxiliary parameter of type [Unit] that carries additional data during the visit.
     * @return A nullable instance of type [T], which can represent a default or fallback result.
     */
    final override fun defaultHandler(node: KSNode, data: Unit): T? = null
}
