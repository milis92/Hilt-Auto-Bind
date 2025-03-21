package com.herman.hiltautobind.model

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

val HILT_SINGLETON_COMPONENT = dagger.hilt.components.SingletonComponent::class.asClassName()

val HILT_PROVIDES_ANNOTATION = AnnotationSpec.builder(dagger.Provides::class).build()
val HILT_BINDS_ANNOTATION = AnnotationSpec.builder(dagger.Binds::class).build()

val HILT_INTO_SET_ANNOTATION = AnnotationSpec.builder(dagger.multibindings.IntoSet::class).build()
val HILT_INTO_MAP_ANNOTATION = AnnotationSpec.builder(dagger.multibindings.IntoMap::class).build()
val HILT_ELEMENTS_INTO_SET_ANNOTATION = AnnotationSpec.builder(dagger.multibindings.ElementsIntoSet::class).build()
val HILT_MULTIBINDS_ANNOTATION = AnnotationSpec.builder(dagger.multibindings.Multibinds::class).build()

const val HILT_MODULE_NAME_SEPARATOR = "_"

interface HiltAutoBindSchema {
    val containingFile: KSFile
    val originalDeclaration: KSDeclaration
    val hiltModuleType: HiltModuleType
    val hiltModuleName: TypeName
    val hiltModuleVisibility: Visibility
    val hiltComponent: TypeName
    val hiltFunctionAnnotations: List<AnnotationSpec>
    val hiltFunctionName: String
    val hiltReplacesModuleName: TypeName
    val isTestModule: Boolean

    enum class HiltModuleType {
        OBJECT,
        INTERFACE
    }
}

fun KSAnnotation.getArgument(
    name: String
): Any? = arguments.find { it.name?.asString() == name }?.value

fun KSAnnotation.getDefaultArgument(
    name: String
): Any? = defaultArguments.find { it.name?.asString() == name }?.value

fun KSAnnotation.getArgumentClassName(
    name: String
): TypeName? = getArgument(name)?.let { (it as? KSType)?.toTypeName() }

fun KSAnnotation.getDefaultArgumentClassName(
    name: String
): TypeName? = getDefaultArgument(name)?.let { (it as? KSType)?.toTypeName() }

fun KSAnnotation.getArgumentClassNameIfNotDefault(
    name: String
): TypeName? = getArgumentClassName(name)?.takeIf { it != getDefaultArgumentClassName(name) }

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

fun TypeName.toClassName(): ClassName {
    (this as? ClassName)?.let { return it } ?: return ClassName.bestGuess(this.toString())
}