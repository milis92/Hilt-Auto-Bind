<h1 align="center">
  <img src="dagger.svg" width="150px" />
<p>Hilt AutoBind</p>
</h1>

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=Release&metadataUrl=https://repo1.maven.org/maven2/com/github/milis92/hiltautobind/processor/maven-metadata.xml)](https://repo1.maven.org/maven2/com/github/milis92/hiltautobind/processor/)

> [!NOTE]  
> Hilt modules are pure boilerplate code that don't contribute to application functionality in any meaningful way.  
> Instead of writing them manually, `Hilt AutoBind` automatically generates these modules since they follow strict,
> predictable patterns.

# ⚙️ Setup

Add the following dependencies to your build.gradle file:

#### With Gradle version catalogs

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

#### Without Gradle version catalogs

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

# ❔ How does it work?

Hilt AutoBind is a Code generator that has two main concepts:

1. `@AutoBind` annotation that you can use to generate a Hilt module that binds a class to a specified supertype
2. `@AutoFactory` annotation that you can use to generate a Hilt module based on a simple factory function

## `@AutoBind`

Annotate a class with `@AutoBind` to generate a module with a binding for the specified supertype.

#### With hilt:

```kotlin
interface AnalyticsService {
    fun analyticsMethods()
}

class AnalyticsServiceImpl @Inject constructor() : AnalyticsService {
    override fun analyticsMethods() { }
}

@Module
@InstallIn(ActivityComponent::class)
abstract class AnalyticsModule {
    @Binds
    abstract fun bindAnalyticsService(
        analyticsServiceImpl: AnalyticsServiceImpl
    ): AnalyticsService
}
 ```
#### With AutoBind:

```kotlin
interface AnalyticsService {
    fun analyticsMethods()
}

@AutoBind(ActivityComponent::class)
class AnalyticsServiceImpl @Inject constructor() : AnalyticsService {
    override fun analyticsMethods() { }
}
```
__AnalyticsModule will be generated automatically.__

### :memo: Usage

---

#### Simple singleton binding

Annotate your class with `@AutoBind` to bind it to its supertype:

```kotlin
interface AnalyticsService

@Singleton @AutoBind
class AnalyticsServiceImpl @Inject constructor() : AnalyticsService
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsService_SingletonComponent_Module {
    @Binds 
    @Singleton
    fun bindAnalyticsService(implementation: AnalyticsServiceImpl): AnalyticsService
}
  ```

---

#### Specifying a different Hilt component

Use the `component` parameter to bind to a different Hilt component:

```kotlin
interface AnalyticsService

@Singleton
@AutoBind(component = ActivityRetainedComponent::class)
class AnalyticsServiceImpl @Inject constructor() : AnalyticsService
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
interface AnalyticsService_ActivityRetainedComponent_Module {
    @Binds
    @Singleton
    fun bindAnalyticsService(implementation: AnalyticsServiceImpl): AnalyticsService
}
```

---

#### Multiple supertypes

When a class implements multiple supertypes, the first one is used by default.
Use the `superType` parameter to bind to a specific supertype instead:

```kotlin
interface Logger
interface Analytics

@Singleton
@AutoBind(superType = Analytics::class)
class AnalyticsImpl @Inject constructor() : Logger, Analytics
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface Analytics_SingletonComponent_Module {
    @Binds
    @Singleton
    fun bindAnalytics(implementation: AnalyticsImpl): Analytics
}
```

---

#### Multi-bindings

By default, the annotation processor binds a value as an instance of the specified supertype.  
For dagger multibindings, you can bind a value to a set or a map of supertypes using the `AutoBindTarget`
parameter.

```kotlin
interface AnalyticsService

@Singleton
@AutoBind(target = AutoBindTarget.SET) // or AutoBindTarget.MAP
class AnalyticsServiceImpl @Inject constructor() : AnalyticsService
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsService_SingletonComponent_Module {
    @Binds
    @IntoSet
    fun bindAnalyticsService(implementation: AnalyticsServiceImpl): AnalyticsService
}
```

