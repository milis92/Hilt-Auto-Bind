package com.herman.hiltautobind.model

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.herman.hiltautobind.annotations.autobind.*
import com.herman.hiltautobind.model.utils.HILT_BINDS_ANNOTATION
import com.herman.hiltautobind.model.utils.HILT_INTO_MAP_ANNOTATION
import com.herman.hiltautobind.model.utils.HILT_INTO_SET_ANNOTATION
import com.herman.hiltautobind.model.utils.HILT_MODULE_NAME_SEPARATOR
import com.herman.hiltautobind.model.utils.HILT_QUALIFIER_ANNOTATIONS
import com.herman.hiltautobind.model.utils.HILT_SINGLETON_COMPONENT
import com.herman.hiltautobind.model.utils.argumentTypeName
import com.herman.hiltautobind.model.utils.argumentTypeNameIfNotDefault
import com.herman.hiltautobind.model.utils.getFirstNonAnySuperType
import com.herman.hiltautobind.model.utils.findNestedAnnotationWithMarker
import com.herman.hiltautobind.model.utils.getStableContentHash
import com.herman.hiltautobind.model.utils.toClassName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

class AutoBindSchema(
    originalDeclaration: KSDeclaration,
) : HiltAutoBindSchema {

    override val containingFile: KSFile =
        requireNotNull(originalDeclaration.containingFile) {
            "Containing file for ${originalDeclaration.simpleName} cannot be null"
        }

    private val annotatedClass: KSClassDeclaration =
        requireNotNull(originalDeclaration as? KSClassDeclaration) {
            "${originalDeclaration.simpleName} must be a Class"
        }

    private val autoBindAnnotation: KSAnnotation =
        requireNotNull(
            annotatedClass.annotations.firstOrNull { annotations ->
                annotations.annotationType.toTypeName() in listOf(BIND_ANNOTATION, TEST_BIND_ANNOTATION)
            }
        ) {
            "No valid @AutoBind or @TestAutoBind annotation found on class ${annotatedClass.simpleName}"
        }

    /**
     * Resolved type of the class providing the implementation for the [boundType]
     *
     * ```
     * // boundType
     * interface BoundType
     *
     * // implementationType
     * object BoundTypeImplementation: BoundType
     *
     * @Binds
     * fun binds(implementationType: BoundTypeImplementation) : BoundType
     * ```
     */
    val implementationType: ClassName =
        annotatedClass.toClassName()

    /**
     * Resolved type of the class of a superType of [implementationType]
     *
     * ```
     * // boundType
     * interface BoundType
     *
     * // implementationType
     * object BoundTypeImplementation: BoundType
     *
     * @Binds
     * fun binds(implementationType: BoundTypeImplementation) : BoundType
     * ```
     *
     * This value is resolved by determining the appropriate type to be used in the binding process.
     * The resolution process involves the following steps:
     *
     * 1. Attempt to retrieve the type specified by the `autoBindAnnotation` using its argument name.
     *    This occurs only if the argument type is not set to its default value.
     * 2. If no type is specified, or it is set to the default, determine the first parent type of
     *    the `annotatedClass` that is not `Any`.
     * 3. If no non-default type or non-`Any` supertype is found, fallback to using the class
     *    name of the `annotatedClass`, which essentially behaves like regular constructor injection.
     */
    val boundType: TypeName =
        autoBindAnnotation.argumentTypeNameIfNotDefault(
            autoBindAnnotation.superTypeArgumentName
        ) ?: annotatedClass.getFirstNonAnySuperType() ?: annotatedClass.toClassName()

    // Hilt module `InstallIn` component.
    override val hiltComponent: TypeName
        get() = autoBindAnnotation.argumentTypeName(
            autoBindAnnotation.hiltComponentArgumentName
        ) ?: HILT_SINGLETON_COMPONENT

    // Hilt Module visibility.
    override val hiltModuleVisibility: Visibility =
        annotatedClass.getVisibility()

    // Hilt Module type.
    override val hiltModuleType: HiltAutoBindSchema.HiltModuleType =
        HiltAutoBindSchema.HiltModuleType.INTERFACE

    // Hilt Module class name.
    override val hiltModuleClassName: ClassName =
        ClassName(
            packageName = boundType.toClassName().packageName,
            simpleNames = listOf(
                (if (isTestModule) HILT_TEST_MODULE_NAME_FORMAT else HILT_MODULE_NAME_FORMAT)
                    .format(simpleHiltModuleName, hiltComponent.toClassName().simpleName)
            )
        )

    // Hilt Module provider function name.
    override val hiltFunctionName: String =
        "$BIND_METHOD_NAME_PREFIX${annotatedClass.simpleName.asString()}"

    // Hilt Module provider function annotations
    override val hiltFunctionAnnotations: List<AnnotationSpec> =
        listOfNotNull(HILT_BINDS_ANNOTATION, hiltMultibindingAnnotation) +
            annotatedClass.annotations.filterNot { annotations ->
                annotations.annotationType.toTypeName() in listOf(BIND_ANNOTATION, TEST_BIND_ANNOTATION)
            }.map { it.toAnnotationSpec(true) }.toList()

    // Hilt Test install in module to be replaced
    override val hiltReplacesModuleName: ClassName? =
        if (isTestModule) {
            ClassName(
                packageName = boundType.toClassName().packageName,
                simpleNames = listOf(
                    HILT_MODULE_NAME_FORMAT.format(
                        simpleHiltModuleName,
                        hiltComponent.toClassName().simpleName
                    )
                )
            )
        } else {
            null
        }

    private val isTestModule: Boolean
        get() = autoBindAnnotation.annotationType.toTypeName() == TEST_BIND_ANNOTATION

    private val simpleHiltModuleName: String
        get() {
            val boundTypeSimpleName = ((boundType as? ParameterizedTypeName)?.rawType ?: boundType.toClassName())
                .simpleName

            val qualifier = annotatedClass.findNestedAnnotationWithMarker(
                HILT_QUALIFIER_ANNOTATIONS,
                skip = setOf(autoBindAnnotation)
            )?.getStableContentHash() ?: ""

            return boundTypeSimpleName + qualifier
        }

    private val hiltMultibindingAnnotation: AnnotationSpec?
        get() = when (
            autoBindAnnotation.argumentTypeName(
                autoBindAnnotation.bindTargetArgumentName
            )?.toClassName()?.simpleName
        ) {
            AutoBindTarget.SET.name -> HILT_INTO_SET_ANNOTATION
            AutoBindTarget.MAP.name -> HILT_INTO_MAP_ANNOTATION
            AutoBindTarget.INSTANCE.name -> null
            else -> null
        }

    private val KSAnnotation.superTypeArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoBind::class.asTypeName() -> AutoBind::superType.name
            TestAutoBind::class.asTypeName() -> TestAutoBind::superType.name
            else -> error("Annotation $annotationType has no superType argument")
        }

    private val KSAnnotation.hiltComponentArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoBind::class.asTypeName() -> AutoBind::component.name
            TestAutoBind::class.asTypeName() -> TestAutoBind::component.name
            else -> error("Annotation $annotationType has no component argument")
        }

    private val KSAnnotation.bindTargetArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoBind::class.asTypeName() -> AutoBind::target.name
            TestAutoBind::class.asTypeName() -> TestAutoBind::target.name
            else -> error("Annotation $annotationType has no target argument")
        }

    companion object {
        private const val BIND_METHOD_NAME_PREFIX =
            "bind"

        val BIND_ANNOTATION =
            AutoBind::class.asTypeName()

        val TEST_BIND_ANNOTATION =
            TestAutoBind::class.asTypeName()

        private const val HILT_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}Module"
        private const val HILT_TEST_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}TestModule"
    }
}
