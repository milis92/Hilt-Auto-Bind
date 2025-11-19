package com.herman.hiltautobind.sample.binding_examples.autofactory

import com.herman.hiltautobind.annotations.autofactory.AutoFactory
import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget
import dagger.multibindings.StringKey

interface Vehicle {
    val name: String
}

@AutoFactory(target = AutoFactoryTarget.MAP)
@StringKey("car")
fun CarFactory(): Vehicle = object : Vehicle {
    override val name: String = "car"
}

@AutoFactory(target = AutoFactoryTarget.MAP)
@StringKey("bike")
fun BikeFactory(): Vehicle = object : Vehicle {
    override val name: String = "bike"
}

typealias Vehicles = Map<String, @JvmSuppressWildcards Vehicle>
