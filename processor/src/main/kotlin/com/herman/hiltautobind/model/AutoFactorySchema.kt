package com.herman.hiltautobind.model

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.herman.hiltautobind.annotations.autofactory.AutoFactory
import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget
import com.herman.hiltautobind.annotations.autofactory.TestAutoFactory
import com.herman.hiltautobind.model.utils.HILT_ELEMENTS_INTO_SET_ANNOTATION
import com.herman.hiltautobind.model.utils.HILT_INTO_MAP_ANNOTATION
import com.herman.hiltautobind.model.utils.HILT_INTO_SET_ANNOTATION
import com.herman.hiltautobind.model.utils.HILT_MODULE_NAME_SEPARATOR
import com.herman.hiltautobind.model.utils.HILT_PROVIDES_ANNOTATION
import com.herman.hiltautobind.model.utils.HILT_QUALIFIER_ANNOTATIONS
import com.herman.hiltautobind.model.utils.HILT_SINGLETON_COMPONENT
import com.herman.hiltautobind.model.utils.argumentTypeName
import com.herman.hiltautobind.model.utils.findNestedAnnotationWithMarker
import com.herman.hiltautobind.model.utils.getStableContentHash
import com.herman.hiltautobind.model.utils.toClassName
import com.herman.hiltautobind.model.utils.toParameterSpec
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toTypeName

