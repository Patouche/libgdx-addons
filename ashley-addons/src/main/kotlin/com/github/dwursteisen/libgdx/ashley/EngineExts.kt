package com.github.dwursteisen.libgdx.ashley

import com.badlogic.ashley.core.*

inline fun <reified T : Component> Engine.createComponent(): T = this.createComponent(T::class.java)

inline fun <reified T : Component> Engine.createComponentWith(block: T.() -> Unit): T {
    val component = this.createComponent(T::class.java)
    block.invoke(component)
    return component
}


fun Engine.addEntity(builder: Entity.() -> Unit): Entity {
    val entity = this.createEntity()
    builder(entity)
    this.addEntity(entity)
    return entity
}

fun Engine.entity(vararg component: Class<out Component>): Entity = this.getEntitiesFor(Family.all(*component).get()).first()

fun Engine.removeAll(entities: Iterable<Entity>): Unit = entities.forEach { this.removeEntity(it) }

fun Engine.removeAllWith(vararg components: Class<out Component>) {
    val entities = this.getEntitiesFor(Family.all(*components).get()).toList()
    for (e in entities) {
        this.removeEntity(e)
    }
    //removeAll(entities)
}
