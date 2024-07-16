package com.herman.hiltautobind.model

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.herman.hiltautobind.AutoFactory
import com.herman.hiltautobind.TestAutoFactory
import com.herman.hiltautobind.TypesCollection
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

class AutoFactorySchema(
    override val containingFile: KSFile,
    val annotatedFunctionName: String,
    val annotatedFunctionParameters: List<ParameterSpec>,
    val annotatedFunctionReturnType: TypeName,
    val enclosingElement: KSDeclaration?,
    override val autoBindAnnotation: KSAnnotation,
    override val otherAnnotations: List<AnnotationSpec>,
    override val hiltModuleVisibility: Visibility,
) : HiltAutoBindSchema {

    constructor(functionDeclaration: KSFunctionDeclaration) : this(
        containingFile = requireNotNull(functionDeclaration.containingFile) {
            "'${functionDeclaration.qualifiedName}' function declaration is not contained in file"
        },
        annotatedFunctionName = functionDeclaration.simpleName.asString(),
        annotatedFunctionParameters = functionDeclaration.parameters.map { parameter ->
            ParameterSpec.builder(
                name = parameter.name?.asString().orEmpty(),
                type = parameter.type.toTypeName()
            ).addAnnotations(parameter.annotations.map { it.toAnnotationSpec() }.toList())
                .build()
        },
        annotatedFunctionReturnType = requireNotNull(functionDeclaration.returnType?.toTypeName()),
        enclosingElement = functionDeclaration.parentDeclaration,
        autoBindAnnotation = functionDeclaration.annotations.first { annotation ->
            annotation in autoFactoryAnnotations
        },
        otherAnnotations = functionDeclaration.annotations.filter { annotation ->
            annotation in autoFactoryAnnotations
        }.map { it.toAnnotationSpec() }.toList(),
        hiltModuleVisibility = functionDeclaration.getVisibility()
    )

    override val hiltComponent: ClassName
        get() = autoBindAnnotation.getArgument(autoBindAnnotation.getBoundComponentArgumentName)?.let {
            (it as? KSType)?.toClassName()
        } ?: dagger.hilt.components.SingletonComponent::class.asClassName()

    override val hiltModuleType: HiltAutoBindSchema.HiltModuleType =
        HiltAutoBindSchema.HiltModuleType.OBJECT

    override val hiltModuleName: ClassName = ClassName(
        packageName = containingFile.packageName.asString(),
        simpleNames = listOf(
            (if (isTestModule) HILT_TEST_MODULE_NAME_FORMAT else HILT_MODULE_NAME_FORMAT).format(
                annotatedFunctionName,
                hiltComponent.simpleName
            )
        )
    )

    override val hiltReplacesModuleName: ClassName
        get() = ClassName(
            packageName = containingFile.packageName.asString(),
            simpleNames = listOf(
                HILT_MODULE_NAME_FORMAT.format(
                    annotatedFunctionName,
                    hiltComponent.simpleName
                )
            )
        )

    override val isTestModule: Boolean
        get() = autoBindAnnotation.annotationType == TestAutoFactory::class

    private val KSAnnotation.getBoundComponentArgumentName: String
        get() = when (annotationType) {
            AutoFactory::class -> AutoFactory::component.name
            TestAutoFactory::class -> TestAutoFactory::component.name
            else -> error("Annotation $annotationType has no component argument")
        }

    override val hiltFunctionName: String
        get() = PROVIDES_METHOD_NAME_PREFIX + annotatedFunctionName

    val formattedCallParameters = annotatedFunctionParameters.joinToString { it.name }

    companion object {
        private const val PROVIDES_METHOD_NAME_PREFIX = "bind"

        private const val HILT_MODULE_NAME_FORMAT =
            "%s{$HILT_MODULE_NAME_SEPARATOR}%s{$HILT_MODULE_NAME_SEPARATOR}AutoFactoryModule"
        private const val HILT_TEST_MODULE_NAME_FORMAT =
            "%s{$HILT_MODULE_NAME_SEPARATOR}%s{$HILT_MODULE_NAME_SEPARATOR}TestAutoFactoryModule"
        val autoFactoryAnnotations = TypesCollection.of(AutoFactory::class, TestAutoFactory::class)
    }
}