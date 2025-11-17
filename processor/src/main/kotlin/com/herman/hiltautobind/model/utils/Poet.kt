package com.herman.hiltautobind.model.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.reflect.KClass

fun KSAnnotation.rawArgumentValue(
    name: String
): Any? = arguments.find { it.name?.asString() == name }?.value

fun KSAnnotation.defaultArgumentRawValue(
    name: String
): Any? = defaultArguments.find { it.name?.asString() == name }?.value

fun KSAnnotation.argumentTypeName(
    name: String
): TypeName? = rawArgumentValue(name)?.let {
    when (it) {
        is KSType -> it.toTypeName()
        is KSClassDeclaration -> it.toClassName()
        else -> null
    }
}

fun KSAnnotation.defaultArgumentTypeName(
    name: String
): TypeName? = defaultArgumentRawValue(name)?.let {
    when (it) {
        is KSType -> it.toTypeName()
        is KSClassDeclaration -> it.toClassName()
        else -> null
    }
}

fun KSAnnotation.argumentTypeNameIfNotDefault(
    name: String
): TypeName? = argumentTypeName(name)?.takeIf { it != defaultArgumentTypeName(name) }

@OptIn(KspExperimental::class)
fun KSAnnotated.findNestedAnnotationWithMarker(
    markerKClass: List<KClass<out Annotation>>,
    skip: Set<KSAnnotation> = setOf()
): KSAnnotation? {
    val visited = skip.toMutableSet()
    return annotations.firstOrNull { annotation ->
        val annotationType = annotation.annotationType.resolve()
        val annotationDeclaration: KSDeclaration = annotationType.declaration

        if (!visited.add(annotation)) {
            false
        } else {
            // Check if the current annotation is marked with any of the markerKClass
            markerKClass.any { annotationDeclaration.getAnnotationsByType(it).any() } ||
                    // Recursively check all annotations on the current annotation
                    annotationDeclaration.findNestedAnnotationWithMarker(markerKClass, visited) != null
        }
    }
}

@OptIn(KspExperimental::class)
fun KSAnnotation.getStableContentHash(): String {
    val annotationFqn = annotationType.resolve().declaration.qualifiedName?.asString()
    val argsString = arguments
        .sortedBy { it.name?.asString() } // Sort arguments by name for consistency
        .joinToString(separator = ";") { arg ->
            "${arg.name?.asString()}:${getStableValueRepresentation(arg.value)}"
        }
    val stableIdentifier = "$annotationFqn($argsString)"
    return stableIdentifier.hashCode().toString()
}

// Helper function for stable value representation
private fun getStableValueRepresentation(value: Any?): String {
    return when (value) {
        is KSType -> value.declaration.qualifiedName?.asString() ?: value.toString()
        is List<*> -> value.joinToString(separator = ",") { getStableValueRepresentation(it) }
        else -> value.toString()
    }
}

fun KSClassDeclaration.getFirstNonAnySuperType(): TypeName? = superTypes.map {
    it.resolve().toTypeName()
}.firstOrNull { it != ANY }

fun KSValueParameter.toParameterSpec(): ParameterSpec {
    val typeName = type.toTypeName()
    return ParameterSpec.builder(
        name = name?.asString().orEmpty(),
        type = if (typeName is ParameterizedTypeName) {
            typeName.copy(
                typeArguments = typeName.typeArguments.map { it.jvmSuppressWildcards() }
            )
        } else {
            typeName
        },
    ).addAnnotations(
        annotations.map { it.toAnnotationSpec(true) }.toList()
    ).build()
}

fun TypeName.toClassName(): ClassName =
    (this as? ClassName) ?: ClassName.bestGuess(this.toString())