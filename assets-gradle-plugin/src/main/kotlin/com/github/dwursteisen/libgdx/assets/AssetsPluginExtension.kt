package com.github.dwursteisen.libgdx.assets

import com.github.dwursteisen.libgdx.gradle.createListProperty
import com.github.dwursteisen.libgdx.gradle.createProperty
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.internal.impldep.org.mozilla.javascript.ast.ObjectProperty
import java.io.File

open class AssetsPluginExtension(project: Project) {
    /**
     * Which directory should be scan so all files will be referenced in the Assets object.
     */
    val assetsDirectory = project.createProperty<FileCollection>()
        .value(project.files("src/main/assets"))

    /**
     * Which package to create for Assets objects.
     */
    val assetsPackage = project.createProperty<String>()
            .value("")

    /**
     * Which class (aka Assets object) will reference all assets name.
     */
    val assetsClass = project.createProperty<File>()
        .value(project.buildDir.resolve("generated/Assets.kt"))

    /**
     * Which extensions should be included in the result Assets object.
     */
    val includeExts = project.createListProperty<String>()
            .empty()
}
