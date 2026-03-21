package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.ServerLinks
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.BookData
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
                ServerLinks.Type.REPORT_BUG to "https://github.com/XodiumSoftware/VanillaPlus/issues",
                ServerLinks.Type.STATUS to "https://modrinth.com/server/illyria",
                ServerLinks.Type.COMMUNITY to "https://discord.gg/jusYH9aYUh",
            ),
        var books: Books = Books(),
    ) : ModuleConfigInterface {
        @Serializable
        data class Books(
            var rules: BookData =
                BookData(
                    title = "<fire>Rules</fire>",
                    pages =
                        listOf(
                            // Page 1: Player Rules (1-7)
                            listOf(
                                "<b><u><dark_aqua>Player Rules:<reset>",
                                "",
                                "<gold>▶ <dark_aqua>01 <dark_gray>| <red>No Griefing",
                                "<gold>▶ <dark_aqua>02 <dark_gray>| <red>No Spamming",
                                "<gold>▶ <dark_aqua>03 <dark_gray>| <red>No Advertising",
                                "<gold>▶ <dark_aqua>04 <dark_gray>| <red>No Cursing/No Constant Cursing",
                                "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Trolling/Flaming",
                                "<gold>▶ <dark_aqua>06 <dark_gray>| <red>No Asking for OP, Ranks, or Items",
                                "<gold>▶ <dark_aqua>07 <dark_gray>| <red>Respect all Players",
                            ),
                            // Page 2: Player Rules (8-13)
                            listOf(
                                "<gold>▶ <dark_aqua>08 <dark_gray>| <red>Obey Staff they are the Law Enforcers",
                                "<gold>▶ <dark_aqua>09 <dark_gray>| <red>No Racist or Sexist Remarks",
                                "<gold>▶ <dark_aqua>10 <dark_gray>| <red>No Mods/Hacks",
                                "<gold>▶ <dark_aqua>12 <dark_gray>| <red>No 1x1 Towers",
                                "<gold>▶ <dark_aqua>13 <dark_gray>| <red>Build in (Fantasy)Medieval style",
                            ),
                            // Page 3: Mod/Admin Rules
                            listOf(
                                "<b><u><dark_aqua>Mod/Admin Rules:<reset>",
                                "",
                                "<gold>▶ <dark_aqua>01 <dark_gray>| <red>Be Responsible with the power you are given as staff",
                                "<gold>▶ <dark_aqua>02 <dark_gray>| <red>Do not spawn blocks or items for other players",
                                "<gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items",
                                "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse",
                            ),
                        ),
                ),
        )
    }
}
