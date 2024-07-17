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
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.extension.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*


@OptIn(ExperimentalCompilerApi::class)
class KotlinCompilationTestExtension(
    private val cleanUp: Boolean = true,
    private val incrementalKsp: Boolean = false,
) : BeforeEachCallback, AfterAllCallback {

    private val compilerOutputDir = Path.of("build")
    private lateinit var compilerWorkDirectory: Path

    private lateinit var compiler: KotlinCompilation

    private val hiltKspCompilerSymbolsRegistrar = ServiceLoader.load(SymbolProcessorProvider::class.java)

    override fun beforeEach(context: ExtensionContext?) {
        compilerWorkDirectory = Files.createTempDirectory(
            compilerOutputDir, context?.displayName ?: "Kotlin-Compilation"
        )
        compiler = KotlinCompilation().apply {
            messageOutputStream = System.out
            inheritClassPath = true
            verbose = false
            configureKsp(true) {
                incremental = incrementalKsp
                symbolProcessorProviders.addAll(hiltKspCompilerSymbolsRegistrar)
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

    fun compile(
        sources: List<SourceFile>,
    ): JvmCompilationResult {
        compiler.sources = sources
        return compiler.compile()
    }
}