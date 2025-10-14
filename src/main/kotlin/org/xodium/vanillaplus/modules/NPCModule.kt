package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.engines.ExpressionEngine
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.HorsePDC.sold
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling wandering trader mechanics within the system. */
internal class NPCModule : ModuleInterface<NPCModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (!enabled()) return

        val player = event.player
        val entity = event.rightClicked

        if (entity !is WanderingTrader) return

        val horse = findLeashedHorse(player) ?: return
        val emeralds = calculateEmeraldValue(horse)

        if (emeralds <= 0 || horse.sold()) return

        event.isCancelled = true

        horse.sold(true)
        horse.isTamed = false
        horse.removeWhenFarAway = true
        horse.setLeashHolder(entity)

        player.inventory.addItem(ItemStack.of(Material.EMERALD, emeralds))
        player.sendActionBar(
            config.i18n.horseTradeSuccessfulMessage.mm(
                Placeholder.component("emeralds", emeralds.toString().mm()),
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
                config.transferRadius.toDouble(),
                config.transferRadius.toDouble(),
                config.transferRadius.toDouble(),
            ).filterIsInstance<Horse>()
            .firstOrNull { it.isLeashed && it.leashHolder == player }

    /**
     * Calculates the emerald value of the horse based on attributes.
     * @param horse The horse to evaluate.
     * @return The emerald value.
     */
    private fun calculateEmeraldValue(horse: Horse): Int {
        val context =
            mapOf(
                "speed" to (horse.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue ?: 0.0),
                "jump" to horse.jumpStrength,
            )

        return config.evaluate(context).coerceAtLeast(1)
    }

    data class Config(
        override var enabled: Boolean = true,
        var transferRadius: Int = 10,
        var horseTradeFormula: String = "speed * 10 + jump * 10 - 12",
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var horseTradeSuccessfulMessage: String = "You traded your horse for: <emeralds> <sprite:item/emerald>",
        )

        /** Evaluates expressions */
        fun evaluate(context: Map<String, Double>): Int =
            ExpressionEngine.evaluate(horseTradeFormula, context, setOf("speed", "jump")).toInt()
    }
}
