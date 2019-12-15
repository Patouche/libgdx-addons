package com.github.dwursteisen.libgdx

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <reified T> locate(): Locator<T> = Locator(T::class.java)

class Locator<T>(private val clazz: Class<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ServiceLocator.get(clazz)
    }
}

object ServiceLocator {

    private var cache: Map<Class<out Any>, Any> = emptyMap()

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(clazz: Class<out T>): T {
        val result = cache[clazz] ?: tryCreateInstance(clazz) ?: locatingError(clazz)
        return result as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> tryCreateInstance(clazz: Class<out T>): T? {
        val emptyConstructor = clazz.constructors.singleOrNull { it.parameterCount == 0 }
        emptyConstructor ?: return null

        val newInstance = emptyConstructor.newInstance()
        register(newInstance, clazz)
        return newInstance as T

    }

    private fun <T> locatingError(clazz: Class<T>): Nothing {
        throw IllegalArgumentException("Impossible to locate any registered class of type $clazz or to create a default instance of it.")
    }

    inline fun <reified T> locate(): T {
        return this[T::class.java]
    }

    fun register(instance: Any, clazz: Class<out Any>) {
        cache += clazz to instance
    }

    @DslMarker
    annotation class ServiceLocatorRegisterDsl

    @ServiceLocatorRegisterDsl
    class ServiceLocatorRegister : ArrayList<Pair<Class<out Any>, () -> Any>>() {
        fun on(clazz: Class<out Any>, block: () -> Any) {
            this.add(clazz to block)
        }

        fun on(clazz: KClass<out Any>, block: () -> Any) = on(clazz.java, block)
    }

    fun registers(configure: ServiceLocatorRegister.() -> Unit) {
        ServiceLocatorRegister().apply(configure).forEach { this.register(it.second.invoke(), it.first) }
    }

}