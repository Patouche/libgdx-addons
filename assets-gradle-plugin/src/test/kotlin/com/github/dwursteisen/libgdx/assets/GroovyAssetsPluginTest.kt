package com.github.dwursteisen.libgdx.assets

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GroovyAssetsPluginTest {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    lateinit var buildFile: File

    @Before
    fun setUp() {
        buildFile = temporaryFolder.newFile("build.gradle")

    }
    @Test
    fun `it should create a Assets object in another directory`() {
        buildFile.writeText("""
// tag::configuration[]
plugins {
    id 'assets'
}

assets {
    assetsClass = file("${"$"}buildDir/generated/NewAssets.kt")
}
// end::configuration[]
        """.trimIndent())

        val asset = File(temporaryFolder.newFolder("src", "main", "assets"), "example.txt")
        asset.writeText("hello world")

        val result = GradleRunner.create()
                .withProjectDir(temporaryFolder.root)
                .withArguments("assets")
                .withPluginClasspath()
                .build()

        assert(result.task(":assets")?.outcome == TaskOutcome.SUCCESS)
        val generated = File(temporaryFolder.root, "build/generated/NewAssets.kt")
        assert(generated.isFile)
        assert(generated.readText().contains("example.txt"))
    }

    @Test
    fun `it should create a Assets object with only the filtered assets`() {


        buildFile.writeText("""
// tag::configuration[]
plugins {
    id 'assets'
}

assets {
     includeExts = ["inc", "txt"]
}
// end::configuration[]
        """.trimIndent())

        val tmpFolder = temporaryFolder.newFolder("src", "main", "assets")
        File(tmpFolder, "include.inc").writeText("hello world")
        File(tmpFolder, "include.txt").writeText("hello world")
        File(tmpFolder, "exclude.out").writeText("hello world")

        val result = GradleRunner.create()
                .withProjectDir(temporaryFolder.root)
                .withArguments("assets")
                .withPluginClasspath()
                .build()

        assert(result.task(":assets")?.outcome == TaskOutcome.SUCCESS)
        val generated = File(temporaryFolder.root, "build/generated/Assets.kt")
        assert(generated.isFile)
        assert(generated.readText().contains("include.inc"))
        assert(generated.readText().contains("include.txt"))
        Assert.assertThat(generated.readText(), CoreMatchers.not(CoreMatchers.containsString("exclude.out")))
    }


}
