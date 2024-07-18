package com.herman.hiltautobind

import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

@OptIn(ExperimentalCompilerApi::class)
class HiltAutoFactoryTest {
    private companion object {
        @JvmField
        @RegisterExtension
        var compilerExtension = KotlinCompilationTestExtension()
    }

    @Test
    fun simpleFactoryPreservesOriginalArguments() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            
            interface Something
            
            @Singleton
            @AutoFactory
            fun ProvideString(): String = "Some string"
            
            @Singleton
            @AutoFactory
            fun ProvidesSomething(someString: String): Something = SomethingImpl(someString)
            
            internal class SomethingImpl(string: String) : Something
            """.trimIndent()
        )

        val provideSomething = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            import kotlin.String
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              public fun provideProvidesSomething(someString: String): Something =
                  ProvidesSomething(someString);
            }
            """.trimIndent()
        )

        val provideString = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            import kotlin.String
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvideString_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              public fun provideProvideString(): String = ProvideString();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/ProvidesSomething_SingletonComponent_AutoFactoryModule.kt") to provideSomething,
                FileName("kotlin/ProvideString_SingletonComponent_AutoFactoryModule.kt") to provideString
            )
        )
    }

    @Test
    fun simpleFactoryPreservesDaggerAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
               
            @Singleton
            @AutoFactory
            @Named("something")
            fun ProvidesSomething(): Something = SomethingImpl()
            
            @Singleton
            @AutoFactory
            @Named("somethingElse")
            fun ProvidesSomethingElse(): Something = SomethingImpl()
            
            internal class SomethingImpl : Something
            """.trimIndent()
        )

        val expectedSomethingProvider = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Named
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              @Named(`value` = "something")
              public fun provideProvidesSomething(): Something = ProvidesSomething();
            }
            """.trimIndent()
        )

        val expectedSomethingElseProvider = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Named
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvidesSomethingElse_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              @Named(`value` = "somethingElse")
              public fun provideProvidesSomethingElse(): Something = ProvidesSomethingElse();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/ProvidesSomething_SingletonComponent_AutoFactoryModule.kt")
                    to expectedSomethingProvider,
                FileName("kotlin/ProvidesSomethingElse_SingletonComponent_AutoFactoryModule.kt")
                    to expectedSomethingElseProvider
            )
        )
    }

    @Test
    fun simpleFactoryPreservesCustomAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            
            annotation class SomeAnnotation(val someString: String)
            
            @Singleton
            @AutoFactory
            @SomeAnnotation("someString")
            fun ProvidesSomething(someString: String): Something = SomethingImpl(someString)
            
            internal class SomethingImpl(string: String) : Something
            """.trimIndent()
        )
        val expectedContent = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            import kotlin.String
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              @SomeAnnotation(someString = "someString")
              public fun provideProvidesSomething(someString: String): Something =
                  ProvidesSomething(someString);
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/ProvidesSomething_SingletonComponent_AutoFactoryModule.kt") to expectedContent
            )
        )
    }

    @Test
    fun simpleFactoryPreservesMultibindingAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            import com.herman.hiltautobind.AutoBindToSet
            
            interface Factory
            interface Something
            
            @Singleton
            @AutoFactory
            @AutoBindToSet
            fun ProvideFactory(): Factory = object: Factory {}
            
            @Singleton
            @AutoFactory
            fun ProvidesSomething(
              factories: Set<@JvmSuppressWildcards Factory>,
            ): Something = SomethingImpl(factories)
            
            internal class SomethingImpl(
                factories: Set<Factory>,
            ) : Something
            """.trimIndent()
        )
        val expectedProvideFactory = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import dagger.multibindings.IntoSet
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvideFactory_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              @IntoSet
              public fun provideProvideFactory(): Factory = ProvideFactory();
            }
            """.trimIndent()
        )
        val expectedSomethingProvider = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            import kotlin.collections.Set
            import kotlin.jvm.JvmSuppressWildcards
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              public fun provideProvidesSomething(factories: Set<@JvmSuppressWildcards Factory>): Something =
                  ProvidesSomething(factories);
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/ProvideFactory_SingletonComponent_AutoFactoryModule.kt")
                    to expectedProvideFactory,
                FileName("kotlin/ProvidesSomething_SingletonComponent_AutoFactoryModule.kt")
                    to expectedSomethingProvider
            )
        )
    }

    @Test
    fun simpleFactoryPreservesTypealiasOfCustomAnnotation() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            
            annotation class SomeAnnotation(val someString: String)
            typealias SomeOtherAnnotation = SomeAnnotation
            
            @Singleton
            @AutoFactory
            @SomeOtherAnnotation("someString")
            fun ProvidesSomething(): Something = SomethingImpl()
            
            internal class SomethingImpl : Something
            """.trimIndent()
        )
        val expectedContent = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              @SomeAnnotation(someString = "someString")
              public fun provideProvidesSomething(): Something = ProvidesSomething();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/ProvidesSomething_SingletonComponent_AutoFactoryModule.kt") to expectedContent
            )
        )
    }

    @Test
    fun simpleFactoryPreservesFactoryFunctionVisibility() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            
            interface Something
            
            @Singleton
            @AutoFactory
            fun ProvidesSomething(): Something = SomethingImpl()
            
            class SomethingImpl : Something
            """.trimIndent()
        )
        val expectedContent = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object ProvidesSomething_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              public fun provideProvidesSomething(): Something = ProvidesSomething();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/ProvidesSomething_SingletonComponent_AutoFactoryModule.kt") to expectedContent
            )
        )
    }

    @Test
    fun simpleFactoryCreatesFactoryWhenPartOfTheClass() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            
            interface Something {
                @Singleton
                @AutoFactory
                fun providesSomething() : Something = SomethingImpl()
            }
            class SomethingImpl : Something
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object providesSomething_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              public fun provideprovidesSomething(factory: Something): Something = factory.providesSomething();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/providesSomething_SingletonComponent_AutoFactoryModule.kt") to expectedContent
            )
        )
    }
}
