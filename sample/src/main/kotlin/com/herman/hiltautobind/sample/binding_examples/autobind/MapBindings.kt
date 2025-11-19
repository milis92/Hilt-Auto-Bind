package com.herman.hiltautobind.sample.binding_examples.autobind

import com.herman.hiltautobind.annotations.autobind.AutoBind
import com.herman.hiltautobind.annotations.autobind.AutoBindTarget
import dagger.multibindings.StringKey
import javax.inject.Inject

interface Fruit {
    val name: String
}

@AutoBind(target = AutoBindTarget.MAP)
@StringKey("orange")
class Orange @Inject constructor() : Fruit {
    override val name: String = "orange"
}

@AutoBind(target = AutoBindTarget.MAP)
@StringKey("apple")
class Apple @Inject constructor() : Fruit {
    override val name: String = "apple"
}

typealias Fruits = Map<String, @JvmSuppressWildcards Fruit>
