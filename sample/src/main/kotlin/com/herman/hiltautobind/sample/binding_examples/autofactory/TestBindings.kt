package com.herman.hiltautobind.sample.binding_examples.autofactory

import com.herman.hiltautobind.annotations.autofactory.AutoFactory

interface Instrument {
    val name: String
}

@AutoFactory
fun Guitar() = object : Instrument {
    override val name: String = "guitar"
}
