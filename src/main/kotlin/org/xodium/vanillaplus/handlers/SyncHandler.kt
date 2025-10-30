package org.xodium.vanillaplus.handlers

import io.netty.buffer.Unpooled
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
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

class SyncHandler : PluginMessageListener {
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
            if (tag.contains("Invisible")) entity.setInvisible(tag.getBoolean("Invisible"))
            if (tag.contains("NoBasePlate")) entity.setBasePlate(!tag.getBoolean("NoBasePlate"))
            if (tag.contains("NoGravity")) entity.setGravity(!tag.getBoolean("NoGravity"))
            if (tag.contains("ShowArms")) entity.setArms(tag.getBoolean("ShowArms"))
            if (tag.contains("Small")) entity.setSmall(tag.getBoolean("Small"))
            if (tag.contains("CustomNameVisible")) entity.setCustomNameVisible(tag.getBoolean("CustomNameVisible"))
            if (tag.contains("Rotation")) {
                val tagList: ListTag = tag.getList("Rotation", Tag.TAG_FLOAT)
                val yaw: Float = tagList.getFloat(0)
                entity.bodyYaw = yaw
                entity.setRotation(yaw, entity.pitch)
            }

            if (tag.contains("DisabledSlots")) {
                val disabledSlots: Int = tag.getInt("DisabledSlots")
                entity.removeDisabledSlots(*EquipmentSlot.entries.toTypedArray())
                for (slot in EquipmentSlot.entries) {
                    if (isSlotDisabled(entity, slot, disabledSlots)) {
                        entity.addDisabledSlots(slot)
                    }
                }
            }

            if (tag.contains("Scale") && canResize(player)) {
                val scale: Double = tag.getDouble("Scale")
                val attribute = entity.getAttribute(Attribute.SCALE)
                if (attribute != null && scale > 0) {
                    attribute.baseValue = scale
                }
            }

            if (tag.contains("Pose")) {
                val poseTag: CompoundTag = tag.getCompound("Pose")
                readPose(entity, poseTag)

                val tagList: ListTag = tag.getList("Move", Tag.TAG_DOUBLE)
                val x: Double = tagList.getDouble(0)
                val y: Double = tagList.getDouble(1)
                val z: Double = tagList.getDouble(2)
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
        val head: ListTag = tag.getList("Head", 5)
        armorStand.headPose = if (head.isEmpty) DEFAULT_HEAD_POSE else getAngle(head)
        val body: ListTag = tag.getList("Body", 5)
        armorStand.bodyPose = if (body.isEmpty) DEFAULT_BODY_POSE else getAngle(body)
        val leftArm: ListTag = tag.getList("LeftArm", 5)
        armorStand.leftArmPose = if (leftArm.isEmpty) DEFAULT_LEFT_ARM_POSE else getAngle(leftArm)
        val rightArm: ListTag = tag.getList("RightArm", 5)
        armorStand.rightArmPose = if (rightArm.isEmpty) DEFAULT_RIGHT_ARM_POSE else getAngle(rightArm)
        val leftLeg: ListTag = tag.getList("LeftLeg", 5)
        armorStand.leftLegPose = if (leftLeg.isEmpty) DEFAULT_LEFT_LEG_POSE else getAngle(leftLeg)
        val rightLeg: ListTag = tag.getList("RightLeg", 5)
        armorStand.rightLegPose = if (rightLeg.isEmpty) DEFAULT_RIGHT_LEG_POSE else getAngle(rightLeg)
    }

    companion object {
        private val DEFAULT_HEAD_POSE: EulerAngle = getAngle(0.0f, 0.0f, 0.0f)
        private val DEFAULT_BODY_POSE: EulerAngle = getAngle(0.0f, 0.0f, 0.0f)
        private val DEFAULT_LEFT_ARM_POSE: EulerAngle = getAngle(-10.0f, 0.0f, -10.0f)
        private val DEFAULT_RIGHT_ARM_POSE: EulerAngle = getAngle(-15.0f, 0.0f, 10.0f)
        private val DEFAULT_LEFT_LEG_POSE: EulerAngle = getAngle(-1.0f, 0.0f, -1.0f)
        private val DEFAULT_RIGHT_LEG_POSE: EulerAngle = getAngle(1.0f, 0.0f, 1.0f)

        private fun getAngle(tag: ListTag): EulerAngle = Companion.getAngle(tag.getFloat(0), tag.getFloat(1), tag.getFloat(2))

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
