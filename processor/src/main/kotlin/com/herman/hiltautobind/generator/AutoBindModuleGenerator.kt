package com.herman.hiltautobind.generator

import com.google.devtools.ksp.symbol.KSFile
import com.herman.hiltautobind.model.AutoBindSchema
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile

class AutoBindModuleGenerator : HiltAutoBindModuleGenerator<AutoBindSchema>() {

    override fun buildHiltProvideFunction(
        schema: AutoBindSchema,
    ): FunSpec = with(schema) {
        FunSpec.builder(name = hiltFunctionName)
            .addAnnotations(hiltFunctionAnnotations)
            .addModifiers(KModifier.ABSTRACT)
            .addParameter(
                name = PARAM_IMPLEMENTATION,
                type = implementationType
            )
            .returns(boundType)
            .build()
    }

    companion object {
        private const val PARAM_IMPLEMENTATION = "implementation"
    }
}
