package org.xodium.vanillaplus.features

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a feature handling pet mechanics within the system. */
internal object PetFeature : FeatureInterface {
    private val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        val source = event.player
        val target = event.rightClicked as? Player ?: return

        if (source == target) return
        if (source.inventory.itemInMainHand.type != Material.LEAD) return

        val leashedEntity = findLeashedPet(source) ?: return

        if (!isTransferablePet(leashedEntity, source)) return

        transferPetOwnership(source, target, leashedEntity)
        event.isCancelled = true
    }

    /**
     * Checks if a pet can be transferred to another player.
     * @param pet The tameable entity to check.
     * @param owner The player attempting to transfer ownership.
     * @return `true` if the pet is tamed and owned by the player, `false` otherwise.
     */
    private fun isTransferablePet(
        pet: Tameable,
        owner: Player,
    ): Boolean = pet.isTamed && pet.owner == owner

    /**
     * Transfers ownership of a pet from one player to another.
     * @param source The original owner of the pet.
     * @param target The new owner of the pet.
     * @param pet The tameable entity being transferred.
     */
    private fun transferPetOwnership(
        source: Player,
        target: Player,
        pet: Tameable,
    ) {
        pet.owner = target
        pet.setLeashHolder(null)

        returnLeadToSource(source)
        notifyTransfer(source, target, pet.customName() ?: pet.name.mm())
    }

    /**
     * Attempts to return a lead to the player's inventory, dropping it if inventory is full.
     * @param player The player to return the lead to.
     */
    private fun returnLeadToSource(player: Player) {
        player.inventory
            .addItem(ItemStack(Material.LEAD))
            .takeIf { it.isNotEmpty() }
            ?.let { player.world.dropItem(player.location, ItemStack(Material.LEAD)) }
    }

    /**
     * Finds the first leashed pet owned by the player within the configured radius.
     * @param player The player to search around.
     * @return The found tameable entity or `null` if none exists.
     */
    private fun findLeashedPet(player: Player): Tameable? =
        player
            .getNearbyEntities(
                config.transferRadius.toDouble(),
                config.transferRadius.toDouble(),
                config.transferRadius.toDouble(),
            ).filterIsInstance<LivingEntity>()
            .firstOrNull { it.isLeashed && it.leashHolder == player }
            as? Tameable

    /**
     * Notifies both players about the pet transfer via action bar messages.
     * @param source The original owner of the pet.
     * @param target The new owner of the pet.
     * @param petName The display name of the transferred pet.
     */
    private fun notifyTransfer(
        source: Player,
        target: Player,
        petName: Component,
    ) {
        source.sendActionBar(
            config.i18n.sourceTransfer.mm(
                Placeholder.component("<pet>", petName),
                Placeholder.component("<target>", target.displayName()),
            ),
        )

        target.sendActionBar(
            config.i18n.targetTransfer.mm(
                Placeholder.component("<pet>", petName),
                Placeholder.component("<source>", source.displayName()),
            ),
        )
    }

    data class Config(
        var transferRadius: Int = 10,
        var i18n: I18n = I18n(),
    ) {
        data class I18n(
            var sourceTransfer: String =
                "<gradient:#CB2D3E:#EF473A>You have transferred</gradient> <pet> " +
                    "<gradient:#CB2D3E:#EF473A>to</gradient> <target>",
            var targetTransfer: String =
                "<source> <gradient:#CB2D3E:#EF473A>has transferred</gradient> <pet> " +
                    "<gradient:#CB2D3E:#EF473A>to you</gradient>",
        )
    }
}
