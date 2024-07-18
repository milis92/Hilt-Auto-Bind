/*
 * Copyright (C) 2020 Ivan Milisavljevic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.herman.hiltautobind

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.*
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class KotlinCompilationTestExtension(
    private val cleanUp: Boolean = true,
    private val incrementalKsp: Boolean = true,
) : BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    private lateinit var compilerOutputDir: Path
    private lateinit var compilerWorkDirectory: Path

    private lateinit var compiler: KotlinCompilation

    private val kspCompilerSymbolsRegistrar = ServiceLoader.load(SymbolProcessorProvider::class.java)

    override fun beforeAll(context: ExtensionContext?) {
        compilerOutputDir = Path.of("build", "kotlin-compile-test", context?.displayName).createDirectories()
    }

    override fun beforeEach(context: ExtensionContext?) {
        compilerWorkDirectory = File(
            compilerOutputDir.toFile(),
            context?.displayName ?: "Kotlin-Compilation"
        ).toPath().createDirectories()

        compiler = KotlinCompilation().apply {
            messageOutputStream = System.out
            inheritClassPath = true
            verbose = false
            configureKsp(useKsp2 = true) {
                incremental = incrementalKsp
                symbolProcessorProviders.addAll(kspCompilerSymbolsRegistrar)
                loggingLevels = CompilerMessageSeverity.VERBOSE
            }
            workingDir = compilerWorkDirectory.toFile()
        }
    }

    @OptIn(ExperimentalPathApi::class)
    override fun afterAll(context: ExtensionContext?) {
        if (cleanUp) {
            compilerWorkDirectory.deleteRecursively()
        }
    }

    fun compileAndAssert(
        sources: List<SourceFile>,
        expectedContent: Map<FileName, ExpectedContent>,
        additionalAssertion: (JvmCompilationResult) -> Unit = {
            assert(it.exitCode == KotlinCompilation.ExitCode.OK)
        }
    ) {
        compiler.sources = sources

        val compilationResult = compiler.compile()
        expectedContent.forEach {
            val file = compiler.kspSourcesDir.resolve(it.key.name)
            assertEquals(it.value.content, file.readText().trim())
        }
        additionalAssertion(compilationResult)
    }
}

@JvmInline
value class FileName(val name: String)

@JvmInline
value class ExpectedContent(@Language("kotlin") val content: String)
