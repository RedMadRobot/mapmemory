import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependency
import org.gradle.plugin.use.PluginDependencySpec

fun PluginDependenciesSpec.id(pluginDependency: ProviderConvertible<PluginDependency>): PluginDependencySpec {
    return id(pluginDependency.id)
}

fun PluginDependenciesSpec.id(pluginDependency: Provider<PluginDependency>): PluginDependencySpec {
    return id(pluginDependency.id)
}

val ProviderConvertible<PluginDependency>.id: String get() = asProvider().id

val Provider<PluginDependency>.id: String get() = get().pluginId
