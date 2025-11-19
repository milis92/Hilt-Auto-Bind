package com.herman.hiltautobind.sample.binding_examples.autofactory

import com.herman.hiltautobind.annotations.autofactory.AutoFactory
import com.herman.hiltautobind.sample.binding_examples.Special

data class Paint(val tone: String)

@AutoFactory
fun PaintFactory(): Paint = Paint("red")

@AutoFactory
@Special
fun SpecialFactory(): Paint = Paint("blue")
