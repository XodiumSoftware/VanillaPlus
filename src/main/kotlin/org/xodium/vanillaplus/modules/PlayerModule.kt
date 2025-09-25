package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
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
                                    val name = StringArgumentType.getString(ctx, "name")
                                    nickname(it.sender as Player, name)
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
        PlayerData.get(event.player).nickname?.let { event.player.displayName(it.mm()) }
    }

    @EventHandler
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return
        val killer = event.entity.killer ?: return

        val entity = event.entity
        val itemStack = ItemStack.of(Material.PLAYER_HEAD)
        val itemMeta = itemStack.itemMeta as SkullMeta
        if (itemMeta.setOwningPlayer(entity)) {
            itemMeta.customName(entity.displayName().append("'s Skull".fireFmt().mm()))
            itemMeta.lore(listOf("${entity.name} killed by ${killer.name}").mm())
            itemStack.itemMeta = itemMeta
            entity.world.dropItemNaturally(entity.location, itemStack)
        }
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
        val newNickname = name.ifBlank { null }
        PlayerData.update(player, PlayerData.get(player).copy(nickname = newNickname))
        player.displayName((newNickname ?: player.name).mm())
        tabListModule.updatePlayerDisplayName(player)
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
