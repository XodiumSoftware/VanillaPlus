@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.illyriaplus.bosses.end.EndBarrensBoss
import org.xodium.illyriaplus.bosses.end.EndHighlandsBoss
import org.xodium.illyriaplus.bosses.end.EndMidlandsBoss
import org.xodium.illyriaplus.bosses.end.SmallEndIslandsBoss
import org.xodium.illyriaplus.bosses.nether.*
import org.xodium.illyriaplus.bosses.overworld.*
import org.xodium.illyriaplus.enchantments.*
import org.xodium.illyriaplus.managers.BossManager
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

        val bosses =
            listOf(
                // Overworld
                BadlandsBoss,
                BeachBoss,
                BirchForestBoss,
                CherryGroveBoss,
                DarkForestBoss,
                DesertBoss,
                DripstoneCavesBoss,
                FlowerForestBoss,
                ForestBoss,
                JungleBoss,
                LushCavesBoss,
                MangroveSwampBoss,
                MountainBoss,
                MushroomBoss,
                OceanBoss,
                PaleGardenBoss,
                PlainsBoss,
                SavannaBoss,
                SnowBoss,
                SwampBoss,
                TaigaBoss,
                // Nether
                BasaltDeltasBoss,
                CrimsonForestBoss,
                NetherWastesBoss,
                SoulSandValleyBoss,
                WarpedForestBoss,
                // End
                EndBarrensBoss,
                EndHighlandsBoss,
                EndMidlandsBoss,
                SmallEndIslandsBoss,
            )

        logger.info(
            "Registered: ${bosses.size} boss(es) | Took ${bosses.sumOf { it.register() }}ms",
        )

        // Register biome mappings for natural spawning
        bosses.forEach { BossManager.registerBiomeBoss(it.biome, it) }
        logger.info("Registered: ${bosses.size} biome boss mapping(s).")

        // Register BossManager events for chunk spawn checking
        server.pluginManager.registerEvents(BossManager, instance)
        logger.info("BossManager events registered.")

        BossManager.startTickScheduler()
        logger.info("Boss tick scheduler started.")
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
