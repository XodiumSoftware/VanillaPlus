package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.ServerLinks
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.configDelegate
import java.net.URI

/** Represents a module handling server info mechanics within the system. */
internal object ServerInfoModule : ModuleInterface {
    override val config by configDelegate { Config() }

    init {
        serverLinks()
    }

    /** Configures server links based on the module's configuration. */
    @Suppress("UnstableApiUsage")
    private fun serverLinks() =
        config.serverLinks.forEach { (type, url) ->
            runCatching { URI.create(url) }.getOrNull()?.let { instance.server.serverLinks.setLink(type, it) }
        }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        override var enabled: Boolean = false,
        @Suppress("UnstableApiUsage") var serverLinks: Map<ServerLinks.Type, String> =
            mapOf(
                ServerLinks.Type.WEBSITE to "https://xodium.org/",
                ServerLinks.Type.REPORT_BUG to "https://discord.gg/jusYH9aYUh",
                ServerLinks.Type.STATUS to "https://modrinth.com/server/illyria",
                ServerLinks.Type.COMMUNITY to "https://discord.gg/jusYH9aYUh",
                ServerLinks.Type.COMMUNITY_GUIDELINES to "https://vanillaplus.xodium.org/",
            ),
    ) : ModuleConfigInterface
}
