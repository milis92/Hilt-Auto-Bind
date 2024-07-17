package com.herman.hiltautobind

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

@OptIn(ExperimentalCompilerApi::class)
class HiltAutoBindTest {
    companion object {
        @JvmField
        @RegisterExtension
        var compilerExtension = KotlinCompilationTestExtension()
    }

    @Test
    fun simpleBindPreservesDaggerAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            
            @Singleton
            @AutoBind
            class SomethingImpl : Something
            """.trimIndent()
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleBindPreservesBoundTypeVisibility(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            
            @Singleton
            @AutoBind
            internal class SomethingImpl : Something
            """.trimIndent()
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleBindBindsToSpecificSuperType(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            interface Another
            
            @Singleton
            @AutoBind(superType = Another::class)
            class SomethingImpl : Something, Another
            """.trimIndent()
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleBindBindsToFirstSuperType(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            interface Another
            
            @Singleton
            @AutoBind
            class SomethingImpl : Something, Another
            """.trimIndent()
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun simpleBindThrowsErrorWhenNoSuperType(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt", contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            @Singleton
            @AutoBind
            class SomethingImpl
            """.trimIndent()
        )

        // When
        val compilationResult = compilerExtension.compile(listOf(sourceFile))

        // Then
        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }
}