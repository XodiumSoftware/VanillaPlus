package org.xodium.vanillaplus.data

import net.kyori.adventure.bossbar.BossBar
import org.xodium.vanillaplus.utils.ExtUtils.mm

/**
 * Represents the data structure for a boss bar in the game.
 * @property name The [name] of the boss bar.
 * @property progress The [progress] of the boss bar, ranging from 0.0 to 1.0.
 * @property color The [color] of the boss bar.
 * @property style The [style] of the boss bar overlay.
 * @property flags Additional [flags] for the boss bar, such as visibility or dark mode.
 */
data class BossBarData(
    val name: String,
    private val progress: Float,
    private val color: BossBar.Color,
    private val style: BossBar.Overlay,
    private val flags: Set<BossBar.Flag> = emptySet()
) {
    /**
     * Converts this [BossBarData] instance to a [BossBar] instance.
     * @return A [BossBar] instance with the properties of this [BossBarData].
     */
    fun toBossBar(): BossBar = BossBar.bossBar(name.mm(), progress, color, style, flags)
}
