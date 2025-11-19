package com.herman.hiltautobind.sample.binding_examples.autobind

import com.herman.hiltautobind.annotations.autobind.AutoBind
import com.herman.hiltautobind.annotations.autobind.AutoBindTarget
import javax.inject.Inject

interface Animal {
    val name: String
}

@AutoBind(target = AutoBindTarget.SET, uniqueKey = "cat")
class Cat @Inject constructor() : Animal {
    override val name: String =
        "cat"
}

@AutoBind(target = AutoBindTarget.SET, uniqueKey = "dog")
class Dog @Inject constructor() : Animal {
    override val name: String =
        "dog"
}

typealias Animals = Set<@JvmSuppressWildcards Animal>
