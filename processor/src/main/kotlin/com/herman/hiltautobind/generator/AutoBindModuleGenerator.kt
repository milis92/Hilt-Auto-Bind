package com.herman.hiltautobind.generator

import com.herman.hiltautobind.model.AutoBindSchema
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName

class AutoBindModuleGenerator : HiltAutoBindModuleGenerator<AutoBindSchema>() {
    override fun buildHiltProvideFunction(schema: AutoBindSchema): FunSpec = with(schema) {
        FunSpec.builder(name = hiltFunctionName)
            .addAnnotations(hiltFunctionAnnotations)
            .addParameter(name = PARAM_IMPLEMENTATION, type = annotatedClass.toClassName())
            .addModifiers(KModifier.ABSTRACT)
            .returns(boundSuperType)
            .build()
    }

    companion object {
        private const val PARAM_IMPLEMENTATION = "implementation"
    }
}
