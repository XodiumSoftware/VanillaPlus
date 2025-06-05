/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.map.MapRenderer
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.mapify.Mapify
import org.xodium.vanillaplus.utils.Utils
import java.util.function.Consumer

/** Represents a module handling map mechanics within the system. */
class MapModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MapModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("mapify")
                .requires { it.sender.hasPermission(Perms.Mapify.USE) }
                .executes { it -> Utils.tryCatch(it) { mapify() } }
                .then(
                    Commands.literal("refresh")
                        .requires { it.sender.hasPermission(Perms.Mapify.REFRESH) }
                        .executes { it -> Utils.tryCatch(it) { mapify() } })
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: MapInitializeEvent) {
        instance.server.scheduler.runTaskAsynchronously(Mapify.INSTANCE, Runnable runTaskAsynchronously@{
            val pluginRenderer: MapRenderer? = Util.getRenderer(event.map)
            if (pluginRenderer == null) return@runTaskAsynchronously

            event.map.renderers.forEach(Consumer { event.map.removeRenderer(it) })
            event.map.addRenderer(pluginRenderer)
        })
    }

    private fun mapify() {

    }
}