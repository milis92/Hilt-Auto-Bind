<h1 align="center">
  <img src="dagger.svg" width="150px" />
<p>Hilt AutoBind</p>
</h1>

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=Release&metadataUrl=https://repo1.maven.org/maven2/com/github/milis92/hiltautobind/processor/maven-metadata.xml)](https://repo1.maven.org/maven2/com/github/milis92/hiltautobind/processor/)

KSP Processor that generates Hilt modules with minimal boilerplate

# How does it work

Hilt AutoBind has two main concepts:

1. `@AutoBind` annotation that you can use to bind a class to a specified supertype
2. `@AutoFactory` annotation that you can use to generate a Hilt module based on a simple factory function

# `@AutoBind`

Annotate a class with `@AutoBind` to generate a module with a binding for the specified supertype.

## Why AutoBind?

`@AutoBind` works similarly as `dagger.Inject`, with a difference being that the dagger with
`@Inject` binds a type explicitly to itself, while `@AutoBind` binds a type to a specified supertype if any.

For example:

#### With dagger:

 ```kotlin
 interface Something

@Singleton
class SomethingImpl @Inject constructor() : Something
 ```

___Dagger binds SomethingImpl to SomethingImpl, Something __cannot be used__ as an injected dependency___

#### With AutoBind:

 ```kotlin
 interface Something

@Singleton
@AutoBind
class SomethingImpl @Inject constructor() : Something
 ```

___Dagger binds SomethingImpl to Something, Something __can be used__ as an injected dependency___

## Usage

### Simple singleton binding

Simply annotate your class with `@AutoBind`

 ```kotlin
 interface Something

@Singleton
@AutoBind
class SomethingImpl @Inject constructor() : Something
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
 @Module
@InstallIn(SingletonComponent::class)
interface SomethingImpl_SingletonComponent_Module {
    @Binds
    @Singleton
    fun bindSomethingImpl(implementation: SomethingImpl): Something
}
  ```

### Specifying different Hilt component

You can specify a different Hilt component using the `component` parameter.

 ```kotlin
 @Singleton
@AutoBind(component = ActivityRetainedComponent::class)
class SomethingImpl @Inject constructor() : Something
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
 @Module
@InstallIn(ActivityRetainedComponent::class)
interface SomethingImpl_ActivityRetainedComponent_Module {
    @Binds
    @Singleton
    fun bindSomethingImpl(implementation: SomethingImpl): Something
}
 ```

### Multiple supertypes

If the class has multiple supertypes, the first super type will be used by default.
If you want to bind to a specific supertype, you can specify it using the `superType` parameter.
___Note that if the class has no super types, the annotation processor will bind a type to itself.___

 ```kotlin
 interface Something
interface Another

@Singleton
@AutoBind(superType = Another::class)
class SomethingImpl @Inject constructor() : Something, Another
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
 @Module
@InstallIn(SingletonComponent::class)
interface SomethingImpl_SingletonComponent_Module {
    @Binds
    @Singleton
    fun bindSomethingImpl(implementation: SomethingImpl): Another
}
 ```

### Multibindings

By default, the annotation processor binds a value as an instance of the specified supertype.
For dagger multibindings, you can bind a value to a set or a map of supertypes using the [`target`][AutoBindTarget]
parameter.

 ```kotlin
 interface Something
interface Another

@Singleton
@AutoBind(target = AutoBindTarget.SET) // or AutoBindTarget.MAP
class SomethingImpl @Inject constructor() : Something, Another
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
 @Module
@InstallIn(SingletonComponent::class)
interface SomethingImpl_SingletonComponent_Module {
    @Binds
    @IntoSet
    fun bindSomethingImpl(implementation: SomethingImpl): Something
 ```

### Visibility

The generated module will have the same visibility as the annotated class.

 ```kotlin
 interface Something

@AutoBind
internal class SomethingImpl : Something
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
 @Module
@InstallIn(SingletonComponent::class)
internal interface SomethingImpl_SingletonComponent_Module {
    @Binds
    fun bindSomethingImpl(implementation: SomethingImpl): Something
}
```

# `@AutoFactory`

Annotate a function with `@AutoFactory` to generate a Hilt module that binds a value of a function return type

## Why AutoFactory?

`@AutoFactory` works similarly as `[@dagger.Provides]`, with a difference being that the dagger with `@Provides`
requires a function to be wrapped in a module, with `@AutoFactory` that module is generated automatically.

