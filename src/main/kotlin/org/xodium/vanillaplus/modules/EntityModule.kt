@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.engines.ExpressionEngine
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.HorsePDC.sold
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.bukkit.Sound as BukkitSound

/** Represents a module handling entity mechanics within the system. */
internal class EntityModule : ModuleInterface<EntityModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (!enabled()) return
        if (shouldCancelGrief(event.entity)) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (!enabled()) return
        if (shouldCancelGrief(event.entity)) event.blockList().clear()
    }

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (!enabled()) return

        val entity = event.rightClicked

        if (entity !is WanderingTrader) return

        handleHorseTrade(event.player, entity)
        event.isCancelled = true
    }

    /**
     * Determines whether an entity's griefing behaviour should be cancelled based on configuration settings.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's griefing behaviour should be cancelled; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean =
        when (entity) {
            is WitherSkull -> config.disableWitherGrief
            is Fireball -> config.disableGhastGrief
            is Blaze -> config.disableBlazeGrief
            is Creeper -> config.disableCreeperGrief
            is EnderDragon -> config.disableEnderDragonGrief
            is Enderman -> config.disableEndermanGrief
            is Wither -> config.disableWitherGrief
            else -> false
        }

    /**
     * Handles the horse-trading logic when a player interacts with a wandering trader.
     * @param player The player initiating the trade.
     * @param trader The wandering trader entity.
     */
    private fun handleHorseTrade(
        player: Player,
        trader: WanderingTrader,
    ) {
        val horse = findLeashedHorse(player) ?: return
        val amount = calculateTradeValue(horse)

        if (amount <= 0 || horse.sold()) {
            player.playSound(config.horseTradeDeniedSound.toSound())
            return
        }

        horse.sold(true)
        horse.isTamed = false
        horse.removeWhenFarAway = true
        horse.setLeashHolder(trader)

        player.inventory.addItem(ItemStack.of(config.horseTradeMaterial, amount))
        player.playSound(config.horseTradeSuccessfulSound.toSound())
        player.sendActionBar(
            config.i18n.horseTradeSuccessfulMessage.mm(
                Placeholder.component("material", amount.toString().mm()),
            ),
        )
    }

    /**
     * Finds the first leashed horse owned by the player within the configured radius.
     * @param player The player to search around.
     * @return The found horse or `null` if none exists.
     */
    private fun findLeashedHorse(player: Player): Horse? =
        player
            .getNearbyEntities(
                config.horseTransferRadius.toDouble(),
                config.horseTransferRadius.toDouble(),
                config.horseTransferRadius.toDouble(),
            ).filterIsInstance<Horse>()
            .firstOrNull { it.isLeashed && it.leashHolder == player }

    /**
     * Calculates the trade value of the horse based on attributes.
     * @param horse The horse to evaluate.
     * @return The trade value.
     */
    private fun calculateTradeValue(horse: Horse): Int =
        ExpressionEngine
            .evaluate(
                config.horseTradeFormula,
                mapOf(
                    "speed" to (horse.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue ?: 0.0),
                    "jump" to horse.jumpStrength,
                ),
            ).toInt()
            .coerceAtLeast(1)

    @Serializable
    data class Config(
        override var enabled: Boolean = true,
        var disableBlazeGrief: Boolean = true,
        var disableCreeperGrief: Boolean = true,
        var disableEnderDragonGrief: Boolean = true,
        var disableEndermanGrief: Boolean = true,
        var disableGhastGrief: Boolean = true,
        var disableWitherGrief: Boolean = true,
        var horseTransferRadius: Int = 10,
        var horseTradeFormula: String = "speed * 10 + jump * 10 - 12",
        var horseTradeMaterial: Material = Material.EMERALD,
        var horseTradeSuccessfulSound: SoundData =
            SoundData(
                BukkitSound.ENTITY_WANDERING_TRADER_TRADE,
                Sound.Source.NEUTRAL,
            ),
        var horseTradeDeniedSound: SoundData =
            SoundData(
                BukkitSound.ENTITY_WANDERING_TRADER_NO,
                Sound.Source.NEUTRAL,
            ),
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        @Serializable
        data class I18n(
            var horseTradeSuccessfulMessage: String = "You traded your horse for: <material> <sprite:item/emerald>",
        )
    }
}
