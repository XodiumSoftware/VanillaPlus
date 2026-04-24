@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.enchantments.EarthrendEnchantment
import org.xodium.vanillaplus.enchantments.EmbertreadEnchantment
import org.xodium.vanillaplus.enchantments.FeatherFallingEnchantment
import org.xodium.vanillaplus.enchantments.FrostbindEnchantment
import org.xodium.vanillaplus.enchantments.InfernoEnchantment
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
import org.xodium.vanillaplus.enchantments.QuakeEnchantment
import org.xodium.vanillaplus.enchantments.SilkTouchEnchantment
import org.xodium.vanillaplus.enchantments.SkysunderEnchantment
import org.xodium.vanillaplus.enchantments.TempestEnchantment
import org.xodium.vanillaplus.enchantments.TetherEnchantment
import org.xodium.vanillaplus.enchantments.VerdanceEnchantment
import org.xodium.vanillaplus.enchantments.VoidpullEnchantment
import org.xodium.vanillaplus.enchantments.WitherbrandEnchantment
import org.xodium.vanillaplus.modules.BookModule
import org.xodium.vanillaplus.modules.ChatModule
import org.xodium.vanillaplus.modules.ChiseledBookshelfModule
import org.xodium.vanillaplus.modules.DimensionModule
import org.xodium.vanillaplus.modules.EntityModule
import org.xodium.vanillaplus.modules.InventoryModule
import org.xodium.vanillaplus.modules.LocatorModule
import org.xodium.vanillaplus.modules.MotdModule
import org.xodium.vanillaplus.modules.OpenableModule
import org.xodium.vanillaplus.modules.PlayerModule
import org.xodium.vanillaplus.modules.ScoreBoardModule
import org.xodium.vanillaplus.modules.ServerInfoModule
import org.xodium.vanillaplus.modules.SitModule
import org.xodium.vanillaplus.modules.TameableModule
import org.xodium.vanillaplus.recipes.ChainmailRecipe
import org.xodium.vanillaplus.recipes.DiamondRecycleRecipe
import org.xodium.vanillaplus.recipes.PaintingRecipe
import org.xodium.vanillaplus.recipes.RottenFleshRecipe
import org.xodium.vanillaplus.recipes.WoodLogRecipe

/** Main class of the plugin. */
internal class VanillaPlus : JavaPlugin() {
    companion object {
        lateinit var instance: VanillaPlus
            private set
    }

    init {
        instance = this
    }

    override fun onEnable() {
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

        val modules =
            listOf(
                BookModule,
                ChatModule,
                ChiseledBookshelfModule,
                DimensionModule,
                EntityModule,
                InventoryModule,
                LocatorModule,
                MotdModule,
                OpenableModule,
                PlayerModule,
                ServerInfoModule,
                ScoreBoardModule,
                SitModule,
                TameableModule,
            )

        logger.info(
            "Registered: ${modules.size} module(s) | Took ${modules.sumOf { it.register() }}ms",
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
            "Registered: ${enchantments.size} enchantment event listener(s) | Took ${enchantments.sumOf { it.registerEvents() }}ms",
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