@Suppress("LongParameterList")
class AutoFactorySchema(
    originalDeclaration: KSDeclaration,
) : HiltAutoBindSchema {

    override val containingFile: KSFile =
        requireNotNull(originalDeclaration.containingFile) {
            "Containing file for ${originalDeclaration.simpleName} cannot be null"
        }

    val parentClass: KSClassDeclaration? =
        originalDeclaration.parentDeclaration as? KSClassDeclaration

    private val annotatedFunction: KSFunctionDeclaration =
        requireNotNull(originalDeclaration as KSFunctionDeclaration) {
            "${originalDeclaration.simpleName} must be a Function"
        }

    private val autoFactoryAnnotation: KSAnnotation =
        requireNotNull(
            annotatedFunction.annotations.firstOrNull { annotations ->
                annotations.annotationType.toTypeName() in listOf(AUTO_FACTORY_ANNOTATION, TEST_AUTO_FACTORY_ANNOTATION)
            }
        ) {
            "No valid @AutoFactory or @TestAutoFactory annotation found on ${annotatedFunction.simpleName}"
        }

    /**
     * Holds the name of the factory function that is providing the actual instances for this module
     */
    val factorFunctionName =
        annotatedFunction.simpleName.asString()

    /**
     * Holds the parameters required for the factory function that is providing the actual instances for this module
     */
    val factoryFunctionParameters: List<ParameterSpec> =
        annotatedFunction.parameters.map(KSValueParameter::toParameterSpec)

    /**
     * Holds the return type of the factory function that is providing the actual instances for this module
     */
    val factoryFunctionReturnType =
        requireNotNull(annotatedFunction.returnType?.toTypeName()) {
            "Return type of ${annotatedFunction.simpleName} cannot be null"
        }

    init {
        val annotatedFunctionReturnType = factoryFunctionReturnType as? ParameterizedTypeName
        if (hiltMultibindingAnnotation == HILT_ELEMENTS_INTO_SET_ANNOTATION) {
            require(annotatedFunctionReturnType?.rawType == SET) {
                "Function annotated with @AutoFactory(target = AutoFactoryTarget.SET_VALUES) must return a Set"
            }
        }
    }

    // Hilt module `InstallIn` component.
    override val hiltComponent: TypeName
        get() = autoFactoryAnnotation.argumentTypeName(
            autoFactoryAnnotation.hiltComponentArgumentName
        ) ?: HILT_SINGLETON_COMPONENT

    // Hilt Module visibility.
    override val hiltModuleVisibility: Visibility =
        annotatedFunction.getVisibility()

    // Hilt Module type.
    override val hiltModuleType: HiltAutoBindSchema.HiltModuleType =
        HiltAutoBindSchema.HiltModuleType.OBJECT

    // Hilt Module class name.
    override val hiltModuleClassName: ClassName = ClassName(
        packageName = annotatedFunction.packageName.asString(),
        simpleNames = listOf(
            (if (isTestModule) HILT_TEST_MODULE_NAME_FORMAT else HILT_MODULE_NAME_FORMAT)
                .format(simpleHiltModuleName, hiltComponent.toClassName().simpleName)
        )
    )

    // Hilt Module provider function name.
    override val hiltFunctionName: String =
        "$PROVIDES_METHOD_NAME_PREFIX${factorFunctionName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }}"

    // Hilt Module provider function annotations
    override val hiltFunctionAnnotations: List<AnnotationSpec> = listOfNotNull(
        HILT_PROVIDES_ANNOTATION, hiltMultibindingAnnotation
    ) + annotatedFunction.annotations.filterNot { annotations ->
        annotations.annotationType.toTypeName() in listOf(AUTO_FACTORY_ANNOTATION, TEST_AUTO_FACTORY_ANNOTATION)
    }.map { it.toAnnotationSpec(true) }.toList()

    // Hilt Test install in module to be replaced
    override val hiltReplacesModuleName: ClassName? =
        if (isTestModule) {
            ClassName(
                packageName = annotatedFunction.packageName.asString(),
                simpleNames = listOf(
                    HILT_MODULE_NAME_FORMAT.format(simpleHiltModuleName, hiltComponent.toClassName().simpleName)
                )
            )
        } else {
            null
        }

    private val hiltMultibindingAnnotation: AnnotationSpec?
        get() = when (
            autoFactoryAnnotation.argumentTypeName(
                autoFactoryAnnotation.bindTargetArgumentName
            )?.toClassName()?.simpleName
        ) {
            AutoFactoryTarget.SET.name -> HILT_INTO_SET_ANNOTATION
            AutoFactoryTarget.MAP.name -> HILT_INTO_MAP_ANNOTATION
            AutoFactoryTarget.SET_VALUES.name -> HILT_ELEMENTS_INTO_SET_ANNOTATION
            AutoFactoryTarget.INSTANCE.name -> null
            else -> null
        }

    private val isTestModule: Boolean
        get() = autoFactoryAnnotation.annotationType.toTypeName() == TEST_AUTO_FACTORY_ANNOTATION

    private val simpleHiltModuleName: String
        get() {
            val boundTypeSimpleName =
                (
                    (factoryFunctionReturnType as? ParameterizedTypeName)?.rawType
                        ?: factoryFunctionReturnType.toClassName()
                    )
                    .simpleName

            val qualifier = annotatedFunction.findNestedAnnotationWithMarker(
                HILT_QUALIFIER_ANNOTATIONS,
                skip = setOf(autoFactoryAnnotation)
            )?.getStableContentHash() ?: ""

            return boundTypeSimpleName + qualifier
        }
    private val KSAnnotation.hiltComponentArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoFactory::class.asTypeName() -> AutoFactory::component.name
            TestAutoFactory::class.asTypeName() -> TestAutoFactory::component.name
            else -> error("Annotation $annotationType has no component argument")
        }

    private val KSAnnotation.bindTargetArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoFactory::class.asTypeName() -> AutoFactory::target.name
            TestAutoFactory::class.asTypeName() -> TestAutoFactory::target.name
            else -> error("Annotation $annotationType has no target argument")
        }

    companion object {
        private const val PROVIDES_METHOD_NAME_PREFIX =
            "provide"
        val AUTO_FACTORY_ANNOTATION =
            AutoFactory::class.asTypeName()
        val TEST_AUTO_FACTORY_ANNOTATION =
            TestAutoFactory::class.asTypeName()

        private const val HILT_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}AutoFactoryModule"
        private const val HILT_TEST_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}TestAutoFactoryModule"
    }
}
