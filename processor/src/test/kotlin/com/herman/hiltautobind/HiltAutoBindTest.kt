package com.herman.hiltautobind

import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

@OptIn(ExperimentalCompilerApi::class)
class HiltAutoBindTest {
    private companion object {
        @JvmField
        @RegisterExtension
        var compilerExtension = KotlinCompilationTestExtension()
    }

    @Test
    fun simpleBindPreservesDaggerAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            
            @Singleton
            @AutoBind
            class SomethingImpl : Something
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              @Singleton
              public fun bindSomething(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent)
        )
    }

    @Test
    fun simpleBindPreservesBoundTypeVisibility() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            interface Something
            
            @Singleton
            @AutoBind
            internal class SomethingImpl : Something
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            internal interface Something_SingletonComponent_Module {
              @Binds
              @Singleton
              public fun bindSomething(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent)
        )
    }

    @Test
    fun simpleBindBindsToSpecificSuperType() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
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

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Another_SingletonComponent_Module {
              @Binds
              @Singleton
              public fun bindAnother(implementation: SomethingImpl): Another
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(FileName("kotlin/Another_SingletonComponent_Module.kt") to expectedContent)
        )
    }

    @Test
    fun simpleBindBindsToFirstSuperType() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
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

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              @Singleton
              public fun bindSomething(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent)
        )
    }

    @Test
    fun simpleBindBindsToItselfWhenThereIsNoSupertype() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.AutoBind
            import javax.inject.Singleton
            import javax.inject.Named
            
            @Singleton
            @AutoBind
            class SomethingImpl
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface SomethingImpl_SingletonComponent_Module {
              @Binds
              @Singleton
              public fun bindSomethingImpl(implementation: SomethingImpl): SomethingImpl
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(FileName("kotlin/SomethingImpl_SingletonComponent_Module.kt") to expectedContent)
        )
    }
}
