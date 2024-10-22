package com.herman.hiltautobind

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.herman.hiltautobind.generator.AutoBindModuleGenerator
import com.herman.hiltautobind.generator.AutoFactoryModuleGenerator
import com.herman.hiltautobind.visitors.AutoBindVisitor
import com.herman.hiltautobind.visitors.AutoFactoryVisitor

@AutoService(SymbolProcessorProvider::class)
class HiltAutoBindSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        HiltAutoBindSymbolProcessor(environment.codeGenerator, environment.logger)
}

internal class HiltAutoBindSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        processBindings(resolver)
        processFactoryMethods(resolver)
        return emptyList()
    }

    private fun processBindings(resolver: Resolver) {
        val generator = AutoBindModuleGenerator()
        AutoBindVisitor(logger).collect(resolver).entries.forEach { (moduleName, schemas) ->
            generator.generate(codeGenerator, moduleName, schemas)
        }
    }

    private fun processFactoryMethods(resolver: Resolver) {
        val generator = AutoFactoryModuleGenerator()
        AutoFactoryVisitor(logger).collect(resolver).entries.forEach { (moduleName, schemas) ->
            generator.generate(codeGenerator, moduleName, schemas)
        }
    }
}
