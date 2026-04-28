package org.xodium.illyriaplus.mechanics

import org.bukkit.ServerLinks
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.interfaces.MechanicInterface
import java.net.URI
import kotlin.time.measureTime

/** Represents a module handling server info mechanics within the system. */
@Suppress("UnstableApiUsage")
internal object ServerInfoMechanic : MechanicInterface {
    private val SERVER_LINKS: Map<ServerLinks.Type, String> =
        mapOf(
            ServerLinks.Type.WEBSITE to "https://xodium.org/",
            ServerLinks.Type.REPORT_BUG to "https://discord.gg/jusYH9aYUh",
            ServerLinks.Type.STATUS to "https://modrinth.com/server/illyria",
            ServerLinks.Type.COMMUNITY to "https://discord.gg/jusYH9aYUh",
            ServerLinks.Type.COMMUNITY_GUIDELINES to "https://vanillaplus.xodium.org/",
        )

    override fun register(): Long = super.register() + measureTime { serverLinks() }.inWholeMilliseconds

    /** Configures server links based on the module's configuration. */
    private fun serverLinks() =
        SERVER_LINKS.forEach { (type, url) -> instance.server.serverLinks.setLink(type, URI.create(url)) }
}
