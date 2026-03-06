@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Mannequin
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.MannequinPDC.OWNER_KEY
import java.util.*

/**
 * Provides access to mannequin-specific persistent data, including ownership.
 * @property OWNER_KEY The namespaced key used for storing the mannequin owner.
 */
internal object MannequinPDC {
    private val OWNER_KEY = NamespacedKey(instance, "owner")
    private val FOLLOWING_KEY = NamespacedKey(instance, "following")
    private val PROXY_ID_KEY = NamespacedKey(instance, "proxy_id")

    /**
     * Gets or sets the [Mannequin]'s owner in its persistent data container.
     * @receiver The [Mannequin] whose owner to access.
     * @return The owner's [UUID].
     */
    var Mannequin.owner: UUID
        get() = UUID.fromString(persistentDataContainer.getOrDefault(OWNER_KEY, PersistentDataType.STRING, ""))
        set(value) = persistentDataContainer.set(OWNER_KEY, PersistentDataType.STRING, value.toString())

    /**
     * Gets or sets whether the [Mannequin] is currently following its owner.
     * @receiver The [Mannequin] whose follow state to access.
     * @return Whether the [Mannequin] is following its owner.
     */
    var Mannequin.following: Boolean
        get() = persistentDataContainer.getOrDefault(FOLLOWING_KEY, PersistentDataType.BOOLEAN, false)
        set(value) = persistentDataContainer.set(FOLLOWING_KEY, PersistentDataType.BOOLEAN, value)

    /**
     * Gets or sets the [UUID] of the [Mannequin]'s invisible proxy navigation mob, or null if none exists.
     * @receiver The [Mannequin] whose proxy [UUID] to access.
     * @return The proxy mob's [UUID], or null if no proxy is active.
     */
    var Mannequin.proxyId: UUID?
        get() = persistentDataContainer.get(PROXY_ID_KEY, PersistentDataType.STRING)?.let { UUID.fromString(it) }
        set(value) {
            if (value == null) {
                persistentDataContainer.remove(PROXY_ID_KEY)
            } else {
                persistentDataContainer.set(PROXY_ID_KEY, PersistentDataType.STRING, value.toString())
            }
        }
}
