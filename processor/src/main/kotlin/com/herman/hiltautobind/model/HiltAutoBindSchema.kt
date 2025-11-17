package com.herman.hiltautobind.model

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import dagger.Binds
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import javax.inject.Qualifier

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
