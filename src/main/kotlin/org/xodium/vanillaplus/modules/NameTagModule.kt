package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class NameTagModule : ModuleInterface<NameTagModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    val activeTags: MutableMap<UUID?, TextDisplay?> = ConcurrentHashMap<UUID?, TextDisplay?>()

    fun display(player: Player, config: TeamConfig) {
        remove(player)
        val location: Location = player.location.clone().add(0.0, 0.2, 0.0)
        val textDisplay: TextDisplay? = player.world.spawn(location, TextDisplay::class.java) { entity ->
            entity.billboard = Display.Billboard.CENTER
            entity.isPersistent = false
            entity.text(deserialize(config.prefix() + config.tag() + player.name + config.suffix()))
            entity.transformation = getDisplayTransformation()
            entity.isShadowed = true
            entity.isDefaultBackground = true
            entity.persistentDataContainer.set(
                NamespacedKey.fromString("player_display"),
                PersistentDataType.STRING,
                player.uniqueId.toString()
            )
        }
        activeTags.put(player.uniqueId, textDisplay)
        player.addPassenger(textDisplay)
        player.hideEntity(instance, textDisplay)
    }

    fun remove(player: Player) {
        val textDisplay = activeTags.remove(player.uniqueId)
        textDisplay?.remove()
    }

    fun update(player: Player, config: TeamConfig?) {
        val textDisplay = activeTags[player.uniqueId]
        if (textDisplay != null && config != null) {
            textDisplay.text(deserialize((config.prefix() + config.tag()).toString() + player.name + config.suffix()))
            textDisplay.transformation = getDisplayTransformation()
        }
    }

    private fun getDisplayTransformation(): Transformation {
        return Transformation(
            Vector3f(0f, 0.2f, 0f),
            AxisAngle4f(),
            Vector3f(1f, 1f, 1f),
            AxisAngle4f()
        )
    }

    fun getTextDisplay(player: Player): TextDisplay? = activeTags[player.uniqueId]

    private fun deserialize(text: String): Component = MiniMessage.miniMessage().deserialize(text)

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}