---

#### Visibility

The generated module will have the same visibility as the annotated class.

```kotlin
interface AnalyticsService

@AutoBind
internal class AnalyticsServiceImpl : AnalyticsService
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal interface AnalyticsService_SingletonComponent_Module {
    @Binds
    fun bindAnalyticsService(implementation: AnalyticsServiceImpl): AnalyticsService
}
```

---

## `@AutoFactory`

Annotate a function with `@AutoFactory` to generate a module with a dagger provides funciton for the specified supertype.

#### With hilt:

```kotlin
interface AnalyticsService {
    fun analyticsMethods()
}

class AnalyticsServiceImpl(
    val sessionId: String
) : AnalyticsService {
    override fun analyticsMethods() { }
}

@Module
@InstallIn(ActivityComponent::class)
object AnalyticsModule {

  @Provides
  fun provideAnalyticsService(
      sessionId: String // sessionId is just an example
  ): AnalyticsService {
      return AnalyticsServiceImpl(sessionId = sessionId)
  }
}
```
#### With Autofactory:

```kotlin
interface AnalyticsService {
    fun analyticsMethods()
}

class AnalyticsServiceImpl(
    val sessionId: String
) : AnalyticsService {
    override fun analyticsMethods() { }
}

@AutoFactory
fun analyticsService(sessionId: String) = 
    AnalyticsServiceImpl(sessionId)
```
__AnalyticsModule will be generated automatically.__

### Usage

---

#### Simple singleton binding

Simply annotate your function with `AutoFactory`

```kotlin
interface AnalyticsService

@Singleton
@AutoFactory
fun analyticsService(): AnalyticsService = 
    AnalyticsServiceImpl()

class AnalyticsServiceImpl : AnalyticsService
 ```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    public fun provideAnalyticsService(): AnalyticsService =
        analyticsService()
}
```

---

#### Specifying a different Hilt component

You can specify a different Hilt component using the `component` parameter.

```kotlin
interface AnalyticsService

@Singleton
@AutoFactory(component = ActivityRetainedComponent::class)
fun analyticsService(): AnalyticsService = AnalyticsServiceImpl()

class AnalyticsServiceImpl : AnalyticsService
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
public object AnalyticsService_ActivityRetainedComponent_AutoFactoryModule {
    @Provides
    @Singleton
    public fun provideAnalyticsService(): AnalyticsService = analyticsService()
}
```

---

#### Multi-bindings

By default, the annotation processor generates a module that provides a single instance of an annotated function return type.
For dagger multibindings, you can bind a value to a set or a map of supertypes using
the `AutoFactoryTarget` parameter.

---

#### Set or map multibindings

```kotlin
interface AnalyticsService

@Singleton
@AutoFactory(target = AutoFactoryTarget.SET) // or AutoFactoryTarget.MAP
fun analyticsService(): AnalyticsService = object : AnalyticsService {}
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    @IntoSet
    public fun provideAnalyticsService(): AnalyticsService = analyticsService()
}
```

---

#### Elements into set

For providing a set of values, that should be bound to a set of a super type, use `AutoFactoryTarget.SET_VALUES`

```kotlin
interface AnalyticsService

@Singleton
@AutoFactory(target = AutoFactoryTarget.SET_VALUES)
fun analyticsService(): Set<AnalyticsService> = setOf(object : AnalyticsService {})
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    @ElementsIntoSet
    public fun provideAnalyticsServices(): Set<AnalyticsService> = analyticsService()
}
```

---

#### Multibindings set provider

With dagger multibindings, you do not have to explicitly provide a set/map container if the bound set/map is
going to have at least one element. If the resulting set could be empty set container needs to be provided.
Equivalent to `dagger.multibindings.Multibinds` you can provide a container for a set
of values using `AutoFactoryTarget.SET_VALUES`

```kotlin
interface AnalyticsService

