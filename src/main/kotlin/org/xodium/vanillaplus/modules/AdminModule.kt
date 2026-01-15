package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.ServerLinks
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.net.URI

/** Represents a module handling admin mechanics within the system. */
internal object AdminModule : ModuleInterface {
    init {
        serverLinks()
    }

    /** Configures server links based on the module's configuration. */
    @Suppress("UnstableApiUsage")
    private fun serverLinks() =
        config.adminModule.serverLinks.forEach { (type, url) ->
            runCatching { URI.create(url) }.getOrNull()?.let { instance.server.serverLinks.setLink(type, it) }
        }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        @Suppress("UnstableApiUsage") var serverLinks: Map<ServerLinks.Type, String> =
            mapOf(
                ServerLinks.Type.WEBSITE to "https://xodium.org/",
                ServerLinks.Type.REPORT_BUG to "https://github.com/XodiumSoftware/VanillaPlus/issues",
                ServerLinks.Type.STATUS to "https://mcsrvstat.us/server/illyria.xodium.org",
                ServerLinks.Type.COMMUNITY to "https://discord.gg/jusYH9aYUh",
            ),
    )
}
