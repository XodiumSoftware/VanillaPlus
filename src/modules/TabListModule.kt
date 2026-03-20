@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.configDelegate

/** Represents a module handling tab-list mechanics within the system. */
internal object TabListModule : ModuleInterface {
    override val config by configDelegate { Config() }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        tablist(event.player)
        event.player.playerListName(event.player.displayName())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: WeatherChangeEvent) = event.world.players.forEach { tablist(it) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: ThunderChangeEvent) = event.world.players.forEach { tablist(it) }

    /**
     * Update the tab list for the given audience.
     * @param audience the audience to update the tab list for.
     */
    private fun tablist(audience: Audience) {
        audience.sendPlayerListHeaderAndFooter(
            MM.deserialize(config.header.joinToString("\n")),
            MM.deserialize(
                config.footer.joinToString("\n"),
                Placeholder.component("weather", MM.deserialize(getWeather())),
            ),
        )
    }

    /**
     * Gets a formatted string representing the current weather in the main world.
     * @return A formatted string representing the weather.
     */
    private fun getWeather(): String =
        with(instance.server.worlds[0]) {
            when {
                isThundering -> config.i18n.weatherThundering
                hasStorm() -> config.i18n.weatherStorm
                else -> config.i18n.weatherClear
            }
        }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        override var enabled: Boolean = false,
        var header: List<String> =
            listOf(
                "<gradient:#FFA751:#FFE259><st>───────────────</st></gradient> " +
                    "<gradient:#CB2D3E:#EF473A>" +
                    "\uD835\uDD74\uD835\uDD91\uD835\uDD91\uD835\uDD9E\uD835\uDD97\uD835\uDD8E\uD835\uDD86" +
                    "</gradient> " +
                    "<gradient:#FFE259:#FFA751><st>───────────────</st></gradient>",
                "",
            ),
        var footer: List<String> =
            listOf(
                "",
                "<gradient:#FFA751:#FFE259><st>──────────</st></gradient>  " +
                    "<gradient:#CB2D3E:#EF473A>Weather:</gradient> <weather> " +
                    " <gradient:#FFE259:#FFA751><st>──────────</st></gradient>",
            ),
        var i18n: I18n = I18n(),
    ) : ModuleConfigInterface {
        /** Represents the internationalization strings for the module. */
        @Serializable
        data class I18n(
            var weatherThundering: String = "<red>\uD83C\uDF29<reset>",
            var weatherStorm: String = "<yellow>\uD83C\uDF26<reset>",
            var weatherClear: String = "<green>\uD83C\uDF24<reset>",
        )
    }
}
