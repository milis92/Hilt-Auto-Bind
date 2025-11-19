package com.herman.hiltautobind.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.herman.hiltautobind.annotations.autofactory.AutoFactoryTarget

import com.herman.hiltautobind.annotations.autofactory.TestAutoFactory
import com.herman.hiltautobind.sample.binding_examples.Special
import com.herman.hiltautobind.sample.binding_examples.autofactory.Beverage
import com.herman.hiltautobind.sample.binding_examples.autofactory.Beverages
import com.herman.hiltautobind.sample.binding_examples.autofactory.Instrument
import com.herman.hiltautobind.sample.binding_examples.autofactory.Paint
import com.herman.hiltautobind.sample.binding_examples.autofactory.Vehicle
import com.herman.hiltautobind.sample.binding_examples.autofactory.Vehicles
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.multibindings.StringKey
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@TestAutoFactory
fun FakeInstrument(): Instrument =
    object : Instrument {
        override val name: String = "violin"
    }

@TestAutoFactory(target = AutoFactoryTarget.SET, uniqueKey = "coffee")
fun FakeCoffee(): Beverage =
    object : Beverage {
        override val name: String = "beer"
    }

@StringKey("car")
@TestAutoFactory(target = AutoFactoryTarget.MAP)
fun FakeCar(): Vehicle =
    object : Vehicle {
        override val name: String = "truck"
    }

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class SampleAutoFactoryTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Test Bindings
    @Inject
    lateinit var instrument: Instrument

    // Qualified bindings
    @Inject
    lateinit var red: Paint
    @Inject
    @Special
    lateinit var special: Paint

    // Set Bindings
    @Inject
    lateinit var beverages: Beverages

    // Map Bindings
    @Inject
    lateinit var vehicles: Vehicles

    @Before
    fun setup() {
        hiltRule.inject()
    }


    @Test
    fun testBindingIsBindingCorrectType() {
        assertEquals("violin", instrument.name)
    }

    @Test
    fun qualifiedBindingIsBindingCorrectType() {
        assertEquals("red", red.tone)
        assertEquals("blue", special.tone)
    }

    @Test
    fun setBindingsBindingCorrectType() {
        val actualElements = beverages.map { it.name }

        // coffee is replaced with beer by [FakeCoffee]
        assertContains(actualElements, "beer")
        assertContains(actualElements, "tea")
    }

    @Test
    fun mapBindingIsBindingCorrectType() {
        val actualElements = vehicles.mapValues { it.value.name }.values

        // car is replaced with a truck by [FakeCar]
        assertContains(actualElements, "truck")

        assertContains(actualElements, "bike")
    }
}
