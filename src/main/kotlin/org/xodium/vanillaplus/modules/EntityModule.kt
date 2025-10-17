@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.block.Barrel
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.engines.ExpressionEngine
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.HorsePDC.sold
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.bukkit.Sound as BukkitSound

/** Represents a module handling entity mechanics within the system. */
internal class EntityModule : ModuleInterface<EntityModule.Config> {
    override val config: Config = Config()

    private val searchRadius = 8
    private val intervalTicks = 100L
    private val searchingKey = NamespacedKey(instance, "food_searching")

    init {
        if (enabled()) {
            instance.server.worlds.forEach { world ->
                world.livingEntities.filterIsInstance<Animals>().forEach { animal -> startSearchTask(animal) }
            }
        }
    }

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

    @EventHandler
    fun on(event: CreatureSpawnEvent) {
        if (!enabled()) return

        val animal = event.entity

        if (animal is Animals) startSearchTask(animal)
    }

    private fun startSearchTask(animal: Animals) {
        val data = animal.persistentDataContainer
        if (data.has(searchingKey, PersistentDataType.BYTE)) return
        data.set(searchingKey, PersistentDataType.BYTE, 1)

        object : BukkitRunnable() {
            override fun run() {
                if (!animal.isValid || animal.isDead) {
                    cancel()
                    return
                }

                if (animal.isLoveMode) return

                val foods = getFoodFor(animal) ?: return
                val barrel =
                    findBarrelWithFood(animal.location.toVector(), animal.world.name, searchRadius, foods)
                        ?: return

                val pathfinder = animal.pathfinder
                pathfinder.moveTo(barrel.location.add(0.5, 0.0, 0.5))

                if (animal.location.distanceSquared(barrel.location) <= 2.25) {
                    consumeOneFood(barrel, foods)
                    animal.loveModeTicks = 600
                    animal.world.spawnParticle(Particle.HEART, animal.location.add(0.0, 1.0, 0.0), 5)
                }
            }
        }.runTaskTimer(instance, 0L, intervalTicks)
    }

    /**
     * Removes one valid food item from the barrel inventory.
     * @param barrel The barrel containing the food.
     * @param foods The valid food materials.
     */
    private fun consumeOneFood(
        barrel: Barrel,
        foods: Set<Material>,
    ) {
        val inv = barrel.inventory

        for (i in inv.contents.indices) {
            val item = inv.getItem(i)

            if (item != null && item.type in foods) {
                item.amount = item.amount - 1

                if (item.amount <= 0) inv.clear(i)

                barrel.update()

                return
            }
        }
    }

    /**
     * Finds a nearby barrel that contains any of the provided food items.
     *
     * @param origin The animal’s position.
     * @param worldName The world name.
     * @param radius The search radius.
     * @param foods The valid food items.
     * @return A barrel containing food, or null if none found.
     */
    private fun findBarrelWithFood(
        origin: Vector,
        worldName: String,
        radius: Int,
        foods: Set<Material>,
    ): Barrel? {
        val world = instance.server.getWorld(worldName) ?: return null
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(origin.blockX + x, origin.blockY + y, origin.blockZ + z)
                    val state = block.state
                    if (state is Barrel && state.inventory.contents.any { it != null && it.type in foods }) {
                        return state
                    }
                }
            }
        }
        return null
    }

    /**
     * Returns the valid food materials for the specified animal.
     *
     * @param animal The target animal.
     * @return The set of valid food materials, or null if not applicable.
     */
    private fun getFoodFor(animal: Animals): Set<Material>? =
        when (animal.type) {
            EntityType.COW, EntityType.SHEEP -> setOf(Material.WHEAT)

            EntityType.PIG -> setOf(Material.CARROT, Material.BEETROOT, Material.POTATO)

            EntityType.CHICKEN ->
                setOf(
                    Material.WHEAT_SEEDS,
                    Material.BEETROOT_SEEDS,
                    Material.MELON_SEEDS,
                    Material.PUMPKIN_SEEDS,
                )

            EntityType.RABBIT -> setOf(Material.DANDELION, Material.CARROT, Material.BEETROOT)

            else -> null
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
        data class I18n(
            var horseTradeSuccessfulMessage: String = "You traded your horse for: <material> <sprite:item/emerald>",
        )
    }
}
