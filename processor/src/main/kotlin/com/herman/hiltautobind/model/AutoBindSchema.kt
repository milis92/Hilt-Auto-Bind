package com.herman.hiltautobind.model

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.herman.hiltautobind.annotations.autobind.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

class AutoBindSchema private constructor(
    override val containingFile: KSFile,
    override val originalDeclaration: KSDeclaration,
) : HiltAutoBindSchema {

    constructor(classDeclaration: KSClassDeclaration) : this(
        containingFile = requireNotNull(classDeclaration.containingFile) {
            "'${classDeclaration.qualifiedName}' class declaration is not contained in file"
        },
        originalDeclaration = classDeclaration,
    )

    val annotatedClass: KSClassDeclaration = originalDeclaration as KSClassDeclaration

    // Read the superType from the annotation if the superType is not the default superType,
    // otherwise use the first superType of the annotated class
    val boundSuperType: TypeName
        get() = autoBindAnnotation.getArgumentClassNameIfNotDefault(
            autoBindAnnotation.getBoundSuperTypeArgumentName
        ) ?: annotatedClass.getFirstNonAnySuperType() ?: annotatedClass.toClassName()

    private val autoBindAnnotation: KSAnnotation = annotatedClass.annotations.first { annotations ->
        annotations.annotationType.toTypeName() in listOf(BIND_ANNOTATION, TEST_BIND_ANNOTATION)
    }

    override val hiltComponent: TypeName
        get() = autoBindAnnotation.getArgumentClassName(
            autoBindAnnotation.getHiltComponentArgumentName
        ) ?: HILT_SINGLETON_COMPONENT

    override val hiltModuleVisibility: Visibility = annotatedClass.getVisibility()

    override val hiltModuleType: HiltAutoBindSchema.HiltModuleType =
        HiltAutoBindSchema.HiltModuleType.INTERFACE

    private val hiltModuleClassSimpleName
        get() = (boundSuperType as? ParameterizedTypeName)?.rawType?.simpleName
            ?: boundSuperType.toClassName().simpleName

    override val hiltModuleName: TypeName
        get() = ClassName(
            packageName = containingFile.packageName.asString(),
            simpleNames = listOf(
                (if (isTestModule) HILT_TEST_MODULE_NAME_FORMAT else HILT_MODULE_NAME_FORMAT)
                    .format(hiltModuleClassSimpleName, hiltComponent.toClassName().simpleName)
            )
        )

    override val hiltReplacesModuleName: TypeName = ClassName(
        packageName = containingFile.packageName.asString(),
        simpleNames = listOf(
            HILT_MODULE_NAME_FORMAT.format(hiltModuleClassSimpleName, hiltComponent.toClassName().simpleName)
        )
    )

    override val hiltFunctionAnnotations: List<AnnotationSpec>
        get() = listOfNotNull(
            HILT_BINDS_ANNOTATION, hiltMultibindingAnnotation
        ) + annotatedClass.annotations.filterNot { annotations ->
            annotations.annotationType.toTypeName() in listOf(BIND_ANNOTATION, TEST_BIND_ANNOTATION)
        }.map { it.toAnnotationSpec(true) }.toList()

    override val hiltFunctionName: String = "$BIND_METHOD_NAME_PREFIX${annotatedClass.simpleName.asString()}"

    private val hiltMultibindingAnnotation: AnnotationSpec?
        get() = when (
            autoBindAnnotation.getArgumentClassName(
                autoBindAnnotation.getAutoBindTargetArgumentName
            )?.toClassName()?.simpleName
        ) {
            AutoBindTarget.INSTANCE.name -> null
            AutoBindTarget.SET.name -> HILT_INTO_SET_ANNOTATION
            AutoBindTarget.MAP.name -> HILT_INTO_MAP_ANNOTATION
            else -> null
        }

    override val isTestModule: Boolean = autoBindAnnotation.annotationType.toTypeName() == TEST_BIND_ANNOTATION

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

    private val KSAnnotation.getAutoBindTargetArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoBind::class.asTypeName() -> AutoBind::target.name
            TestAutoBind::class.asTypeName() -> TestAutoBind::target.name
            else -> error("Annotation $annotationType has no target argument")
        }

    companion object {
        private const val BIND_METHOD_NAME_PREFIX = "bind"

        val BIND_ANNOTATION = AutoBind::class.asTypeName()
        val TEST_BIND_ANNOTATION = TestAutoBind::class.asTypeName()

        private const val HILT_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}Module"
        private const val HILT_TEST_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}TestModule"
    }
}
