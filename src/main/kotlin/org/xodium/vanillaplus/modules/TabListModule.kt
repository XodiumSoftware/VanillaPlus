@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.util.*
import kotlin.math.roundToInt

/** Represents a module handling tab-list mechanics within the system. */
internal class TabListModule : ModuleInterface<TabListModule.Config> {
    override val config: Config = Config()

    companion object {
        private const val MIN_TPS = 0.0
        private const val MAX_TPS = 20.0
        private const val TPS_DECIMAL_FORMAT = "%.1f"
        private const val MAX_COLOR_VALUE = 255
        private const val COLOR_FORMAT = "#%02X%02X%02X"
    }

    init {
        if (enabled()) {
            instance.server.onlinePlayers.forEach {
                updateTabList(it)
                updatePlayerDisplayName(it)
            }
            // TPS Check.
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable { instance.server.onlinePlayers.forEach { updateTabList(it) } },
                config.initDelayInTicks,
                config.intervalInTicks,
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        updateTabList(event.player)
        updatePlayerDisplayName(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: WeatherChangeEvent) {
        if (!enabled()) return
        event.world.players.forEach { updateTabList(it) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: ThunderChangeEvent) {
        if (!enabled()) return
        event.world.players.forEach { updateTabList(it) }
    }

    /**
     * Update the player's display name in the tab list.
     * @param player the player to update.
     */
    fun updatePlayerDisplayName(player: Player): Unit = player.playerListName(player.displayName())

    /**
     * Update the tab list for the given audience.
     * @param audience the audience to update the tab list for.
     */
    private fun updateTabList(audience: Audience) {
        val joinConfig = JoinConfiguration.separator(Component.newline())
        audience.sendPlayerListHeaderAndFooter(
            Component.join(joinConfig, config.header.mm()),
            Component.join(
                joinConfig,
                config.footer.mm(
                    Placeholder.component("weather", getWeather().mm()),
                    Placeholder.component("tps", getTps().mm()),
                ),
            ),
        )
    }

    /**
     * A function to get the tps of the server.
     * @return The tps of the server.
     */
    private fun getTps(): String {
        val tps = instance.server.tps[0]
        val clampedTps = tps.coerceIn(MIN_TPS, MAX_TPS)
        val ratio = clampedTps / MAX_TPS
        val color = getColorForTps(ratio)
        val formattedTps = String.format(Locale.ENGLISH, TPS_DECIMAL_FORMAT, tps)
        return "<color:$color>$formattedTps</color>"
    }

    /**
     * Calculate a hex colour between red and green based on the provided ratio (0.0 to 1.0).
     * @param ratio The ratio to calculate the colour for.
     * @return The hex colour for the ratio.
     */
    private fun getColorForTps(ratio: Double): String {
        val clamped = ratio.coerceIn(0.0, 1.0)
        val r = (MAX_COLOR_VALUE * (1 - clamped)).roundToInt()
        val g = (MAX_COLOR_VALUE * clamped).roundToInt()
        return String.format(Locale.ENGLISH, COLOR_FORMAT, r, g, 0)
    }

    /**
     * Gets a formatted string representing the current weather in the main world.
     * @return A formatted string representing the weather.
     */
    private fun getWeather(): String {
        val world = instance.server.worlds[0]
        return when {
            world.isThundering -> config.i18n.weatherThundering
            world.hasStorm() -> config.i18n.weatherStorm
            else -> config.i18n.weatherClear
        }
    }

    @Serializable
    data class Config(
        override var enabled: Boolean = true,
        var initDelayInTicks: Long = 0,
        var intervalInTicks: Long = 10,
        var header: List<String> =
            listOf(
                "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}   ${"⚡ IllyriaRPG ⚡".fireFmt()}   ${
                    "]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
                }",
                "",
            ),
        var footer: List<String> =
            listOf(
                "",
                "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}  ${"TPS:".fireFmt()} <tps> ${"|".mangoFmt()} ${
                    "Weather:".fireFmt()
                } <weather>  ${
                    "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
                }",
            ),
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        @Serializable
        data class I18n(
            var weatherThundering: String = "<red>\uD83C\uDF29<reset>",
            var weatherStorm: String = "<yellow>\uD83C\uDF26<reset>",
            var weatherClear: String = "<green>\uD83C\uDF24<reset>",
        )
    }
}
