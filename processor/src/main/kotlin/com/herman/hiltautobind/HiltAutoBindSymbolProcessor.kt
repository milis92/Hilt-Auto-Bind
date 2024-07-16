package com.herman.hiltautobind

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.herman.hiltautobind.generator.AutoBindModuleGenerator
import com.herman.hiltautobind.generator.AutoFactoryModuleGenerator
import com.herman.hiltautobind.model.AutoBindSchema
import com.herman.hiltautobind.model.AutoFactorySchema
import com.herman.hiltautobind.visitors.HiltAutoBindSymbolVisitor

@AutoService(SymbolProcessorProvider::class)
class HiltAutoBindSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        HiltAutoBindSymbolProcessor(environment.codeGenerator, environment.logger)
}

class HiltAutoBindSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val autoBindModuleGenerator = AutoBindModuleGenerator()
        val autoFactoryModuleGenerator = AutoFactoryModuleGenerator()

        HiltAutoBindSymbolVisitor(logger)
            .collect(resolver)
            .forEach {
                when (it) {
                    is AutoBindSchema -> autoBindModuleGenerator.generate(codeGenerator, it)
                    is AutoFactorySchema -> autoFactoryModuleGenerator.generate(codeGenerator, it)
                }
            }
        return emptyList()
    }
}

