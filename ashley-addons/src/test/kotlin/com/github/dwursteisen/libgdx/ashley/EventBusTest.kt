package com.github.dwursteisen.libgdx.ashley

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * @author Patrick Allain - 9/5/19.
 */
class EventBusTest {

    @Before
    fun setUp() {
        Gdx.app = Mockito.mock(Application::class.java)
        Gdx.input = Mockito.mock(Input::class.java)
    }

    @Test
    fun `Should have register all listener using the DSL`() {

        val eventBus = EventBus(mapOf(1 to "event-1", 2 to "event-2", 3 to "event-3"))

        eventBus
                .registers {
                    onEvents(1) { _, _ -> }
                    onEvents(2, 3) { _, _ -> }
                }

        assertThat(eventBus.listeners).containsKeys(1, 2, 3)
    }

}