@Singleton
@AutoFactory(target = AutoFactoryTarget.MULTIBINDING_CONTAINER)
fun analyticsService(): Set<AnalyticsService> = setOf()
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    @ElementsIntoSet
    public fun provideAnalyticsServices(): Set<AnalyticsService> = analyticsService()
}
```

---

#### Factory Function Patterns

The `@AutoFactory` annotated function can be defined in different contexts. Each pattern generates the appropriate provider function based on where the factory function is located.

##### Top-level function

Define the factory function at the top level of a file:

```kotlin
interface AnalyticsService

@Singleton
@AutoFactory
fun analyticsService(): AnalyticsService = AnalyticsServiceImpl()
```

The annotation processor will generate:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    public fun provideAnalyticsService(): AnalyticsService = analyticsService()
}
```

##### Companion object function

Define the factory function in a companion object:

```kotlin
interface AnalyticsService

class AnalyticsContainer {
    companion object {
        @Singleton
        @AutoFactory
        fun analyticsService(): AnalyticsService = AnalyticsServiceImpl()
    }
}
```

The annotation processor will generate:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    public fun provideAnalyticsService(): AnalyticsService = 
        AnalyticsContainer.analyticsService()
}
```

##### Class member function

Define the factory function in a regular class. The class must be part of the dependency graph (typically injected):

```kotlin
interface AnalyticsService

class AnalyticsProvider @Inject constructor() {
    @Singleton
    @AutoFactory
    fun analyticsService(): AnalyticsService = AnalyticsServiceImpl()
}
```

The annotation processor will generate:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    public fun provideAnalyticsService(
        provider: AnalyticsProvider
    ): AnalyticsService = provider.analyticsService()
}
```

> [!NOTE]  
> **Note:** When the factory function is in a class, the class itself becomes a dependency and must be available in the component's dependency graph.

---

#### Visibility

The generated module will have the same visibility as the annotated function.

```kotlin
interface AnalyticsService

@AutoFactory
internal fun analyticsService(): AnalyticsService = AnalyticsServiceImpl()
```

The annotation processor will generate the following Hilt module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    @Singleton
    fun provideAnalyticsService(): AnalyticsService = analyticsService()
}
```

# ✔️ Testing

Hilt AutoBind provides test-specific annotation counterparts to generate test modules that automatically
replace production bindings in your tests.
This allows you to inject test implementations or stubs without manually creating test modules.

## `@TestAutoBind`

Use `@TestAutoBind` to create a test binding that replaces the production binding generated by `@AutoBind`.

### Example:

#### Production code (Analytics example):

```kotlin
interface AnalyticsService {
    fun analyticsMethods()
}

@AutoBind
class AnalyticsServiceImpl @Inject constructor() : AnalyticsService {
    override fun analyticsMethods() { }
}
```

Generated production module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public interface AnalyticsService_SingletonComponent_Module {
    @Binds
    public fun bindAnalyticsService(implementation: AnalyticsServiceImpl): AnalyticsService
}
```

#### Test code:

```kotlin
@TestAutoBind
class AnalyticsServiceStub : AnalyticsService {
    override fun analyticsMethods() { }
}
```

Generated test module (automatically replaces the production module in tests):

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsService_SingletonComponent_Module::class],
)
public interface AnalyticsService_SingletonComponent_TestModule {
    @Binds
    public fun bindAnalyticsServiceStub(implementation: AnalyticsServiceStub): AnalyticsService
}
```

#### Qualified bindings

You can also replace qualified bindings. In production, you might have multiple implementations qualified with @Named; in tests, you can provide a qualified stub that replaces exactly that binding:

```kotlin
interface AnalyticsService

@AutoBind(superType = AnalyticsService::class)
class AnalyticsDefaultImpl @Inject constructor() : AnalyticsService

@Named("debug")
@AutoBind(superType = AnalyticsService::class)
class AnalyticsDebugImpl @Inject constructor() : AnalyticsService

