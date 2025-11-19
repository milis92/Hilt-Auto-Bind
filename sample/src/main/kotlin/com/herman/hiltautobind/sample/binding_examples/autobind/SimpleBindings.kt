package com.herman.hiltautobind.sample.binding_examples.autobind

import com.herman.hiltautobind.annotations.autobind.AutoBind
import com.herman.hiltautobind.sample.binding_examples.Special
import javax.inject.Inject
import javax.inject.Named

interface Tool {
    val name: String
}

@AutoBind
@Named("hammer")
class Hammer @Inject constructor() : Tool {
    override val name: String =
        "hammer"
}

@AutoBind
@Special
class SpecialTool @Inject constructor() : Tool {
    override val name: String =
        "special"
}
