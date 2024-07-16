package com.herman.hiltautobind

import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class AutoBind(
    /**
     * Hilt component in which the binding should be installed.
     *
     * Must be a type annotated with `@DefineComponent`.
     *
     * If not defined, the binding will be installed in [SingletonComponent].
     */
    val component: KClass<*> = SingletonComponent::class,

    val superType: KClass<*> = Any::class
)


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class TestAutoBind(
    /**
     * Hilt component in which the binding should be installed.
     *
     * Must be a type annotated with `@DefineComponent`.
     *
     * If not defined, the binding will be installed in [SingletonComponent].
     */
    val component: KClass<*> = SingletonComponent::class,

    val superType: KClass<*> = Any::class
)


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class AutoFactory(

    /**
     * Hilt component in which the provider should be installed.
     *
     * Must be a type annotated with `@DefineComponent`.
     *
     * If not defined, the provider will be installed in [SingletonComponent].
     */
    val component: KClass<*> = SingletonComponent::class
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class TestAutoFactory(

    /**
     * Hilt component in which the provider should be installed.
     *
     * Must be a type annotated with `@DefineComponent`.
     *
     * If not defined, the provider will be installed in [SingletonComponent].
     */
    val component: KClass<*> = SingletonComponent::class
)
