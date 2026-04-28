@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.illyriaplus.enchantments.EarthrendEnchantment
import org.xodium.illyriaplus.enchantments.EmbertreadEnchantment
import org.xodium.illyriaplus.enchantments.FeatherFallingEnchantment
import org.xodium.illyriaplus.enchantments.FrostbindEnchantment
import org.xodium.illyriaplus.enchantments.InfernoEnchantment
import org.xodium.illyriaplus.enchantments.NimbusEnchantment
import org.xodium.illyriaplus.enchantments.QuakeEnchantment
import org.xodium.illyriaplus.enchantments.SilkTouchEnchantment
import org.xodium.illyriaplus.enchantments.SkysunderEnchantment
import org.xodium.illyriaplus.enchantments.TempestEnchantment
import org.xodium.illyriaplus.enchantments.TetherEnchantment
import org.xodium.illyriaplus.enchantments.VerdanceEnchantment
import org.xodium.illyriaplus.enchantments.VoidpullEnchantment
import org.xodium.illyriaplus.enchantments.WitherbrandEnchantment
import org.xodium.illyriaplus.mechanics.BookMechanic
import org.xodium.illyriaplus.mechanics.ChatMechanic
import org.xodium.illyriaplus.mechanics.ChiseledBookshelfMechanic
import org.xodium.illyriaplus.mechanics.DimensionMechanic
import org.xodium.illyriaplus.mechanics.InventoryMechanic
import org.xodium.illyriaplus.mechanics.LocatorMechanic
import org.xodium.illyriaplus.mechanics.MotdMechanic
import org.xodium.illyriaplus.mechanics.OpenableMechanic
import org.xodium.illyriaplus.mechanics.PlayerMechanic
import org.xodium.illyriaplus.mechanics.ScoreBoardMechanic
import org.xodium.illyriaplus.mechanics.ServerInfoMechanic
import org.xodium.illyriaplus.mechanics.SitMechanic
import org.xodium.illyriaplus.mechanics.TameableMechanic
import org.xodium.illyriaplus.mechanics.entity.BatMechanic
import org.xodium.illyriaplus.mechanics.entity.DisableEntityMechanic
import org.xodium.illyriaplus.mechanics.entity.GriefingMechanic
import org.xodium.illyriaplus.mechanics.entity.HuskMechanic
import org.xodium.illyriaplus.mechanics.entity.SpawnEggMechanic
import org.xodium.illyriaplus.recipes.ChainmailRecipe
import org.xodium.illyriaplus.recipes.DiamondRecycleRecipe
import org.xodium.illyriaplus.recipes.PaintingRecipe
import org.xodium.illyriaplus.recipes.RottenFleshRecipe
import org.xodium.illyriaplus.recipes.WoodLogRecipe

/** Main class of the plugin. */
internal class IllyriaCore : JavaPlugin() {
    companion object {
        lateinit var instance: IllyriaCore
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
                DisableEntityMechanic,
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

        val sum = enchantments.sumOf { it.registerEvents() }

        logger.info(
            "Registered: ${enchantments.size} enchantment event listener(s) | Took ${sum}ms",
        )
    }

    /**
     * Disable the plugin and log the message.
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String): Nothing {
        logger.severe(msg)
        server.pluginManager.disablePlugin(instance)
        throw IllegalStateException(msg)
    }
}
