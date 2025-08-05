@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling mobs mechanics within the system. */
internal class MobsModule : ModuleInterface<MobsModule.Config> {
    override val config: Config = Config()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("settings")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { guis()[0].open(ctx.source.sender as Player) } },
                "Opens the mob settings gui",
                emptyList(),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.mob-settings.gui".lowercase(),
                "Allows to access the mob settings gui",
                PermissionDefault.OP,
            ),
        )

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (!enabled()) return
        if (when (event.entity) {
                is Creeper -> config.disableCreeperGrief
                is Wither -> config.disableWitherGrief
                is WitherSkull -> config.disableWitherSkullGrief
                is EnderDragon -> config.disableEnderDragonGrief
                else -> false
            }
        ) {
            event.blockList().clear()
        }
    }

    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (!enabled() || !config.disableGhastGrief) return

        val projectile = event.entity
        if (projectile is Fireball && projectile.shooter is Ghast) {
            event.hitBlock?.let {
                event.isCancelled = true
                projectile.remove()
            }
        }
    }

    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (!enabled()) return
        if (event.entity is Enderman && config.disableEndermanGrief) event.isCancelled = true
    }

    @EventHandler
    fun on(event: BlockIgniteEvent) {
        if (!enabled()) return
        if (event.ignitingEntity is Blaze && config.disableBlazeGrief) event.isCancelled = true
    }

    override fun guis(): List<Gui> =
        listOf(
            buildGui {
                containerType = chestContainer { rows = 1 }
                spamPreventionDuration = config.guiSpamPrevention
                title(config.guiTitle.mm())
                statelessComponent {
                    it[1, 5] =
                        ItemBuilder
                            .from(Material.BAKED_POTATO)
                            .name("item_name_sample".fireFmt().mm())
                            .asGuiItem()
                }
            },
        )

    data class Config(
        override var enabled: Boolean = true,
        var guiTitle: String = "Mobs Settings".fireFmt(),
        var guiSpamPrevention: Duration = 1.seconds,
        var disableCreeperGrief: Boolean = true,
        var disableGhastGrief: Boolean = true,
        var disableWitherGrief: Boolean = true,
        var disableWitherSkullGrief: Boolean = true,
        var disableEnderDragonGrief: Boolean = true,
        var disableEndermanGrief: Boolean = true,
        var disableBlazeGrief: Boolean = true,
    ) : ModuleInterface.Config
}
