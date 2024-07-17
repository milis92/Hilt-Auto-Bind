package com.herman.hiltautobind.generator

import com.herman.hiltautobind.model.AutoBindSchema
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import dagger.Binds

class AutoBindModuleGenerator : HiltAutoBindModuleGenerator<AutoBindSchema>() {
    override fun buildHiltProvideFunction(schema: AutoBindSchema): FunSpec = with(schema) {
        FunSpec.builder(name = hiltFunctionName)
            .addAnnotation(daggerBindsClassName)
            .addAnnotations(otherAnnotations)
            .addParameter(name = PARAM_IMPLEMENTATION, type = annotatedClass)
            .addModifiers(KModifier.ABSTRACT)
            .returns(boundSuperType)
            .build()
    }

    companion object {
        private val daggerBindsClassName = Binds::class.asClassName()
        private const val PARAM_IMPLEMENTATION = "implementation"
    }
}
