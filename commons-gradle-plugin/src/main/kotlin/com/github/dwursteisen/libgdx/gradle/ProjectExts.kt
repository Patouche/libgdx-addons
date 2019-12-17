package com.github.dwursteisen.libgdx.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

@Suppress("UnstableApiUsage")
inline fun <reified T> Project.createProperty(): Property<T> {
    return this.objects.property(T::class.java)
}
@Suppress("UnstableApiUsage")
inline fun <reified T> Project.createListProperty(): ListProperty<T> {
    return this.objects.listProperty(T::class.java)
}
