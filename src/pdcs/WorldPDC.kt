package org.xodium.illyriaplus.pdcs

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance

/** Provides access to [World]-specific persistent data for anchored bosses. */
internal object WorldPDC {
    /** The [NamespacedKey] used for storing anchored boss data. */
    private val ANCHORED_BOSSES_KEY = NamespacedKey(instance, "anchored_bosses")

    /** Data class representing an anchored boss. */
    data class AnchoredBoss(
        val bossClassName: String,
        val world: String,
        val x: Double,
        val y: Double,
        val z: Double,
    ) {
        /** Gets the location of this anchored boss. */
        fun getLocation(): Location? {
            val bukkitWorld = instance.server.getWorld(world) ?: return null

            return Location(bukkitWorld, x, y, z)
        }

        /** Returns a formatted string representation. */
        override fun toString(): String = "$bossClassName at (${x.toInt()}, ${y.toInt()}, ${z.toInt()}) in $world"
    }

    /** Gets all anchored bosses from the world's persistent data. */
    var World.anchoredBosses: List<AnchoredBoss>
        get() {
            val data = persistentDataContainer.get(ANCHORED_BOSSES_KEY, PersistentDataType.STRING) ?: return emptyList()
            return data.split(";").mapNotNull {
                val parts = it.split(",")

                if (parts.size == 5) {
                    AnchoredBoss(parts[0], parts[1], parts[2].toDouble(), parts[3].toDouble(), parts[4].toDouble())
                } else {
                    null
                }
            }
        }
        set(value) {
            if (value.isEmpty()) {
                persistentDataContainer.remove(ANCHORED_BOSSES_KEY)
            } else {
                val data = value.joinToString(";") { "${it.bossClassName},${it.world},${it.x},${it.y},${it.z}" }

                persistentDataContainer.set(ANCHORED_BOSSES_KEY, PersistentDataType.STRING, data)
            }
        }

    /** Adds an anchored boss to this world. */
    fun World.addAnchoredBoss(
        bossClassName: String,
        location: Location,
    ) {
        val current = anchoredBosses.toMutableList()

        current.add(AnchoredBoss(bossClassName, location.world.name, location.x, location.y, location.z))
        anchoredBosses = current
    }

    /** Removes an anchored boss from this world by index. */
    fun World.removeAnchoredBoss(index: Int): Boolean {
        val current = anchoredBosses.toMutableList()

        if (index >= 0 && index < current.size) {
            current.removeAt(index)
            anchoredBosses = current
            return true
        }
        return false
    }
}
