package com.herman.hiltautobind

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

@OptIn(ExperimentalCompilerApi::class)
class HiltAutoFactoryTest {
    companion object {
        @JvmField
        @RegisterExtension
        var compilerExtension = KotlinCompilationTestExtension()
    }

    @Test
    fun simpleFactoryPreservesOriginalArguments() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
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
            """
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleFactoryPreservesDaggerAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
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
            """
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleFactoryPreservesCustomAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
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
            """
        )
        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleFactoryPreservesMultibindingAnnotations() {

        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
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
            """
        )
        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleFactoryPreservesTypealiasOfCustomAnnotation(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
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
            """
        )
        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleFactoryPreservesFactoryFunctionVisibility() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            
            interface Something

            @Singleton
            @AutoFactory
            fun ProvidesSomething(): Something = SomethingImpl()

            class SomethingImpl : Something
            """
        )
        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleFactoryCreatesFactoryWhenPartOfTheClass() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
            import com.herman.hiltautobind.AutoFactory
            import javax.inject.Singleton
            
            interface Something {
                @Singleton
                @AutoFactory
                fun providesSomething() : Something = SomethingImpl()
            }
            class SomethingImpl : Something
            """
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }
}