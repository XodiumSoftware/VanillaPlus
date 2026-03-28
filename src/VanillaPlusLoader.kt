package org.xodium.vanillaplus

import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository

/** Main loader class of the plugin. */
@Suppress("UnstableApiUsage", "Unused")
internal class VanillaPlusLoader : PluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        val resolver = MavenLibraryResolver()

        resolver.addRepository(
            RemoteRepository.Builder("xenondevs", "default", "https://repo.xenondevs.xyz/releases/").build(),
        )
        resolver.addDependency(Dependency(DefaultArtifact("xyz.xenondevs.invui:invui:pom:1.49"), null))
        resolver.addDependency(Dependency(DefaultArtifact("xyz.xenondevs.invui:invui-kotlin:1.49"), null))

        classpathBuilder.addLibrary(resolver)
    }
}
