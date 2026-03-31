package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.SmallFireball
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.util.Vector
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Represents an object handling inferno enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object InfernoEnchantment : EnchantmentInterface<PlayerInteractEvent> {
    private val cooldowns = HashMap<UUID, Long>()
    private const val COOLDOWN_MS = 1500L

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(4)
            .maxLevel(3)
            .weight(1)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 5))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    override fun effect(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = event.item ?: return

        if (item.type != Material.BLAZE_ROD) return
        if (!item.containsEnchantment(get())) return

        val player = event.player

        if (player.gameMode != GameMode.SURVIVAL) return

        val now = System.currentTimeMillis()

        if ((cooldowns[player.uniqueId] ?: 0L) > now) return

        cooldowns[player.uniqueId] = now + COOLDOWN_MS

        event.isCancelled = true

        val level = item.getEnchantmentLevel(get())
        val angleOffsets =
            when (level) {
                1 -> doubleArrayOf(0.0)
                2 -> doubleArrayOf(-10.0, 0.0, 10.0)
                else -> doubleArrayOf(-20.0, -10.0, 0.0, 10.0, 20.0)
            }
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))

        for (angleDeg in angleOffsets) {
            val radians = angleDeg * PI / 180.0
            val cos = cos(radians)
            val sin = sin(radians)
            val spread =
                Vector(
                    direction.x * cos - direction.z * sin,
                    direction.y,
                    direction.x * sin + direction.z * cos,
                ).normalize()
            val fireball = player.world.spawn(spawnLocation, SmallFireball::class.java)

            fireball.shooter = player
            fireball.direction = spread.multiply(1.5)
            fireball.yield = 0.0f
        }
    }
}
