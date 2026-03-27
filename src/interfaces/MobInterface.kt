package org.xodium.vanillaplus.interfaces

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.BannerPatternLayers
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.inventory.ItemStack

/** Represents a contract for a custom mob within the system. */
internal interface MobInterface<T : Mob, M : Entity> {
    /** A black shield with a red skull pattern, shared across horde mob loadouts. */
    @Suppress("UnstableApiUsage")
    val shield
        get() =
            ItemStack.of(Material.SHIELD).apply {
                setData(DataComponentTypes.BASE_COLOR, DyeColor.BLACK)
                setData(
                    DataComponentTypes.BANNER_PATTERNS,
                    BannerPatternLayers
                        .bannerPatternLayers()
                        .add(Pattern(DyeColor.RED, PatternType.SKULL))
                        .build(),
                )
            }

    /** The class of the entity this mob represents. */
    val mobClass: Class<T>

    /** The class of the mount entity, or null if this mob has no mount. */
    val mountClass: Class<out M>? get() = null

    /**
     * Configures the given [entity] with this mob's stats, equipment, and appearance.
     * @param entity The entity to configure.
     */
    fun mob(entity: T)

    /**
     * Configures the mount [entity]. No-op by default; override when [mountClass] is set.
     * @param entity The mount entity to configure.
     */
    fun mount(entity: M) {}

    /**
     * Spawns this mob at the given [location]. If [mountClass] is set, also spawns and
     * configures the mount via [mount], then seats this mob on it.
     * @param location The [Location] to spawn the mob at.
     * @return The spawned entity.
     */
    @Suppress("UNCHECKED_CAST")
    fun spawn(location: Location): T =
        location.world.spawn(location, mobClass) { mob(it) }.also { rider ->
            mountClass?.let { cls -> location.world.spawn(location, cls as Class<M>) { mount(it) }.addPassenger(rider) }
        }
}
