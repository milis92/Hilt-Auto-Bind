package com.herman.hiltautobind

import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

class HiltAutoBindTest {
    private companion object {
        @JvmField
        @RegisterExtension
        var compilerExtension =
            KotlinCompilationTestExtension(cleanUp = false)
    }

    @Test
    fun autoBindPreservesClassAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            import javax.inject.Singleton
            
            annotation class SomeAnnotation(
                val someAnnotationString : String,
                val someAnnotationStringWithDefault : String = "default"
            )
            
            interface Something
            
            @AutoBind
            @Singleton
            @SomeAnnotation("someAnnotationValue")
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
              @SomeAnnotation(someAnnotationString = "someAnnotationValue")
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoBindPreservesBoundTypeVisibility() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            
            interface Something
            
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
            
            @Module
            @InstallIn(SingletonComponent::class)
            internal interface Something_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoBindBindsToASpecifiedSupertype() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            
            interface Something
            interface Another
            
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
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Another_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Another
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Another_SingletonComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoBindBindsToFirstSuperType() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            
            interface Something
            interface Another
            
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
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName(
                    "kotlin/Something_SingletonComponent_Module.kt"
                ) to expectedContent
            )
        )
    }

    @Test
    fun autoBindPreservesSuperTypeTypeArguments() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            
            interface Something<T: Any>
            
            @AutoBind
            internal class SomethingImpl : Something<Any>
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import kotlin.Any
            
            @Module
            @InstallIn(SingletonComponent::class)
            internal interface Something_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Something<Any>
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoBindBindsToItselfWhenNoSuperType() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            
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
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface SomethingImpl_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): SomethingImpl
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/SomethingImpl_SingletonComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoBindBindsToACustomComponent() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            import dagger.hilt.DefineComponent
            import dagger.hilt.components.SingletonComponent
            
            @DefineComponent(parent = SingletonComponent::class)
            interface CustomComponent
            
            @AutoBind(component = CustomComponent::class)
            class SomethingImpl
            """.trimIndent()
        )
        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            
            @Module
            @InstallIn(CustomComponent::class)
            public interface SomethingImpl_CustomComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): SomethingImpl
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/SomethingImpl_CustomComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoBindBindsToASetWithSetTarget() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            import com.herman.hiltautobind.annotations.autobind.AutoBindTarget
            
            interface Something
            
            @AutoBind(target = AutoBindTarget.SET)
            class SomethingImpl : Something
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import dagger.multibindings.IntoSet
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              @IntoSet
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoBindBindsToAMapWithMapTarget() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            import com.herman.hiltautobind.annotations.autobind.AutoBindTarget
            import dagger.multibindings.StringKey
            
            interface Something
            
            @StringKey("foo")
            @AutoBind(target = AutoBindTarget.MAP)
            class SomethingImpl : Something
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import dagger.multibindings.IntoMap
            import dagger.multibindings.StringKey
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              @IntoMap
              @StringKey(`value` = "foo")
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent
            )
        )
    }

    @Test
    fun testAutoBindReplacesAutoBindModule() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            package com.herman.hiltautobind.test
            
            import com.herman.hiltautobind.annotations.autobind.TestAutoBind
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            
            interface Something
            
            @AutoBind
            class SomethingImpl : Something
            
            @TestAutoBind
            class SomethingStub : Something
            """.trimIndent()
        )

        val expectedRuntimeComponent = ExpectedContent(
            """
            package com.herman.hiltautobind.test
            
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        val expectedTestComponent = ExpectedContent(
            """
            package com.herman.hiltautobind.test
            
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.components.SingletonComponent
            import dagger.hilt.testing.TestInstallIn
            
            @Module
            @TestInstallIn(
              components = [SingletonComponent::class],
              replaces = [Something_SingletonComponent_Module::class],
            )
            public interface Something_SingletonComponent_TestModule {
              @Binds
              public fun bindSomethingStub(implementation: SomethingStub): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/com/herman/hiltautobind/test/Something_SingletonComponent_Module.kt") to expectedRuntimeComponent,
                FileName("kotlin/com/herman/hiltautobind/test/Something_SingletonComponent_TestModule.kt") to expectedTestComponent
            )
        )
    }

    @Test
    fun autoBindSeparatesQualifiedDependencies() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            import javax.inject.Named
            
            interface Something
            
            @AutoBind(superType = Something::class)
            class SomethingImpl : Something
            
            @Named("somethingElse")
            @AutoBind(superType = Something::class)
            class SomethingElseImpl: Something
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        val expectedQualifiedContent = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Named
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something1294259891_SingletonComponent_Module {
              @Binds
              @Named(`value` = "somethingElse")
              public fun bindSomethingElseImpl(implementation: SomethingElseImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedContent,
                FileName("kotlin/Something1294259891_SingletonComponent_Module.kt") to expectedQualifiedContent
            )
        )
    }

    @Test
    fun autoBindExcludesPackageNameInTheGeneratedModuleName() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            package com.herman.hiltautobind.test
            
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            
            interface Something
            
            @AutoBind
            class SomethingImpl : Something
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            package com.herman.hiltautobind.test
            
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName(
                    "kotlin/com/herman/hiltautobind/test/Something_SingletonComponent_Module.kt"
                ) to expectedContent
            )
        )
    }

    @Test
    fun testAutoBindWithNamedQualifierReplacesQualifiedAutoBindModule() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autobind.AutoBind
            import com.herman.hiltautobind.annotations.autobind.TestAutoBind
            import javax.inject.Named
            
            interface Something
            
            @AutoBind(superType = Something::class)
            class SomethingImpl : Something
            
            @Named("somethingElse")
            @AutoBind(superType = Something::class)
            class SomethingElseImpl: Something
            
            @Named("somethingElse")
            @TestAutoBind(superType = Something::class)
            class SomethingElseStub: Something
            """.trimIndent()
        )

        val expectedUnqualifiedRuntimeModule = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something_SingletonComponent_Module {
              @Binds
              public fun bindSomethingImpl(implementation: SomethingImpl): Something
            }
            """.trimIndent()
        )

        val expectedQualifiedRuntimeModule = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Named
            
            @Module
            @InstallIn(SingletonComponent::class)
            public interface Something1294259891_SingletonComponent_Module {
              @Binds
              @Named(`value` = "somethingElse")
              public fun bindSomethingElseImpl(implementation: SomethingElseImpl): Something
            }
            """.trimIndent()
        )

        val expectedQualifiedTestModule = ExpectedContent(
            """
            import dagger.Binds
            import dagger.Module
            import dagger.hilt.components.SingletonComponent
            import dagger.hilt.testing.TestInstallIn
            import javax.inject.Named
            
            @Module
            @TestInstallIn(
              components = [SingletonComponent::class],
              replaces = [Something1294259891_SingletonComponent_Module::class],
            )
            public interface Something1294259891_SingletonComponent_TestModule {
              @Binds
              @Named(`value` = "somethingElse")
              public fun bindSomethingElseStub(implementation: SomethingElseStub): Something
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_Module.kt") to expectedUnqualifiedRuntimeModule,
                FileName("kotlin/Something1294259891_SingletonComponent_Module.kt") to expectedQualifiedRuntimeModule,
                FileName("kotlin/Something1294259891_SingletonComponent_TestModule.kt") to expectedQualifiedTestModule,
            )
        )
    }
}
