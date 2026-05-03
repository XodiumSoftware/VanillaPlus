@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.illyriaplus.enchantments.*
import org.xodium.illyriaplus.mechanics.*
import org.xodium.illyriaplus.mechanics.entity.BatMechanic
import org.xodium.illyriaplus.mechanics.entity.GriefingMechanic
import org.xodium.illyriaplus.mechanics.entity.HuskMechanic
import org.xodium.illyriaplus.mechanics.entity.SpawnEggMechanic
import org.xodium.illyriaplus.recipes.*

/** Main class of the plugin. */
internal class IllyriaPlus : JavaPlugin() {
    companion object {
        lateinit var instance: IllyriaPlus
            private set
    }

    override fun onEnable() {
        instance = this

        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version.substringBefore("+"))) disablePlugin(unsupportedVersionMsg)

        val recipes =
            listOf(
                ChainmailRecipe,
                DiamondRecycleRecipe,
                PaintingRecipe,
                RottenFleshRecipe,
                WoodLogRecipe,
            )

        logger.info(
            "Registered: ${recipes.sumOf { it.recipes.size }} recipes(s) | Took ${recipes.sumOf { it.register() }}ms",
        )

        val mechanics =
            listOf(
                BatMechanic,
                BookMechanic,
                ChatMechanic,
                ChiseledBookshelfMechanic,
                DimensionMechanic,
                GriefingMechanic,
                HuskMechanic,
                InventoryMechanic,
                LocatorMechanic,
                MotdMechanic,
                OpenableMechanic,
                PlayerMechanic,
                ScoreBoardMechanic,
                ServerInfoMechanic,
                SitMechanic,
                SpawnEggMechanic,
                TameableMechanic,
            )

        logger.info(
            "Registered: ${mechanics.size} module(s) | Took ${mechanics.sumOf { it.register() }}ms",
        )

        val enchantments =
            listOf(
                EarthrendEnchantment,
                EmbertreadEnchantment,
                FeatherFallingEnchantment,
                FrostbindEnchantment,
                InfernoEnchantment,
                NimbusEnchantment,
                QuakeEnchantment,
                SilkTouchEnchantment,
                SkysunderEnchantment,
                TempestEnchantment,
                TetherEnchantment,
                VerdanceEnchantment,
                VoidpullEnchantment,
                WitherbrandEnchantment,
            )

        logger.info(
            "Registered: ${enchantments.size} enchantment events | Took ${enchantments.sumOf { it.register() }}ms",
        )
    }

    /**
     * Disable the plugin and log the message.
     *
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String): Nothing {
        logger.severe(msg)
        server.pluginManager.disablePlugin(instance)
        throw IllegalStateException(msg)
    }
}
