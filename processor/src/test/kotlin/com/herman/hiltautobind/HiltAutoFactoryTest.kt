package com.herman.hiltautobind

import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

class HiltAutoFactoryTest {
    private companion object {
        @JvmField
        @RegisterExtension
        var compilerExtension = KotlinCompilationTestExtension()
    }

    @Test
    fun autoFactoryPreservesFactoryFunctionAnnotations() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            import javax.inject.Singleton
            
            annotation class SomeAnnotation(
                val someAnnotationString : String,
                val someAnnotationStringWithDefault : String = "default"
            )
            
            interface Something
            
            interface SomethingElse
            
            @Singleton
            @AutoFactory
            @SomeAnnotation("someAnnotationString", "someAnnotationStringWithDefault")
            fun SomethingFactory(somethingElse : Set<@JvmSuppressWildcards SomethingElse>): Something = object : Something {}
            """.trimIndent()
        )


        val provideSomething = ExpectedContent(
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
            public object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              @Singleton
              @SomeAnnotation(
                someAnnotationString = "someAnnotationString",
                someAnnotationStringWithDefault = "someAnnotationStringWithDefault",
              )
              public fun provideSomethingFactory(somethingElse: Set<@JvmSuppressWildcards SomethingElse>):
                  Something = SomethingFactory(somethingElse);
            }
            """.trimIndent()
        )
        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_AutoFactoryModule.kt") to provideSomething,
            )
        )
    }

    @Test
    fun autoFactoryPreservesFunctionArguments(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            
            interface Something
            
            @AutoFactory
            fun ProvidesSomething(someArgument: String): Something = SomethingImpl(something)
            
            internal class SomethingImpl(something: String) : SomethingElse
            """.trimIndent()
        )

        val expectedSomethingElseProvider = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import kotlin.String
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              public fun provideProvidesSomething(someArgument: String): Something =
                  ProvidesSomething(someArgument);
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_AutoFactoryModule.kt")
                    to expectedSomethingElseProvider
            )
        )
    }

    @Test
    fun autoFactoryPreservesFactoryFunctionVisibility() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            
            interface Something
            
            @AutoFactory
            internal fun SomethingFactory(): Something = object : Something {}
            """.trimIndent()
        )

        val provideSomething = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            internal object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              internal fun provideSomethingFactory(): Something = SomethingFactory();
            }
            """.trimIndent()
        )
        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_AutoFactoryModule.kt") to provideSomething,
            )
        )
    }

    @Test
    fun autoFactoryBindsToACustomComponent() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            import dagger.hilt.DefineComponent
            import dagger.hilt.components.SingletonComponent
            
            @DefineComponent(parent = SingletonComponent::class)
            interface CustomComponent
            
            interface Something
            
            @AutoFactory(component = CustomComponent::class)
            fun SomethingFactory(): Something = object : Something {}
            """.trimIndent()
        )

        val provideSomething = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            
            @Module
            @InstallIn(CustomComponent::class)
            public object Something_CustomComponent_AutoFactoryModule {
              @Provides
              public fun provideSomethingFactory(): Something = SomethingFactory();
            }
            """.trimIndent()
        )
        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_CustomComponent_AutoFactoryModule.kt") to provideSomething,
            )
        )
    }

    @Test
    fun autoFactoryBindsToASetWithSetTarget() {
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget
            
            interface Something
            
            @AutoFactory(target = AutoFactoryTarget.SET)
            fun SomethingFactory(): Something = object : Something {}
            """.trimIndent()
        )

        val provideSomething = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import dagger.multibindings.IntoSet
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              @IntoSet
              public fun provideSomethingFactory(): Something = SomethingFactory();
            }
            """.trimIndent()
        )
        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_AutoFactoryModule.kt") to provideSomething,
            )
        )
    }

    @Test
    fun autoFactoryBindsToAMapWithMapTarget(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget
            import dagger.multibindings.StringKey
            
            interface Something
            
            @StringKey("foo")
            @AutoFactory(target = AutoFactoryTarget.MAP)    
            fun SomethingFactory(): Something = object : Something {}
            """.trimIndent()
        )

        val provideSomething = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import dagger.multibindings.IntoMap
            import dagger.multibindings.StringKey
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              @IntoMap
              @StringKey(`value` = "foo")
              public fun provideSomethingFactory(): Something = SomethingFactory();
            }
            """.trimIndent()
        )
        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_AutoFactoryModule.kt") to provideSomething,
            )
        )

    }

    @Test
    fun autoFactoryFailsIfTargetIsSetButReturnTypeIsNotSet(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget
            
            interface Something
            
            @AutoFactory(target = AutoFactoryTarget.SET)
            fun SomethingFactory(): String = "something"
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(),
            expectSuccess = false
        )
    }

    @Test
    fun autoFactoryFailsIfTargetIsMapButReturnTypeIsNotMap(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget
            
            interface Something
            
            @AutoFactory(target = AutoFactoryTarget.MAP)
            fun SomethingFactory(): String = "something"
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(),
            expectSuccess = false
        )
    }

    @Test
    fun testAutoFactoryReplacesAutoFactoryModule(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            import com.herman.hiltautobind.annotations.autofactory.TestAutoFactory
            
            interface Something
                        
            @AutoFactory  
            fun SomethingFactory(): Something = object : Something {}
                        
            @TestAutoFactory   
            fun SomethingFactoryStub(): Something = object : Something {}
            """.trimIndent()
        )

        val expectedRuntimeComponent = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              public fun provideSomethingFactory(): Something = SomethingFactory();
            }
            """.trimIndent()
        )

        val expectedTestComponent = ExpectedContent(
            """
            import Something_SingletonComponent_AutoFactoryModule
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.components.SingletonComponent
            import dagger.hilt.testing.TestInstallIn
            
            @Module
            @TestInstallIn(
              components = [SingletonComponent::class],
              replaces = [Something_SingletonComponent_AutoFactoryModule::class],
            )
            public object Something_SingletonComponent_TestAutoFactoryModule {
              @Provides
              public fun provideSomethingFactoryStub(): Something = SomethingFactoryStub();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_AutoFactoryModule.kt") to expectedRuntimeComponent,
                FileName("kotlin/Something_SingletonComponent_TestAutoFactoryModule.kt") to expectedTestComponent
            )
        )
    }

    @Test
    fun autoFactoryGroupsProvidersOnTheSameModule(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            
            interface Something
                        
            @AutoFactory  
            fun SomethingFactory(): Something = object : Something {}
                
            @AutoFactory
            fun SomethingElseFactory(): Something = object : Something {}
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              public fun provideSomethingFactory(): Something = SomethingFactory();
            
              @Provides
              public fun provideSomethingElseFactory(): Something = SomethingElseFactory();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName("kotlin/Something_SingletonComponent_AutoFactoryModule.kt") to expectedContent
            )
        )
    }

    @Test
    fun autoFactoryExcludesPackageNameInTheGeneratedModuleName(){
        // Given
        val sourceFile = SourceFile.kotlin(
            name = "Main.kt",
            contents = """
            package com.herman.hiltautobind.test
            
            import com.herman.hiltautobind.annotations.autofactory.AutoFactory
            
            interface Something
                        
            @AutoFactory  
            fun SomethingFactory(): Something = object : Something {}
                
            @AutoFactory
            fun SomethingElseFactory(): Something = object : Something {}
            """.trimIndent()
        )

        val expectedContent = ExpectedContent(
            """
            package com.herman.hiltautobind.test

            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            
            @Module
            @InstallIn(SingletonComponent::class)
            public object Something_SingletonComponent_AutoFactoryModule {
              @Provides
              public fun provideSomethingFactory(): Something = SomethingFactory();
            
              @Provides
              public fun provideSomethingElseFactory(): Something = SomethingElseFactory();
            }
            """.trimIndent()
        )

        // Then
        compilerExtension.compileAndAssert(
            sources = listOf(sourceFile),
            expectedContent = mapOf(
                FileName(
                    "kotlin/com/herman/hiltautobind/test/Something_SingletonComponent_AutoFactoryModule.kt"
                ) to expectedContent
            )
        )
    }


}
