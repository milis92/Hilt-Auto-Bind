package com.herman.hiltautobind.model

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.herman.hiltautobind.AutoBind
import com.herman.hiltautobind.TestAutoBind
import com.herman.hiltautobind.TypesCollection
import com.herman.hiltautobind.kotlinpoet.toUnwrappedAnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

class AutoBindSchema private constructor(
    override val containingFile: KSFile,
    val annotatedClass: ClassName,
    override val autoBindAnnotation: KSAnnotation,
    override val otherAnnotations: List<AnnotationSpec>,
    override val hiltModuleVisibility: Visibility,
) : HiltAutoBindSchema {

    constructor(classDeclaration: KSClassDeclaration) : this(
        containingFile = requireNotNull(classDeclaration.containingFile) {
            "'${classDeclaration.qualifiedName}' class declaration is not contained in file"
        },
        annotatedClass = classDeclaration.toClassName(),
        autoBindAnnotation = classDeclaration.annotations.first { annotations ->
            annotations in bindAnnotations
        },
        otherAnnotations = classDeclaration.annotations.filterNot { annotations ->
            annotations in bindAnnotations
        }.map(KSAnnotation::toUnwrappedAnnotationSpec).toList(),
        hiltModuleVisibility = classDeclaration.getVisibility()
    )

    // Read the superType from the annotation if the superType is not the default superType,
    // otherwise use the first superType of the annotated class
    val boundSuperType: ClassName
        get() = autoBindAnnotation.getArgument(autoBindAnnotation.getBoundSuperTypeArgumentName)?.let {
            (it as? KSType)?.toClassName()
        }.takeIf {
            it != (autoBindAnnotation.getDefaultArgument(
                autoBindAnnotation.getBoundSuperTypeArgumentName
            ) as KSType).toClassName()
        } ?: annotatedClass

    override val hiltComponent: ClassName
        get() = autoBindAnnotation.getArgument(autoBindAnnotation.getHiltComponentArgumentName)?.let {
            (it as? KSType)?.toClassName()
        } ?: dagger.hilt.components.SingletonComponent::class.asClassName()

    override val hiltModuleType: HiltAutoBindSchema.HiltModuleType =
        HiltAutoBindSchema.HiltModuleType.INTERFACE

    override val hiltModuleName: ClassName = ClassName(
        packageName = containingFile.packageName.asString(),
        simpleNames = listOf(
            (if (isTestModule) HILT_TEST_MODULE_NAME_FORMAT else HILT_MODULE_NAME_FORMAT).format(
                boundSuperType.simpleName,
                hiltComponent.simpleName
            )
        )
    )

    override val hiltReplacesModuleName: ClassName
        get() = ClassName(
            packageName = containingFile.packageName.asString(),
            simpleNames = listOf(
                HILT_MODULE_NAME_FORMAT.format(boundSuperType.simpleName, hiltComponent.simpleName)
            )
        )

    override val isTestModule: Boolean
        get() = autoBindAnnotation.annotationType.toTypeName() == TestAutoBind::class.asTypeName()

    override val hiltFunctionName: String
        get() = "$BIND_METHOD_NAME_PREFIX${annotatedClass.simpleName}"

    private val KSAnnotation.getBoundSuperTypeArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoBind::class.asTypeName() -> AutoBind::superType.name
            TestAutoBind::class.asTypeName() -> TestAutoBind::superType.name
            else -> error("Annotation $annotationType has no superType argument")
        }

    private val KSAnnotation.getHiltComponentArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoBind::class.asTypeName() -> AutoBind::component.name
            TestAutoBind::class.asTypeName() -> TestAutoBind::component.name
            else -> error("Annotation $annotationType has no component argument")
        }

    companion object {
        private const val BIND_METHOD_NAME_PREFIX = "bind"
        val bindAnnotations = TypesCollection.of(AutoBind::class, TestAutoBind::class)

        private const val HILT_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}Module"
        private const val HILT_TEST_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}TestModule"
    }
}
