@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.entity

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.CommandUtils.playerExecuted
import org.xodium.illyriaplus.bosses.DesertBoss
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.interfaces.MechanicInterface

/** Handles boss spawning and mechanics. */
internal object BossMechanic : MechanicInterface {
    /** List of all boss implementations. */
    private val bosses =
        listOf<BossInterface>(
            DesertBoss,
        )

    /** Map of boss class names to boss implementations for quick lookup. */
    private val bossByName = bosses.associateBy { it.javaClass.simpleName }

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("boss")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .argument("name", StringArgumentType.word())
                            .suggests { _, builder ->
                                bossByName.keys.forEach { builder.suggest(it) }
                                builder.buildFuture()
                            }.playerExecuted { player, ctx ->
                                bossByName[ctx.getArgument("name", String::class.java)]?.spawn(player.location)
                            },
                    ),
                "Spawns a boss at the player's location",
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.boss".lowercase(),
                "Allows use of the boss command",
                PermissionDefault.OP,
            ),
        )

    @EventHandler
    fun on(event: EntityDeathEvent) {
        bosses.forEach { it.onDeath(event.entity) }
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        bosses.forEach { it.onDamage(event.entity as? LivingEntity ?: return) }
    }
}
