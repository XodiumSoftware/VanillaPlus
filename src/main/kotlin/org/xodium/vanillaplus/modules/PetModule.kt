package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

/** Represents a module handling pet mechanics within the system. */
internal class PetModule : ModuleInterface<PetModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (!enabled()) return

        val source = event.player
        if (event.rightClicked !is Player) return

        val target = event.rightClicked as Player
        if (source == target) return
        if (source.inventory.itemInMainHand.type != Material.LEAD) return

        val leashedEntity =
            source.getNearbyEntities(config.transferRadius, config.transferRadius, config.transferRadius).firstOrNull {
                it is LivingEntity && it.isLeashed && it.leashHolder == source
            }

        if (leashedEntity == null) return
        if (leashedEntity !is Tameable) return
        if (!leashedEntity.isTamed || leashedEntity.owner != source) return

        val petName = leashedEntity.customName() ?: leashedEntity.name.mm()

        leashedEntity.owner = target
        leashedEntity.setLeashHolder(null)

        source.inventory.addItem(ItemStack.of(Material.LEAD))

        source.sendActionBar(
            config.sourceTransferMessage.mm(
                Placeholder.component("<pet>", petName),
                Placeholder.component("<target>", target.displayName())
            )
        )

        target.sendActionBar(
            config.targetTransferMessage.mm(
                Placeholder.component("<pet>", petName),
                Placeholder.component("<source>", source.displayName())
            )
        )

        event.isCancelled = true
    }

    data class Config(
        override var enabled: Boolean = true,
        var transferRadius: Double = 10.0,
        var sourceTransferMessage: String = "${"You have transferred".fireFmt()} <pet> ${"to".fireFmt()} <target>",
        var targetTransferMessage: String = "<source> ${"has transferred".fireFmt()} <pet> ${"to you".fireFmt()}",
    ) : ModuleInterface.Config
}