#### With dagger:

 ```kotlin
class SomethingImpl(someInt: Int)

@Module
@InstallIn(SingletonComponent::class)
object ProvidesSomething_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    fun provideProvidesSomething(): Something {
        val calculateSomething = 1 + 1
        return SomethingImpl(calculateSomething)
    };
}
 ```

___Dagger requires a module to be created and a function to be annotated with `@Provides`___

#### With AutoFactory:

 ```kotlin
class SomethingImpl(someInt: Int)

@Singleton
@AutoFactory
fun SomethingFactory(): Something {
    val calculateSomething = 1 + 1
    return SomethingImpl(calculatesSomething)
}
 ```

___The annotation processor generates a module for the `SomethingImpl` class automatically___

## Usage

### Simple singleton binding

Simply annotate your function with `AutoFactory`

 ```kotlin
 interface Something

@Singleton
@AutoFactory
fun ProvidesSomethingFactory(): Something = SomethingImpl(someString)

class SomethingImpl(string: String) : Something
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    public fun provideProvidesSomething(): Something = ProvidesSomethingFactory();
 ```

### Specifying different Hilt component

You can specify a different Hilt component using the `component` parameter.

 ```kotlin
 interface Something

@Singleton
@AutoFactory(component = ActivityRetainedComponent::class)
fun ProvidesSomethingFactory(): Something = SomethingImpl(someString)

class SomethingImpl(string: String) : Something
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
public object ProvidesSomething_ActivityRetainedComponent_AutoFactoryModule {
    @Provides
    @Singleton
    public fun provideProvidesSomething(): Something = ProvidesSomethingFactory();
 ```

### Multibindings

By default, the annotation processor generates a module that provides a single instance of annotated
function return type.
For dagger multibindings, you can bind a value to a set or a map of supertypes using
the `AutoFactoryTarget` parameter.

#### Set or map multibindings

 ```kotlin
interface Something

@Singleton
@AutoFactory(target = AutoFactoryTarget.SET) // or AutoFactoryTarget.MAP
fun SomethingFactory(): Something = object : Something {}
  ```

The annotation processor will generate a following Hilt module

 ```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object Something_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    @IntoSet
    public fun provideSomething(): Something = SomethingFactory();
}
 ```

#### Elements into set

For providing a set of values, that should be bound to a set of a super type, use `AutoFactoryTarget.SET_VALUES`

 ```kotlin
interface Something

@Singleton
@AutoFactory(target = AutoFactoryTarget.SET_VALUES)
fun SomethingFactory(): Set<Something> = setOf(object : Something {})
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object Something_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    @ElementsIntoSet
    public fun provideSomething(): Set<Something> = SomethingFactory();
}
 ```

#### Multibindings set provider

With dagger multibindings, you do not have to explicitly provide a set/map container if the bound set/map is
going to have at least one element. If the resulting set could be empty set container needs to be provided.
Equivalent to `dagger.multibindings.Multibinds` you can provide a container for a set
of values using `AutoFactoryTarget.MULTIBINDING_CONTAINER`

 ```kotlin
interface Something

@Singleton
@AutoFactory(target = AutoFactoryTarget.MULTIBINDING_CONTAINER)
fun SomethingFactory(): Set<Something> = setOf()
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object Something_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    @Multibinds
    public fun provideSomething(): Set<Something> = SomethingFactory();
}
 ```

### Visibility

The generated module will have the same visibility as the annotated class.

 ```kotlin
interface Something

@AutoFactory
internal class SomethingImpl : Something
 ```

The annotation processor will generate a following Hilt module

 ```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object SomethingImpl_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    fun provideSomethingImpl(): Something = SomethingImpl();
}
 ```

# Testing

Both `AutoBind` and `AutoFactory` hava specific Test annotation counterpart that can be used to generate HiltTest
modules for testing purposes.
`TestAutoBind` and `TestAutoFactory` will automatically replace the original binding in the test environment.

# Setup

Add the following dependencies to your build.gradle file:

### With Gradle version catalogs

```toml
hilt-autobind = "version"

hilt-autobind = { module = "com.github.milis92.hiltautobind:runtime", version.ref = "hilt-autobind" }
hilt-autobind-processor = { module = "com.github.milis92.hiltautobind:processor", version.ref = "hilt-autobind" }
```

```kotlin
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}
dependencies {
    implementation(libs.hilt.autobind)
    ksp(libs.hilt.autobind.processor)
}
```

### Without Gradle version catalogs

```kotlin
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}
dependencies {
    implementation("com.github.milis92.hiltautobind:runtime:<version>")
    ksp("com.github.milis92.hiltautobind:processor:<version>")
}
```