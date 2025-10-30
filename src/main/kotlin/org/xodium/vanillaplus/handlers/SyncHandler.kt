package org.xodium.vanillaplus.handlers

import io.netty.buffer.Unpooled
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.util.EulerAngle
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Handles plugin messages related to synchronizing [ArmorStand] properties. */
internal class SyncHandler : PluginMessageListener {
    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray,
    ) {
        if (channel != "armorposer:sync_packet") return

        val byteBuf = FriendlyByteBuf(Unpooled.wrappedBuffer(message))
        val uuid = byteBuf.readUUID()
        val tag = byteBuf.readNbt()
        val entity = instance.server.getEntity(uuid)

        if (tag != null && entity is ArmorStand) {
            tag.getBoolean("Invisible").ifPresent(entity::setInvisible)
            tag.getBoolean("NoBasePlate").ifPresent { entity.setBasePlate(!it) }
            tag.getBoolean("NoGravity").ifPresent { entity.setGravity(!it) }
            tag.getBoolean("ShowArms").ifPresent(entity::setArms)
            tag.getBoolean("Small").ifPresent(entity::setSmall)
            tag.getBoolean("CustomNameVisible").ifPresent(entity::setCustomNameVisible)
            tag.getList("Rotation").ifPresent { tagList ->
                tagList.getFloat(0).ifPresent {
                    entity.bodyYaw = it
                    entity.setRotation(it, entity.pitch)
                }
            }
            tag.getInt("DisabledSlots").ifPresent { disabledSlots ->
                entity.removeDisabledSlots(*EquipmentSlot.entries.toTypedArray())
                for (slot in EquipmentSlot.entries) {
                    if (isSlotDisabled(entity, slot, disabledSlots)) entity.addDisabledSlots(slot)
                }
            }
            tag.getDouble("Scale").ifPresent { scale ->
                val attribute = entity.getAttribute(Attribute.SCALE)
                if (attribute != null && scale > 0) {
                    attribute.baseValue = scale
                }
            }

            if (tag.contains("Pose")) {
                tag.getCompound("Pose").ifPresent { poseTag ->
                    readPose(entity, poseTag)
                }

                tag.getList("Move").ifPresent { tagList ->
                    val x = tagList.getDouble(0).orElse(0.0)
                    val y = tagList.getDouble(1).orElse(0.0)
                    val z = tagList.getDouble(2).orElse(0.0)
                    if (x != 0.0 || y != 0.0 || z != 0.0) {
                        entity.teleport(
                            Location(
                                entity.world,
                                entity.x + x,
                                entity.y + y,
                                entity.z + z,
                            ),
                            PlayerTeleportEvent.TeleportCause.PLUGIN,
                        )
                    }
                }
            }
        }
    }

    private fun isSlotDisabled(
        armorStand: ArmorStand,
        slot: EquipmentSlot,
        disabledSlots: Int,
    ): Boolean {
        val filterFlag =
            when (slot) {
                EquipmentSlot.HAND -> 0
                EquipmentSlot.OFF_HAND -> 5
                EquipmentSlot.FEET -> 1
                EquipmentSlot.LEGS -> 2
                EquipmentSlot.CHEST -> 3
                else -> 4
            }
        return (disabledSlots and (1 shl filterFlag)) != 0 ||
            (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) && !armorStand.hasArms()
    }

    private fun readPose(
        armorStand: ArmorStand,
        tag: CompoundTag,
    ) {
        tag.getList("Head").ifPresent { head ->
            armorStand.headPose = if (head.isEmpty) DEFAULT_HEAD_POSE else getAngle(head)
        }
        tag.getList("Body").ifPresent { body ->
            armorStand.bodyPose = if (body.isEmpty) DEFAULT_BODY_POSE else getAngle(body)
        }
        tag.getList("LeftArm").ifPresent { leftArm ->
            armorStand.leftArmPose = if (leftArm.isEmpty) DEFAULT_LEFT_ARM_POSE else getAngle(leftArm)
        }
        tag.getList("RightArm").ifPresent { rightArm ->
            armorStand.rightArmPose = if (rightArm.isEmpty) DEFAULT_RIGHT_ARM_POSE else getAngle(rightArm)
        }
        tag.getList("LeftLeg").ifPresent { leftLeg ->
            armorStand.leftLegPose = if (leftLeg.isEmpty) DEFAULT_LEFT_LEG_POSE else getAngle(leftLeg)
        }
        tag.getList("RightLeg").ifPresent { rightLeg ->
            armorStand.rightLegPose = if (rightLeg.isEmpty) DEFAULT_RIGHT_LEG_POSE else getAngle(rightLeg)
        }
    }

    companion object {
        private val DEFAULT_HEAD_POSE: EulerAngle = getAngle(0.0f, 0.0f, 0.0f)
        private val DEFAULT_BODY_POSE: EulerAngle = getAngle(0.0f, 0.0f, 0.0f)
        private val DEFAULT_LEFT_ARM_POSE: EulerAngle = getAngle(-10.0f, 0.0f, -10.0f)
        private val DEFAULT_RIGHT_ARM_POSE: EulerAngle = getAngle(-15.0f, 0.0f, 10.0f)
        private val DEFAULT_LEFT_LEG_POSE: EulerAngle = getAngle(-1.0f, 0.0f, -1.0f)
        private val DEFAULT_RIGHT_LEG_POSE: EulerAngle = getAngle(1.0f, 0.0f, 1.0f)

        private fun getAngle(tag: ListTag): EulerAngle =
            getAngle(
                tag.getFloat(0).orElse(0.0f),
                tag.getFloat(1).orElse(0.0f),
                tag.getFloat(2).orElse(0.0f),
            )

        private fun getAngle(
            xDeg: Float,
            yDeg: Float,
            zDeg: Float,
        ): EulerAngle =
            EulerAngle(
                Math.toRadians(xDeg.toDouble()),
                Math.toRadians(yDeg.toDouble()),
                Math.toRadians(zDeg.toDouble()),
            )
    }
}
