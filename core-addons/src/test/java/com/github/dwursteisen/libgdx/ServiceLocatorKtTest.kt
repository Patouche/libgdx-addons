package com.github.dwursteisen.libgdx

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

/**
 *
 * @author Patrick Allain - 9/11/19.
 */
class ServiceLocatorKtTest {
    @Test
    fun `Kotlin DSL should register new instance`() {
        ServiceLocator.registers {
            on(Random::class) { Random.Default }
        }

        val locate = ServiceLocator.locate<Random>()

        Assert.assertThat(locate, CoreMatchers.notNullValue())

    }
}