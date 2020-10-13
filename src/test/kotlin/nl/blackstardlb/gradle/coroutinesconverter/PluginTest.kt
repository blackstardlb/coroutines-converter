package nl.blackstardlb.gradle.coroutinesconverter

import nl.blackstardlb.gradle.coroutinesconverter.config.CoroutinesConverterExtension
import nl.blackstardlb.gradle.coroutinesconverter.impl.WrapperType
import org.gradle.testfixtures.ProjectBuilder
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.test.Test

class PluginTest {

    @Test
    fun pluginCanBeApplied() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(TestUtils.PLUGIN_ID)

        expectThat(project.plugins.getPlugin(CoroutinesConverterPlugin::class.java)).isNotNull()
    }

    @Test
    fun extensionIsRegistered() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(TestUtils.PLUGIN_ID)

        expectThat(project.coroutinesConverterConfig()).isA<CoroutinesConverterExtension>()
    }
}
