package com.herman.hiltautobind.model

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*

const val HILT_MODULE_NAME_SEPARATOR = "_"

interface HiltAutoBindSchema {
    val containingFile: KSFile
    val autoBindAnnotation: KSAnnotation
    val otherAnnotations: List<AnnotationSpec>
    val hiltModuleType: HiltModuleType
    val hiltModuleName: ClassName
    val hiltModuleVisibility: Visibility
    val hiltComponent: ClassName
    val hiltFunctionName: String
    val hiltReplacesModuleName: ClassName
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
