/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.TimeUtils
import kotlin.math.roundToInt

/** Represents a module handling tab-list mechanics within the system. */
class TabListModule : ModuleInterface<TabListModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

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
                config.initDelay,
                config.interval
            )
        }
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        updateTabList(event.player)
        updatePlayerDisplayName(event.player)
    }

    @EventHandler
    fun on(event: WeatherChangeEvent) {
        if (!enabled()) return
        event.world.players.forEach { updateTabList(it) }
    }

    @EventHandler
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
                    Placeholder.component("tps", getTps().mm())
                )
            )
        )
    }

    /**
     * A function to get the tps of the server.
     * @return The tps of the server.
     */
    private fun getTps(): String {
        val tps = instance.server.tps[0]
        val clampedTps = tps.coerceIn(0.0, 20.0)
        val ratio = clampedTps / 20.0
        val color = getColorForTps(ratio)
        val formattedTps = String.format("%.1f", tps)
        return "<color:$color>$formattedTps</color>"
    }

    /**
     * Calculate a hex colour between red and green based on the provided ratio (0.0 to 1.0).
     * @param ratio The ratio to calculate the colour for.
     * @return The hex colour for the ratio.
     */
    private fun getColorForTps(ratio: Double): String {
        val clamped = ratio.coerceIn(0.0, 1.0)
        val r = (255 * (1 - clamped)).roundToInt()
        val g = (255 * clamped).roundToInt()
        return String.format("#%02X%02X%02X", r, g, 0)
    }

    /**
     * Gets a formatted string representing the current weather in the main world.
     * @return A formatted string representing the weather.
     */
    private fun getWeather(): String {
        val world = instance.server.worlds[0]
        return when {
            world.isThundering -> "<red>\uD83C\uDF29<reset>"
            world.hasStorm() -> "<yellow>\uD83C\uDF26<reset>"
            else -> "<green>\uD83C\uDF24<reset>"
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var initDelay: Long = 0L,
        var interval: Long = TimeUtils.seconds(10),
        var header: List<String> = listOf(
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}   ${"⚡ IllyriaRPG ⚡".fireFmt()}   ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }",
            ""
        ),
        var footer: List<String> = listOf(
            "",
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}  ${"TPS:".fireFmt()} <tps> ${"|".mangoFmt()} ${
                "Weather:".fireFmt()
            } <weather>  ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }"
        ),
    ) : ModuleInterface.Config
}