@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.google.common.io.ByteStreams
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.handlers.RenameHandler
import org.xodium.vanillaplus.handlers.SwapHandler
import org.xodium.vanillaplus.handlers.SyncHandler
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling armour stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    init {
        if (enabled()) {
            instance.server.messenger.apply {
                registerIncomingPluginChannel(instance, "armorposer:sync_packet", SyncHandler())
                registerOutgoingPluginChannel(instance, "armorposer:swap_packet", SwapHandler())
                registerOutgoingPluginChannel(instance, "armorposer:rename_packet", RenameHandler())
            }
        }
    }

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled()) return

        val player = event.player
        val entity = event.rightClicked

        if (entity is ArmorStand && player.isSneaking) {
            if (event.hand == EquipmentSlot.HAND) {
                val out = ByteStreams.newDataOutput()

                out.writeInt(entity.entityId)
                instance.server.messenger.registerOutgoingPluginChannel(instance, "armorposer:screen_packet")
                player.sendPluginMessage(instance, "armorposer:screen_packet", out.toByteArray())
                instance.server.messenger.unregisterOutgoingPluginChannel(instance, "armorposer:screen_packet")
            }
            event.isCancelled = true
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
