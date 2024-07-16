package com.herman.hiltautobind

import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

class TypesCollection(
    private val types: List<KClass<*>>
) : List<KClass<*>> by types {

    private val classNames = types.map { it.asClassName() }

    private val simpleNames = types.map { it.simpleName }

    operator fun contains(annotation: KSAnnotation): Boolean =
        annotation.shortName.asString() in simpleNames &&
                annotation.annotationType.resolve().toClassName() in classNames

    companion object {
        fun of(vararg types: KClass<*>): TypesCollection = TypesCollection(types.toList())
    }
}