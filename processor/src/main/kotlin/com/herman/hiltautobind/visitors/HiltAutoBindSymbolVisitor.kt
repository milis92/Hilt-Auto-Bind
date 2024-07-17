package com.herman.hiltautobind.visitors

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.herman.hiltautobind.model.HiltAutoBindSchema

abstract class HiltAutoBindSymbolVisitor <T: HiltAutoBindSchema> : KSDefaultVisitor<Unit, T?>() {
    abstract fun collect(resolver: Resolver): List<T>
    override fun defaultHandler(node: KSNode, data: Unit): T? = null
}