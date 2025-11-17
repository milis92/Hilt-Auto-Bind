package com.herman.hiltautobind.model

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*

interface HiltAutoBindSchema {
    val containingFile: KSFile
    val hiltModuleClassName: ClassName
    val hiltModuleType: HiltModuleType
    val hiltModuleVisibility: Visibility
    val hiltComponent: TypeName
    val hiltFunctionAnnotations: List<AnnotationSpec>
    val hiltFunctionName: String
    val hiltReplacesModuleName: ClassName?

    enum class HiltModuleType {
        OBJECT,
        INTERFACE
    }
}
