package org.xodium.vanillaplus.modules

import org.bukkit.ServerLinks
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.net.URI

/** Represents a module handling server info mechanics within the system. */
internal object ServerInfoModule : ModuleInterface {
    init {
        serverLinks()
    }

    /** Configures server links based on the module's configuration. */
    @Suppress("UnstableApiUsage")
    private fun serverLinks() =
        Config.SERVER_LINKS.forEach { (type, url) ->
            runCatching { instance.server.serverLinks.setLink(type, URI.create(url)) }
        }

    /** Represents the config of the module. */
    object Config {
        @Suppress("UnstableApiUsage")
        val SERVER_LINKS: Map<ServerLinks.Type, String> =
            mapOf(
                ServerLinks.Type.WEBSITE to "https://xodium.org/",
                ServerLinks.Type.REPORT_BUG to "https://discord.gg/jusYH9aYUh",
                ServerLinks.Type.STATUS to "https://modrinth.com/server/illyria",
                ServerLinks.Type.COMMUNITY to "https://discord.gg/jusYH9aYUh",
                ServerLinks.Type.COMMUNITY_GUIDELINES to "https://vanillaplus.xodium.org/",
            )
    }
}
