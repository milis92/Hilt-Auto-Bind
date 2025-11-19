package com.herman.hiltautobind.sample.binding_examples.autobind

import com.herman.hiltautobind.annotations.autobind.AutoBind
import javax.inject.Inject

interface Shape {
    val name: String
}

@AutoBind
class Circle @Inject constructor() : Shape {
    override val name: String = "circle"
}
