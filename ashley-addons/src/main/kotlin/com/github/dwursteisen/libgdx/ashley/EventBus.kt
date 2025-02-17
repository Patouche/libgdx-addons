package com.github.dwursteisen.libgdx.ashley

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.github.dwursteisen.libgdx.ashley.fsm.Event
import com.github.dwursteisen.libgdx.ashley.fsm.StateMachineSystem
import ktx.log.debug

interface EventListener {
    fun onEvent(event: Event, eventData: EventData)
}

class EventData(var event: Int = Int.MIN_VALUE, var target: Entity? = null, var body: Any? = null) : Pool.Poolable {

    override fun reset() {
        event = Int.MIN_VALUE
        target = null
        body = null
    }
}

data class EventTimer(var timer: Float = 0f, val event: Event, val data: EventData)



/**
 * Simple Event bus.
 *
 * - register listener through register method
 * - The update method should be called from the main loop
 *
 */
class EventBus(private val eventMapper: Map<Int, String> = emptyMap()) {

    private val pool: Pool<EventData> = object : Pool<EventData>() {
        override fun newObject(): EventData = EventData()
    }

    @DslMarker
    annotation class RegisterListenerDsl

    @RegisterListenerDsl
    class Listeners : ArrayList<Pair<List<Event>, (Event, EventData) -> Unit>>() {
        fun onEvents(vararg events: Event, function: (Event, EventData) -> Unit) {
            this.add(events.toList() to function)
        }
    }

    class EventInputProcessor(private val bus: EventBus) : InputAdapter() {

        private val touchDown = Vector2()
        private val touchUp = Vector2()

        override fun keyDown(keycode: Int): Boolean {
            val keyEventData = bus.createEventData()
            keyEventData.body = keycode
            bus.emit(StateMachineSystem.EVENT_KEY, keyEventData)
            return super.keyDown(keycode)
        }

        override fun keyUp(keycode: Int): Boolean {
            val keyEventData = bus.createEventData()
            keyEventData.body = keycode
            bus.emit(StateMachineSystem.EVENT_KEY_UP, keyEventData)
            return super.keyDown(keycode)
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            val screenTouchData = bus.createEventData()
            touchDown.set(screenX.toFloat(), screenY.toFloat())
            screenTouchData.body = touchDown
            bus.emit(StateMachineSystem.EVENT_TOUCHED, screenTouchData)
            return super.touchDown(screenX, screenY, pointer, button)
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            touchDown.set(screenX.toFloat(), screenY.toFloat())
            return super.touchDragged(screenX, screenY, pointer)
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            val screenTouchData = bus.createEventData()
            touchUp.set(screenX.toFloat(), screenY.toFloat())
            screenTouchData.body = touchDown to touchUp
            bus.emit(StateMachineSystem.EVENT_SLIDE, screenTouchData)
            return super.touchUp(screenX, screenY, pointer, button)
        }
    }

    init {
        resetInputProcessor()
    }

    private var currentProcessor: InputProcessor? = null

    private fun resetInputProcessor() {
        currentProcessor = Gdx.input.inputProcessor
        val processor = EventInputProcessor(this)
        if (currentProcessor == null) {
            Gdx.input.inputProcessor = processor
        } else {
            Gdx.input.inputProcessor = InputMultiplexer(processor, currentProcessor)
        }
    }

    fun clearInputProcessor() {
        Gdx.input.inputProcessor = null
    }

    internal var listeners: MutableMap<Event, List<EventListener>> = mutableMapOf()

    private var emitter: MutableList<Pair<Event, EventData>> = mutableListOf()
    private var emitterMirror: MutableList<Pair<Event, EventData>> = mutableListOf()

    private var emitterLatter: MutableList<EventTimer> = mutableListOf()
    private var emitterLatterMirror: MutableList<EventTimer> = mutableListOf()

    @Deprecated("This method will be marked as private in future release. Use #emit + lambda version instead.")
    fun createEventData(): EventData = pool.obtain()

    fun emit(event: Event, entity: Entity, data: EventData = createEventData()) {
        data.target = entity
        emit(event, data)
    }

    fun emit(event: Event, data: EventData = createEventData()) {
        data.event = event
        debug { "will emit ${eventMapper[event] ?: "??"} (id : $event)" }
        emitter.add(event to data)
    }

    fun emit(event: Event, vararg entities: Entity, data: EventData = createEventData()) {
        entities.forEach {
            val cpy = createEventData()
            cpy.body = data.body
            emit(event, it, cpy)
        }
    }

    fun emitData(event: Event, body: () -> Any? = { null }) {
        val data = createEventData()
        data.body = body()
        emit(event, data)
    }

    fun emitLater(delta: Float, event: Event, entity: Entity, data: EventData = createEventData()) {
        data.target = entity
        emitLater(delta, event, data)
    }

    fun emitLater(delta: Float, event: Event, data: EventData = createEventData()) {
        data.event = event
        val timer = EventTimer(delta, event, data)
        emitterLatter.add(timer)
    }

    fun register(eventListener: EventListener, vararg events: Event) {
        events.forEach {
            val lst = listeners[it] ?: emptyList()
            listeners[it] = lst + eventListener
        }
    }

    fun registers(block: Listeners.() -> Unit): EventBus {
        Listeners().apply(block).toMap().forEach { (events, eventListener) ->
            this.register(
                    eventListener = object : EventListener {
                        override fun onEvent(event: Event, eventData: EventData) {
                            eventListener.invoke(event, eventData)
                        }
                    },
                    events = *events.toIntArray()
            )
        }
        return this
    }

    fun update(delta: Float) {

        emitterLatter.forEach { it.timer -= delta }
        val toEmitNow = emitterLatter.filter { it.timer < 0 }

        emitterLatterMirror.addAll(toEmitNow)
        emitterLatterMirror.sortBy { it.event }

        emitterMirror.addAll(emitter)
        emitterMirror.sortBy { it.first }

        debug { emitterMirror.joinToString("\n") { "emit ${eventMapper[it.first] ?: "??"} (id : ${it.first})" } }

        emitterLatterMirror.forEach { invoke(it.event, it.data) }
        emitterMirror.forEach { invoke(it.first, it.second) }

        val eventDataToReset = toEmitNow.map { it.data } + emitterMirror.map { it.second }

        emitterLatter.removeAll(toEmitNow)
        emitter.removeAll(emitterMirror)

        eventDataToReset.forEach { pool.free(it) }

        emitterMirror.clear()
        emitterLatterMirror.clear()
    }

    private fun invoke(event: Event, data: EventData) {
        listeners[event]?.forEach { lst -> lst.onEvent(event, data) }
    }
}
