package com.herman.hiltautobind.kotlinpoet

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Converts a [KSValueParameter] to a [ParameterSpec].
 *
 * This function also adds explicit [JvmSuppressWildcards] annotation to the type arguments of the parameterized type,
 * because KotlinPoet looses the annotations on the type arguments when converting from KSP to KotlinPoet.
 *
 * https://github.com/square/kotlinpoet/issues/1946
 */
fun KSValueParameter.toParameterSpec(): ParameterSpec {
    val typeName = type.toTypeName()
    val parameterisedTypeWithSuppressWildcard = (typeName as? ParameterizedTypeName)?.let { parameterizedTypeName ->
        val typeWithSuppressWildcards = parameterizedTypeName.typeArguments.map { typeArgument ->
            typeArgument.copy(
                annotations = typeArgument.annotations + AnnotationSpec.builder(JvmSuppressWildcards::class).build()
            )
        }
        parameterizedTypeName.copy(typeArguments = typeWithSuppressWildcards)
    } ?: typeName

    return ParameterSpec.builder(
        name = name?.asString().orEmpty(),
        type = parameterisedTypeWithSuppressWildcard,
    ).addAnnotations(
        annotations.map { it.toAnnotationSpec(true) }.toList()
    ).build()
}
