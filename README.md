<h1 align="center">
  <img src="dagger.svg" width="150px" />
<p>Hilt AutoBind</p>
</h1>

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=Release&metadataUrl=https://repo1.maven.org/maven2/com/github/milis92/hiltautobind/processor/maven-metadata.xml)](https://repo1.maven.org/maven2/com/github/milis92/hiltautobind/processor/)

KSP Processor that streamlines working with Hilt by generating Hilt modules with minimal boilerplate.

# How does it work

Hilt AutoBind has two main concepts:

1. SuperType Binding that you can use to automatically bind a class to a supertype
2. Factory function that you can use to provide a dependency with a simple function

## Automatic Binding

`@AutoBind` is an annotation that can be used to automatically bind a class to a specific supertype in a Hilt module,
without having to manually write the entire module

Consider following example with Hilt without `AutoBind`:

```kotlin
import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

interface Foo
class FooImpl @Inject constructor() : Foo

@Module
@InstallIn(SingletonComponent::class)
abstract class FooModule {
    @Binds
    abstract fun bindFoo(fooImpl: FooImpl): Foo
}
```

With `AutoBind`, you can achieve the same result with following code:

```kotlin
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.github.milis92.hiltautobind.AutoBind

interface Foo

@AutoBind
class FooImpl @Inject constructor() : Foo
```

Hilt module will be generated automatically for you.

### Multiple supertypes

If the class has multiple supertypes, you can specify the supertype to bind to using the `superType` parameter.
Otherwise, the first supertype will be used. If class has no super types, the annotation processor will throw an error.

```kotlin
interface Foo
interface Bar

@AutoBind(superType = Foo::class)
class FooBarImpl @Inject constructor() : Foo, Bar
```

### Visibility

The generated module will have the same visibility as the annotated class.

## Factory function

`AutoFactory` is an annotation that can be used to automatically generate a biding for a class, based on the factory
function
In other words generates the entire hilt module based on a simple function, without the Module boilerplate.

Consider following example with Hilt without FactoryFunction:

```kotlin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

interface Foo

class SpecificFoo @Inject constructor() : Foo

@Module
@InstallIn(SingletonComponent::class)
object FooModule {
    @Provides
    fun provideFoo(): Foo {
        //Do some configuration etc
        return SpecificFoo()
    }
}


```

With FactoryFunction, you can achieve the same result with following code:

```kotlin
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.github.milis92.hiltautobind.AutoFactory

interface Foo

class SpecificFoo @Inject constructor() : Foo

@AutoFactory
fun Foo(): Foo {
    // Do some configuration and provide the Foo
    return SpecificFoo()
}
```

Hilt module will be generated automatically for you.

### Component

By default, the factory is generated for the `SingletonComponent`.
You can specify a different component using the `component` parameter.

### Visibility

The generated module will have the same visibility as the annotated function.

# Multibinding

Both `AutoBind` and `AutoFactory` support multibinding. To provide an element into a set or a map use one of
`AutoBindToMap`, `AutoBindToSet`, `AutoBindValuesToSet` or for the `dagger.Multibinds` use `AutoBindMultiBinds`.

Note: Using these annotations is required for multibinding, otherwise Dagger will pick up annotated types which
will result in a processing error. Other dagger mutlbind annotations should be used direcly ex. `ClassKey`, `MapKey` etc.

For example:

```kotlin
interface Foo

@AutoBindToSet
class FooImpl @Inject constructor() : Foo
```
or with a factory

```kotlin
interface Foo

class SpecificFoo : Foo

@AutoFactory
@AutoBindToSet
fun Foo(): Foo {
    return SpecificFoo()
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