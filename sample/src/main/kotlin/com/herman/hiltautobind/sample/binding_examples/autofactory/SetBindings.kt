package com.herman.hiltautobind.sample.binding_examples.autofactory

import com.herman.hiltautobind.annotations.autofactory.AutoFactory
import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget

interface Beverage {
    val name: String
}

@AutoFactory(target = AutoFactoryTarget.SET, uniqueKey = "coffee")
fun CoffeeFactory(): Beverage = object : Beverage {
    override val name: String = "coffee"
}

@AutoFactory(target = AutoFactoryTarget.SET, uniqueKey = "tea")
fun TeaFactory(): Beverage = object : Beverage {
    override val name: String = "tea"
}

typealias Beverages = Set<@JvmSuppressWildcards Beverage>
