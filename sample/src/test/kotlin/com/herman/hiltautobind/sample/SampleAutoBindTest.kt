package com.herman.hiltautobind.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.herman.hiltautobind.annotations.autobind.AutoBind
import com.herman.hiltautobind.annotations.autobind.AutoBindTarget
import com.herman.hiltautobind.annotations.autobind.TestAutoBind
import com.herman.hiltautobind.sample.binding_examples.Special
import com.herman.hiltautobind.sample.binding_examples.autobind.Animal
import com.herman.hiltautobind.sample.binding_examples.autobind.Animals
import com.herman.hiltautobind.sample.binding_examples.autobind.Fruit
import com.herman.hiltautobind.sample.binding_examples.autobind.Fruits
import com.herman.hiltautobind.sample.binding_examples.autobind.Shape
import com.herman.hiltautobind.sample.binding_examples.autobind.Tool
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.multibindings.StringKey
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Named
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@TestAutoBind
class FakeCircleShape @Inject constructor() : Shape {
    override val name: String = "square"
}

@TestAutoBind(target = AutoBindTarget.SET, uniqueKey = "cat")
class FakeCat @Inject constructor() : Animal {
    override val name: String = "lion"
}

@TestAutoBind(target = AutoBindTarget.MAP) @StringKey("orange")
class FakeOrange @Inject constructor(): Fruit {
    override val name: String = "banana"
}

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class SampleAutoBindTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Test Bindings
    @Inject lateinit var shape: Shape

    // Qualified bindings
    @Inject @Named("hammer") lateinit var hammer : Tool
    @Inject @Special lateinit var specialTool: Tool

    // Set Bindings
    @Inject lateinit var animals: Animals

    // Map Bindings
    @Inject lateinit var fruits: Fruits

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testBindingIsBindingCorrectType() {
        assertEquals("square", shape.name)
    }

    @Test
    fun qualifiedBindingIsBindingCorrectType() {
        assertEquals("hammer", hammer.name)
        assertEquals("special", specialTool.name)
    }

    @Test
    fun setBindingBindingCorrectType() {
        val actualElements = animals.map { it.name }
        // cat is replaced with a lion by [FakeCat]
        assertContains(actualElements, "lion")
        assertContains(actualElements, "dog")
    }

    @Test
    fun mapBindingIsBindingCorrectType(){
        val actualElements = fruits.mapValues { it.value.name }.values
        // orange is replaced with an apple by [FakeOrange]
        assertContains(actualElements, "banana")
        assertContains(actualElements, "apple")
    }
}
