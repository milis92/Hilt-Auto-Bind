package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.herman.hiltautobind.model.AutoBindSchema
import com.herman.hiltautobind.model.HiltAutoBindSchema
import com.squareup.kotlinpoet.ClassName

abstract class HiltAutoBindSymbolVisitor<T : HiltAutoBindSchema> : KSDefaultVisitor<Unit, T?>() {
    abstract fun collect(resolver: Resolver): Map<ClassName, List<T>>
    override fun defaultHandler(node: KSNode, data: Unit): T? = null
}
