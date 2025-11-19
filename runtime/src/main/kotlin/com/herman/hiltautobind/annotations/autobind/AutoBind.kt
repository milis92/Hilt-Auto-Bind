package com.herman.hiltautobind.annotations.autobind

import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 * Represents the target for the generated [dagger.Binds] method.
 *
 * Dagger can bind a value to a single Type, or with multibindings, to a Set or a Map.
 *
 * @see <a href="https://dagger.dev/multibindings">Multibindings</a>
 */
enum class AutoBindTarget {
    /**
     * Bind a specified type explicitly
     */
    INSTANCE,

    /**
     * Bind a specified type to a set of super types
     * @see <a href="https://dagger.dev/multibindings#set-multibindings">Set multibinding</a>
     */
    SET,

    /**
     * Bind a specified type to a map of super types
     * @see <a href="https://dagger.dev/multibindings#map-multibindings">Map multibinding</a>
     */
    MAP,
}

/**
 * ___Annotate a class with `@AutoBind` to generate a Hilt module that binds a class to a specified supertype.___
 *
 * # Why AutoBind?
 * `@AutoBind` works similarly as [@dagger.Inject][javax.inject.Inject], with
 * a difference being that the dagger with `@Inject` binds a type explicitly to itself,
 * `@AutoBind` binds a type to a specified supertype if any.
 *
 * #### With dagger:
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * class SomethingImpl @Inject constructor(): Something
 * ```
 * ___Dagger binds SomethingImpl to SomethingImpl, Something __cannot be used__ as an injected dependency___
 *
 * #### With AutoBind:
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoBind
 * class SomethingImpl @Inject constructor(): Something
 * ```
 * ___Dagger binds SomethingImpl to Something, Something __can be used__ as an injected dependency___
 *
 * # Usage
 *
 * ## Simple singleton binding
 * Simply annotate your class with `@AutoBind`
 *
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoBind
 * class SomethingImpl @Inject constructor(): Something
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * interface SomethingImpl_SingletonComponent_Module {
 *   @Binds
 *   @Singleton
 *   fun bindSomethingImpl(implementation: SomethingImpl): Something
 * }
 *  ```
 *
 * ## Specifying different Hilt component
 * You can specify a different Hilt component using the `component` parameter.
 *
 * ```kotlin
 * @Singleton
 * @AutoBind(component = ActivityRetainedComponent::class)
 * class SomethingImpl @Inject constructor(): Something
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(ActivityRetainedComponent::class)
 * interface SomethingImpl_ActivityRetainedComponent_Module {
 *   @Binds
 *   @Singleton
 *   fun bindSomethingImpl(implementation: SomethingImpl): Something
 * }
 * ```
 *
 * ## Multiple supertypes
 * If the class has multiple supertypes, the first super type will be used by default.
 * If you want to bind to a specific supertype, you can specify it using the `superType` parameter.
 * ___Note that if the class has no super types, the annotation processor will bind a type to itself.___
 *
 * ```kotlin
 * interface Something
 * interface Another
 *
 * @Singleton
 * @AutoBind(superType = Another::class)
 * class SomethingImpl @Inject constructor(): Something, Another
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * interface SomethingImpl_SingletonComponent_Module {
 *   @Binds
 *   @Singleton
 *   fun bindSomethingImpl(implementation: SomethingImpl): Another
 * }
 * ```
 *
 * ## Multibindings
 * By default the annotation processor binds a value as an instance of the specified supertype.
 * For dagger multibindings, you can bind a value to a set or a map of supertypes using the
 * [`target`][AutoBindTarget] parameter.
 *
 * ```kotlin
 * interface Something
 * interface Another
 *
 * @Singleton
 * @AutoBind(target = AutoBindTarget.SET) // or AutoBindTarget.MAP
 * class SomethingImpl @Inject constructor(): Something, Another
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * interface SomethingImpl_SingletonComponent_Module {
 *  @Binds
 *  @IntoSet
 *  fun bindSomethingImpl(implementation: SomethingImpl): Something
 * ```
 *
 * ## Visibility
 * The generated module will have the same visibility as the annotated class.
 *
 * ```kotlin
 * interface Something
 *
 * @AutoBind
 * internal class SomethingImpl : Something
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * internal interface SomethingImpl_SingletonComponent_Module {
 *   @Binds
 *   fun bindSomethingImpl(implementation: SomethingImpl): Something
 * }
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class AutoBind(
    val component: KClass<*> = SingletonComponent::class,
    val superType: KClass<*> = Any::class,
    val target: AutoBindTarget = AutoBindTarget.INSTANCE,
    val uniqueKey: String = ""
)

/**
 * Same as [AutoBind] but for testing with Hilt
 *
 * # Usage
 *
 * The usage is the same as [AutoBind] but the generated module will be
 * annotated with [dagger.hilt.testing.TestInstallIn] instead of [dagger.hilt.InstallIn].
 *
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoBind
 * class SomethingImpl : Something
 * ```
 * And later in your test source
 * ```kotlin
 * @Singleton
 * @TestAutoBind
 * class SomethingStub : Something
 * ```
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @TestInstallIn(
 *   components = [SingletonComponent::class],
 *   replaces = [SomethingImpl_SingletonComponent_Module::class]
 * )
 * interface SomethingStub_SingletonComponent_Module {
 *   @Binds
 *   @Singleton
 *   fun bindSomethingImpl(implementation: SomethingStub): Something
 * }
 * ```
 * ___Note that `@TestAutoBind` will automatically replace the module generated by the [AutoBind]
 * annotation matched by a specified supertype___
 *
 * @see AutoBind
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@Repeatable
annotation class TestAutoBind(
    val component: KClass<*> = SingletonComponent::class,
    val superType: KClass<*> = Any::class,
    val target: AutoBindTarget = AutoBindTarget.INSTANCE,
    val uniqueKey: String = ""
)
