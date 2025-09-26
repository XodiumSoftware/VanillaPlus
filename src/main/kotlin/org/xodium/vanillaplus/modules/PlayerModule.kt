package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.pt
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

/** Represents a module handling player mechanics within the system. */
internal class PlayerModule(
    private val tabListModule: TabListModule,
) : ModuleInterface<PlayerModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled && tabListModule.enabled()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("nickname")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            nickname(it.sender as Player, "")
                        }
                    }.then(
                        Commands
                            .argument("name", StringArgumentType.greedyString())
                            .executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    nickname(it.sender as Player, StringArgumentType.getString(ctx, "name"))
                                }
                            },
                    ),
                "Allows players to set or remove their nickname",
                listOf("nick"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.nickname".lowercase(),
                "Allows use of the nickname command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        PlayerData.get(event.player)?.nickname?.let { event.player.displayName(it.mm()) }
    }

    @EventHandler
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return
        dropPlayerHead(event.entity, event.entity.killer ?: return)
    }

    /**
     * Sets the nickname of the player to the given name.
     * @param player The player whose nickname is to be set.
     * @param name The new nickname for the player.
     */
    private fun nickname(
        player: Player,
        name: String,
    ) {
        PlayerData.set(player, name)
        player.displayName((name).mm())
        tabListModule.updatePlayerDisplayName(player)
    }

    /**
     * Drops the player's head with custom metadata when killed by another player.
     * @param entity The player who died.
     * @param killer The player who killed the entity.
     */
    @Suppress("UnstableApiUsage")
    private fun dropPlayerHead(
        entity: Player,
        killer: Player,
    ) {
        entity.world.dropItemNaturally(
            entity.location,
            ItemStack.of(Material.PLAYER_HEAD).apply {
                setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(entity.playerProfile))
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    config.l18n.playerHeadName
                        .replace("<player>", entity.displayName().pt())
                        .mm(),
                )
                setData(
                    DataComponentTypes.LORE,
                    ItemLore
                        .lore()
                        .addLine(
                            config.l18n.playerHeadLore
                                .replace("<player>", entity.name)
                                .replace("<killer>", killer.name)
                                .mm(),
                        ).build(),
                )
            },
        )
    }

    data class Config(
        override var enabled: Boolean = true,
        var l18n: L18n = L18n(),
    ) : ModuleInterface.Config {
        data class L18n(
            var playerHeadName: String = "<player>’s Skull".fireFmt(),
            var playerHeadLore: String = "<player> killed by <killer>",
        )
    }
}
