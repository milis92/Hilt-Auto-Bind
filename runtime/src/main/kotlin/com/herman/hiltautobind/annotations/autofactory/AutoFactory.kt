package com.herman.hiltautobind.annotations.autofactory

import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 * Represents the target for the generated [dagger.Provides] method.
 *
 * Dagger can bind a value to a single Type, or with multibindings, to a Set or a Map.
 *
 * @see <a href="https://dagger.dev/multibindings">Multibindings</a>
 */
enum class AutoFactoryTarget {
    /**
     * Bind specified type explicitly
     */
    INSTANCE,

    /**
     * Bind specified type to a set of super type
     * @see <a href="https://dagger.dev/multibindings#set-multibindings">Set multibinding</a>
     */
    SET,

    /**
     * Bind specified type to a map of super types
     * @see <a href="https://dagger.dev/multibindings#map-multibindings">Map multibinding</a>
     */
    MAP,

    /**
     * Bind a set of specified types to a set of super types
     * Return type must be a Set
     * @see <a href="https://dagger.dev/multibindings#set-multibindings">Set multibinding</a>
     */
    SET_VALUES,

    /**
     * Binds a set container of the specified type
     * Return type must be a Set or a Map
     * @see <a href="https://dagger.dev/multibindings">Multibindings</a>
     */
    MULTIBINDING_CONTAINER
}

/**
 * ___Annotate a function with `@AutoFactory` to generate a Hilt module that binds a value of a function return type___
 *
 * # Why AutoFactory?
 * `@AutoFactory` works similarly as [@dagger.Provides][dagger.Provides], with a difference being that the
 * dagger with `@Provides` requires a function to be wrapped in a module,
 * with `@AutoFactory` that module is generated automatically.
 *
 * #### With dagger:
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object ProvidesSomething_SingletonComponent_AutoFactoryModule {
 *  @Provides
 *  @Singleton
 *  fun provideProvidesSomething(): Something {
 *    val calculateSomething = 1 + 1
 *    return SomethingImpl(calculateSomething)
 *  };
 *}
 *
 * class SomethingImpl(someInt: Int)
 * ```
 * ___Dagger requires a module to be created and a function to be annotated with `@Provides`___
 *
 * #### With AutoFactory:
 * ```kotlin
 * @Singleton
 * @AutoFactory
 * fun SomethingFactory(): Something {
 *  val calculateSomething = 1 + 1
 *  return SomethingImpl(calculatesSomething)
 * }
 * class SomethingImpl(someInt: Int)
 * ```
 * ___The annotation processor generates a module for the `SomethingImpl` class automatically___
 *
 * # Usage
 * ## Simple singleton binding
 * Simply annotate your function with `AutoFactory`
 *
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoFactory
 * fun ProvidesSomethingFactory(): Something = SomethingImpl(someString)
 *
 * class SomethingImpl(string: String) : Something
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
 *   @Provides
 *   @Singleton
 *   public fun provideProvidesSomething(): Something = ProvidesSomethingFactory();
 * ```
 *
 * ## Specifying different Hilt component
 * You can specify a different Hilt component using the `component` parameter.
 *
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoFactory(component = ActivityRetainedComponent::class)
 * fun ProvidesSomethingFactory(): Something = SomethingImpl(someString)
 *
 * class SomethingImpl(string: String) : Something
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(ActivityRetainedComponent::class)
 * public object ProvidesSomething_ActivityRetainedComponent_AutoFactoryModule {
 *   @Provides
 *   @Singleton
 *   public fun provideProvidesSomething(): Something = ProvidesSomethingFactory();
 * ```
 *
 * ## Multibindings
 * By default the annotation processor generates a module that provides a single instance of annotated
 * function return type.
 * For dagger multibindings, you can bind a value to a set or a map of supertypes using
 * the [`target`][AutoFactoryTarget] parameter.
 *
 * #### Set or map multibindings
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoFactory(target = AutoFactoryTarget.SET) // or AutoFactoryTarget.MAP
 * fun SomethingFactory(): Something = object : Something {}
 *```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * public object Something_SingletonComponent_AutoFactoryModule {
 *   @Provides
 *   @Singleton
 *   @IntoSet
 *   public fun provideSomething(): Something = SomethingFactory();
 * }
 * ```
 *
 * #### Elements into set
 * For providing a set of values, that should be bound to a set of a super type, use [AutoFactoryTarget.SET_VALUES]
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoFactory(target = AutoFactoryTarget.SET_VALUES)
 * fun SomethingFactory(): Set<Something> = setOf(object : Something {})
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * public object Something_SingletonComponent_AutoFactoryModule {
 *   @Provides
 *   @Singleton
 *   @ElementsIntoSet
 *   public fun provideSomething(): Set<Something> = SomethingFactory();
 * }
 * ```
 *
 * #### Multibindings set provider
 * With dagger multibindings, you do not have to explicitly provide a set/map container if the bound set/map is
 * going to have at least one element. If the resulting set could be empty set container needs to be provided.
 * Equivalent to [dagger.multibindings.Multibinds] you can provide a container for a set
 * of values using [AutoFactoryTarget.MULTIBINDING_CONTAINER]
 * ```kotlin
 * interface Something
 *
 * @Singleton
 * @AutoFactory(target = AutoFactoryTarget.MULTIBINDING_CONTAINER)
 * fun SomethingFactory(): Set<Something> = setOf()
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * public object Something_SingletonComponent_AutoFactoryModule {
 *   @Provides
 *   @Singleton
 *   @Multibinds
 *   public fun provideSomething(): Set<Something> = SomethingFactory();
 * }
 * ```
 *
 * ## Visibility
 * The generated module will have the same visibility as the annotated class.
 * ```kotlin
 * interface Something
 * @AutoFactory
 * internal class SomethingImpl : Something
 * ```
 *
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * internal object SomethingImpl_SingletonComponent_AutoFactoryModule {
 *   @Provides
 *   @Singleton
 *   fun provideSomethingImpl(): Something = SomethingImpl();
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class AutoFactory(
    val component: KClass<*> = SingletonComponent::class,
    val target: AutoFactoryTarget = AutoFactoryTarget.INSTANCE,
)

/**
 * Same as [AutoFactory] but for testing with Hilt
 *
 * # Usage
 *
 * The usage is the same as [AutoFactory] but the generated module will be annotated with
 * [dagger.hilt.testing.TestInstallIn] instead of [dagger.hilt.InstallIn].
 *
 * ```kotlin
 * interface Something
 *
 * @AutoFactory
 * fun SomethingFactory(): Something = object : Something {}
 * ```
 * And later in your test source
 * ```kotlin
 * @TestAutoFactory
 * fun SomethingFactoryStub(): Something = object : Something {}
 * ```
 * The annotation processor will generate a following Hilt module
 * ```kotlin
 * @Module
 * @TestInstallIn(
 *   components = [SingletonComponent::class],
 *   replaces = [SomethingImpl_SingletonComponent_Module::class]
 * )
 * public object Something_SingletonComponent_TestAutoFactoryModule {
 *   @Provides
 *   @Singleton
 *   fun provideSomething(): Something = SomethingFactoryStub();
 * }
 * ```
 * ___Note that `@TestAutoBind` will automatically replace the module generated by the [AutoFactory]
 * annotation matched by a specified supertype___
 *
 * @see AutoFactory
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class TestAutoFactory(
    val component: KClass<*> = SingletonComponent::class,
    val target: AutoFactoryTarget = AutoFactoryTarget.INSTANCE,
)
