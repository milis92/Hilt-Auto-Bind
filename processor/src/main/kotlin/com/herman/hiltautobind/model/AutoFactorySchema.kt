package com.herman.hiltautobind.model

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.herman.hiltautobind.annotations.autofactory.AutoFactory
import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget
import com.herman.hiltautobind.annotations.autofactory.TestAutoFactory
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

@Suppress("LongParameterList")
class AutoFactorySchema(
    override val containingFile: KSFile,
    override val originalDeclaration: KSDeclaration,
) : HiltAutoBindSchema {

    constructor(functionDeclaration: KSFunctionDeclaration) : this(
        containingFile = requireNotNull(functionDeclaration.containingFile) {
            "'${functionDeclaration.qualifiedName}' function declaration is not contained in file"
        },
        originalDeclaration = functionDeclaration,
    )

    init {
        val annotatedFunctionReturnType = annotatedFunctionReturnType as? ParameterizedTypeName
        if (hiltMultibindingAnnotation == HILT_ELEMENTS_INTO_SET_ANNOTATION) {
            require(annotatedFunctionReturnType?.rawType == SET::class.asTypeName()) {
                "Function annotated with @AutoFactory(target = AutoFactoryTarget.SET_VALUES) must return a Set"
            }
        } else if (hiltMultibindingAnnotation == HILT_MULTIBINDS_ANNOTATION) {
            require(
                annotatedFunctionReturnType?.rawType == SET::class.asTypeName() ||
                    annotatedFunctionReturnType?.rawType == MAP::class.asTypeName()
            ) {
                "Function annotated with @AutoFactory(target = AutoFactoryTarget.MULTIBINDING_CONTAINER)" +
                    " must return a Set or a Map"
            }
        }
    }

    val annotatedFunction: KSFunctionDeclaration
        get() = originalDeclaration as KSFunctionDeclaration

    val annotatedFunctionParameters = annotatedFunction.parameters.map(
        KSValueParameter::toParameterSpec
    )

    val annotatedFunctionName = annotatedFunction.simpleName.asString()

    val annotatedFunctionReturnType
        get() = requireNotNull(annotatedFunction.returnType?.toTypeName())

    private val autoFactoryAnnotation: KSAnnotation
        get() = annotatedFunction.annotations.first { annotations ->
            annotations.annotationType.toTypeName() in listOf(AUTO_FACTORY_ANNOTATION, TEST_AUTO_FACTORY_ANNOTATION)
        }

    override val hiltComponent: ClassName
        get() = autoFactoryAnnotation.getArgumentClassName(
            autoFactoryAnnotation.getHiltComponentArgumentName
        ) ?: HILT_SINGLETON_COMPONENT

    override val hiltModuleVisibility: Visibility = annotatedFunction.getVisibility()

    override val hiltModuleType: HiltAutoBindSchema.HiltModuleType =
        HiltAutoBindSchema.HiltModuleType.OBJECT

    private val hiltModuleClassSimpleName
        get() = annotatedFunction.returnType?.resolve()?.toClassName()?.simpleNames?.joinToString("")

    override val hiltModuleName: ClassName = ClassName(
        packageName = containingFile.packageName.asString(),
        simpleNames = listOf(
            (if (isTestModule) HILT_TEST_MODULE_NAME_FORMAT else HILT_MODULE_NAME_FORMAT)
                .format(hiltModuleClassSimpleName, hiltComponent.simpleName)
        )
    )

    override val hiltReplacesModuleName: ClassName
        get() = ClassName(
            packageName = containingFile.packageName.asString(),
            simpleNames = listOf(
                HILT_MODULE_NAME_FORMAT.format(hiltModuleClassSimpleName, hiltComponent.simpleName)
            )
        )

    override val hiltFunctionAnnotations: List<AnnotationSpec>
        get() = listOfNotNull(
            HILT_PROVIDES_ANNOTATION.takeIf { hiltMultibindingAnnotation != HILT_MULTIBINDS_ANNOTATION },
            hiltMultibindingAnnotation
        ) + annotatedFunction.annotations.filterNot { annotations ->
            annotations.annotationType.toTypeName() in listOf(AUTO_FACTORY_ANNOTATION, TEST_AUTO_FACTORY_ANNOTATION)
        }.map { it.toAnnotationSpec(true) }.toList()

    override val hiltFunctionName: String
        get() = PROVIDES_METHOD_NAME_PREFIX + annotatedFunctionName

    private val hiltMultibindingAnnotation: AnnotationSpec?
        get() = when (
            autoFactoryAnnotation.getArgumentClassName(
                autoFactoryAnnotation.getAutoBindTargetArgumentName
            )?.simpleName
        ) {
            AutoFactoryTarget.INSTANCE.name -> null
            AutoFactoryTarget.SET.name -> HILT_INTO_SET_ANNOTATION
            AutoFactoryTarget.MAP.name -> HILT_INTO_MAP_ANNOTATION
            AutoFactoryTarget.SET_VALUES.name -> HILT_ELEMENTS_INTO_SET_ANNOTATION
            AutoFactoryTarget.MULTIBINDING_CONTAINER.name -> HILT_MULTIBINDS_ANNOTATION
            else -> null
        }

    override val isTestModule: Boolean
        get() = autoFactoryAnnotation.annotationType.toTypeName() == TEST_AUTO_FACTORY_ANNOTATION

    private val KSAnnotation.getHiltComponentArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoFactory::class.asTypeName() -> AutoFactory::component.name
            TestAutoFactory::class.asTypeName() -> TestAutoFactory::component.name
            else -> error("Annotation $annotationType has no component argument")
        }

    private val KSAnnotation.getAutoBindTargetArgumentName: String
        get() = when (annotationType.toTypeName()) {
            AutoFactory::class.asTypeName() -> AutoFactory::target.name
            TestAutoFactory::class.asTypeName() -> TestAutoFactory::target.name
            else -> error("Annotation $annotationType has no target argument")
        }

    val formattedCallParameters = annotatedFunctionParameters.joinToString { it.name }

    companion object {
        private const val PROVIDES_METHOD_NAME_PREFIX = "provide"
        val AUTO_FACTORY_ANNOTATION = AutoFactory::class.asTypeName()
        val TEST_AUTO_FACTORY_ANNOTATION = TestAutoFactory::class.asTypeName()

        private const val HILT_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}AutoFactoryModule"
        private const val HILT_TEST_MODULE_NAME_FORMAT =
            "%s${HILT_MODULE_NAME_SEPARATOR}%s${HILT_MODULE_NAME_SEPARATOR}TestAutoFactoryModule"
    }
}
