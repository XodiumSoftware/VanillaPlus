package org.xodium.illyriaplus.bosses.nether

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface
import kotlin.random.Random

/**
 * An enderman shaman that channels the warped energy of the forest.
 */
internal object WarpedForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00CED1:#191970>Vexis, the Warped Seer</gradient></bold>")
    override val bossType: EntityType = EntityType.ENDERMAN
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 250.0,
            Attribute.MOVEMENT_SPEED to 0.35,
            Attribute.ATTACK_DAMAGE to 10.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Teleport and blindness every 5 seconds (100 ticks)
        if (entity.ticksLived % 100 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull() ?: return
        val randomLoc = entity.location.add((Random.nextDouble() - 0.5) * 10, 0.0, (Random.nextDouble() - 0.5) * 10)
        randomLoc.y = entity.world.getHighestBlockYAt(randomLoc).toDouble() + 1

        entity.teleport(randomLoc)
        target.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 60, 0))
        entity.world.spawnParticle(Particle.PORTAL, entity.location, 25, 0.5, 0.5, 0.5, 0.1)
    }
}
