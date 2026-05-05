package org.xodium.illyriaplus.data

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance

/**
 * Represents the data structure for a teleport destination.
 *
 * @property worldName The name of the world this teleport destination resides in.
 * @property x The X coordinate of the teleport location.
 * @property y The Y coordinate of the teleport location.
 * @property z The Z coordinate of the teleport location.
 * @property pitch The pitch of the teleport location.
 * @property yaw The yaw of the teleport location.
 * @property name The display name of this teleport anchor.
 */
@Serializable
internal data class TeleportAnchorData(
    private val worldName: String,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val pitch: Float,
    private val yaw: Float,
    val name: String,
) {
    /** The [World] this anchor resides in. */
    val world: World get() = instance.server.getWorld(worldName) ?: error("World '$worldName' not found")

    /** The specific [Location] within the world to teleport to. */
    val location: Location get() = Location(world, x, y, z, yaw, pitch)

    /**
     * Convenience constructor from Bukkit types.
     *
     * @param world The [World] this anchor resides in.
     * @param location The specific [Location] to teleport to.
     * @param name The display name of this teleport anchor.
     */
    constructor(world: World, location: Location, name: String) : this(
        worldName = world.name,
        x = location.x,
        y = location.y,
        z = location.z,
        pitch = location.pitch,
        yaw = location.yaw,
        name = name,
    )

    /**
     * Checks if the given [Location] matches this teleport anchor's position.
     *
     * @param location The [Location] to compare against.
     * @return `true` if the world and block coordinates (X, Y, Z) match; `false` otherwise.
     */
    fun matches(location: Location): Boolean =
        world == location.world &&
            this.location.blockX == location.blockX &&
            this.location.blockY == location.blockY &&
            this.location.blockZ == location.blockZ

    /**
     * Returns a copy of this anchor with the given [name].
     *
     * @param name The new display name.
     * @return A new [TeleportAnchorData] with the updated name.
     */
    fun name(name: String): TeleportAnchorData = copy(name = name)

    companion object {
        /**
         * Generates the next available default anchor name (e.g., "Anchor 1", "Anchor 2").
         *
         * @param existing The list of existing [TeleportAnchorData] entries.
         * @return The next unused "Anchor N" name.
         */
        fun nextName(existing: List<TeleportAnchorData>): String =
            "Anchor ${
                (1..Int.MAX_VALUE).first {
                    it !in
                        existing
                            .mapNotNull { anchor -> anchor.name.removePrefix("Anchor ").toIntOrNull() }
                            .toSet()
                }
            }"
    }
}