@Named("debug")
@TestAutoBind(superType = AnalyticsService::class)
class AnalyticsDebugStub : AnalyticsService
```

Generated modules (simplified from processor tests; the exact class name suffix for qualified modules may vary):

```kotlin
// Unqualified runtime binding
@Module
@InstallIn(SingletonComponent::class)
public interface AnalyticsService_SingletonComponent_Module {
    @Binds
    public fun bindAnalyticsService(implementation: AnalyticsDefaultImpl): AnalyticsService
}

// Qualified runtime binding (the exact name may vary)
@Module
@InstallIn(SingletonComponent::class)
public interface AnalyticsServiceQualified_SingletonComponent_Module {
    @Binds
    @Named("debug")
    public fun bindAnalyticsServiceDebug(implementation: AnalyticsDebugImpl): AnalyticsService
}

// Qualified test replacement
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsServiceQualified_SingletonComponent_Module::class],
)
public interface AnalyticsServiceQualified_SingletonComponent_TestModule {
    @Binds
    @Named("debug")
    public fun bindAnalyticsServiceDebugStub(implementation: AnalyticsDebugStub): AnalyticsService
}
```

> [!NOTE]  
> `@TestAutoBind` will automatically replace the module generated by `@AutoBind` for the same type (and qualifier, if present) inside your test environment.

## `@TestAutoFactory`

Use `@TestAutoFactory` to create a test factory that replaces the production module generated by `@AutoFactory`.

### Example:

#### Production code (Analytics example):

```kotlin
interface AnalyticsService

class AnalyticsServiceImpl : AnalyticsService

@AutoFactory
fun analyticsService(): AnalyticsService = AnalyticsServiceImpl()
```

Generated production module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    public fun provideAnalyticsService(): AnalyticsService = analyticsService()
}
```

#### Test code:

```kotlin
@TestAutoFactory
fun analyticsServiceStub(): AnalyticsService = object : AnalyticsService {}
```

Generated test module (automatically replaces the production module in tests):

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsService_SingletonComponent_AutoFactoryModule::class],
)
public object AnalyticsService_SingletonComponent_TestAutoFactoryModule {
    @Provides
    public fun provideAnalyticsServiceStub(): AnalyticsService = analyticsServiceStub()
}
```

#### Qualified factories

Qualified production factories and their qualified test counterparts are matched and replaced by qualifier:

```kotlin
interface AnalyticsService

@AutoFactory
fun analyticsService(): AnalyticsService = object : AnalyticsService {}

@Named("debug")
@AutoFactory
fun analyticsServiceDebug(): AnalyticsService = object : AnalyticsService {}

@Named("debug")
@TestAutoFactory
fun analyticsServiceDebugStub(): AnalyticsService = object : AnalyticsService {}
```

Generated modules (simplified):

```kotlin
// Unqualified runtime factory
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsService_SingletonComponent_AutoFactoryModule {
    @Provides
    public fun provideAnalyticsService(): AnalyticsService = analyticsService()
}

// Qualified runtime factory (class name may include a hash to disambiguate)
@Module
@InstallIn(SingletonComponent::class)
public object AnalyticsServiceQualified_SingletonComponent_AutoFactoryModule {
    @Provides
    @Named("debug")
    public fun provideAnalyticsServiceDebug(): AnalyticsService = analyticsServiceDebug()
}

// Qualified test replacement
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsServiceQualified_SingletonComponent_AutoFactoryModule::class],
)
public object AnalyticsServiceQualified_SingletonComponent_TestAutoFactoryModule {
    @Provides
    @Named("debug")
    public fun provideAnalyticsServiceDebugStub(): AnalyticsService = analyticsServiceDebugStub()
}
```

> [!NOTE]  
> `@TestAutoFactory` will automatically replace the module generated by `@AutoFactory` for the same return type (and qualifier, if present) inside your test environment.