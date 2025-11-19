package com.herman.hiltautobind.model.utils

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import dagger.Binds
import dagger.MapKey
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet

val HILT_SINGLETON_COMPONENT: ClassName =
    SingletonComponent::class.asClassName()

val HILT_PROVIDES_ANNOTATION: AnnotationSpec =
    AnnotationSpec.builder(Provides::class).build()

val HILT_BINDS_ANNOTATION: AnnotationSpec =
    AnnotationSpec.builder(Binds::class).build()

val HILT_INTO_SET_ANNOTATION: AnnotationSpec =
    AnnotationSpec.builder(IntoSet::class).build()

val HILT_INTO_MAP_ANNOTATION: AnnotationSpec =
    AnnotationSpec.builder(IntoMap::class).build()

val HILT_ELEMENTS_INTO_SET_ANNOTATION: AnnotationSpec =
    AnnotationSpec.builder(ElementsIntoSet::class).build()

val HILT_QUALIFIER_ANNOTATIONS =
    listOf(
        javax.inject.Qualifier::class,
        jakarta.inject.Qualifier::class
    )

val HILT_MAP_KEY_ANNOTATION =
    MapKey::class

const val HILT_MODULE_NAME_SEPARATOR = "_"
