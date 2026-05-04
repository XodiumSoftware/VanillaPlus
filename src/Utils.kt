@file:Suppress("ktlint:standard:no-wildcard-imports", "Unused")

package org.xodium.illyriaplus

import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.pdcs.ItemPDC.selectedSpell
import org.xodium.illyriaplus.pdcs.PlayerPDC.nickname
import org.xodium.illyriaplus.pdcs.PlayerPDC.scoreboardVisibility
import java.net.URI
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64

/** General utilities. */
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [IllyriaPlus] messages. */
    val IllyriaPlus.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /** Extension function to convert snake_case to Proper Case with spaces. */
    fun String.snakeToProperCase(): String =
        split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    /** Extension function to convert CamelCase to snake_case, removing a specified suffix. */
    inline fun <reified T> Class<*>.toRegistryKeyFragment(): String =
        simpleName
            .removeSuffix(T::class.simpleName ?: "")
            .split(Regex("(?=[A-Z])"))
            .filter { it.isNotEmpty() }
            .joinToString("_") { it.lowercase() }

    /** Enchantment-related utilities. */
    object EnchantmentUtils {
        /** Extension function specifically for enchantment keys */
        fun TypedKey<Enchantment>.displayName(): Component = MM.deserialize(value().snakeToProperCase())

        /**
         * Checks if the given [item] has the specified [spell] currently selected.
         * Returns true if the item's selectedSpell matches the spell's key.
         */
        fun isSelectedSpell(
            item: ItemStack?,
            spell: Enchantment,
        ): Boolean = item?.selectedSpell == spell.key.toString()
    }

    /** World-related utilities. */
    object WorldUtils {
        /**
         * Returns the i18n string matching the current weather state of this world.
         *
         * @param thundering The string to return when it is thundering.
         * @param storm The string to return when there is a storm.
         * @param clear The string to return when the weather is clear.
         */
        fun World.weather(
            thundering: String,
            storm: String,
            clear: String,
        ): String =
            when {
                isThundering -> thundering
                hasStorm() -> storm
                else -> clear
            }
    }

    /** Schedule-related utilities. */
    object ScheduleUtils {
        /**
         * Schedules a repeating task.
         *
         * @param delay The initial delay before the first execution in ticks.
         * @param period The interval between executions in ticks.
         * @param duration The total duration for which the task should run in ticks. If `null`, the task runs indefinitely.
         * @param content The content to execute in the scheduled task.
         * @return The scheduled [org.bukkit.scheduler.BukkitTask].
         */
        fun schedule(
            delay: Long = 0L,
            period: Long = 2L,
            duration: Long? = null,
            content: () -> Unit,
        ): BukkitTask =
            instance.server.scheduler
                .runTaskTimer(instance, content, delay, period)
                .also { task ->
                    duration?.let {
                        instance.server.scheduler.runTaskLater(
                            instance,
                            task::cancel,
                            it,
                        )
                    }
                }

        /**
         * Spawns a repeating particle trail on [entity] every tick until it is no longer valid.
         *
         * @param entity The entity to follow.
         * @param particles Called each tick with the entity's current [org.bukkit.Location] to spawn particles.
         * @return The [BukkitTask] running the trail.
         */
        fun spawnProjectileTrail(
            entity: Entity,
            particles: (Location) -> Unit,
        ): BukkitTask {
            lateinit var task: BukkitTask

            task =
                schedule(delay = 1L, period = 1L) {
                    if (!entity.isValid) {
                        task.cancel()
                        return@schedule
                    }

                    particles(entity.location)
                }

            return task
        }
    }

    /** Command-related utilities. */
    object CommandUtils {
        /**
         * Registers a command execution handler on an [com.mojang.brigadier.builder.ArgumentBuilder] with automatic try/catch handling.
         *
         * @param action The action executed when the command runs.
         * @return The same [com.mojang.brigadier.builder.ArgumentBuilder] for further configuration.
         */
        fun <T : ArgumentBuilder<CommandSourceStack, T>> T.executesCatching(
            action: (CommandContext<CommandSourceStack>) -> Unit,
        ): T {
            executes { ctx ->
                runCatching { action(ctx) }
                    .onFailure {
                        instance.logger.severe(
                            """
                            Command error: ${it.message}
                            ${it.stackTraceToString()}
                            """.trimIndent(),
                        )
                        (ctx.source.sender as? Player)?.sendMessage(
                            MM.deserialize(
                                "${instance.prefix} <red>An error has occurred. Check server logs for details.",
                            ),
                        )
                    }
                Command.SINGLE_SUCCESS
            }
            return this
        }

        /**
         * Registers a command execution handler on an [ArgumentBuilder] specifically for [Player] senders with automatic try/catch handling.
         *
         * @param action The action executed when the command runs, receiving the [Player] and [CommandContext].
         * @return The same [ArgumentBuilder] for further configuration.
         * @throws IllegalStateException if the command is executed by a non-[Player] sender.
         */
        fun <T : ArgumentBuilder<CommandSourceStack, T>> T.playerExecuted(
            action: (Player, CommandContext<CommandSourceStack>) -> Unit,
        ): T {
            executesCatching {
                action(
                    it.source.sender as? Player ?: run {
                        instance.logger.warning("Command can only be executed by a Player!")
                        return@executesCatching
                    },
                    it,
                )
            }
            return this
        }
    }

    /** Block-related utilities. */
    object BlockUtils {
        /**
         * Get the centre of a [org.bukkit.block.Block], handling [org.bukkit.block.DoubleChest]s properly.
         *
         * @return The centre [Location] of the block.
         */
        fun Block.center(): Location {
            val baseAddition = Location(location.world, location.x + 0.5, location.y + 0.5, location.z + 0.5)
            val chestState = state as? Chest ?: return baseAddition
            val holder = chestState.inventory.holder as? DoubleChest ?: return baseAddition
            val leftBlock = (holder.leftSide as? Chest)?.block
            val rightBlock = (holder.rightSide as? Chest)?.block

            if (leftBlock == null || rightBlock == null || leftBlock.world !== rightBlock.world) return baseAddition

            val world = leftBlock.world
            val cx = (leftBlock.x + rightBlock.x) / 2.0 + 0.5
            val cy = (leftBlock.y + rightBlock.y) / 2.0 + 0.5
            val cz = (leftBlock.z + rightBlock.z) / 2.0 + 0.5

            return Location(world, cx, cy, cz)
        }
    }

    /** Player-related utilities. */
    object PlayerUtils {
        private const val FACE_X = 8
        private const val FACE_Y = 8
        private const val FACE_WIDTH = 8
        private const val FACE_HEIGHT = 8
        private const val MAX_COORDINATE = 7
        private const val COLOR_MASK = 0xFF
        private const val BLACK_COLOR = "#000000"
        private const val PIXEL_CHAR = "█"
        private const val ALPHA_SHIFT = 24
        private const val RED_SHIFT = 16
        private const val GREEN_SHIFT = 8

        /**
         * Retrieves a [Player]'s face as a [String].
         *
         * @param size The size of the face in pixels (default is 8).
         * @return A [String] representing the player's face.
         */
        fun Player.face(size: Int = 8): String {
            // 1. fetch skin URL from the playerProfile
            val texturesProp =
                playerProfile.properties
                    .firstOrNull { it.name == "textures" }
                    ?: error("Player has no skin texture")
            val json = JsonParser.parseString(Base64.decode(texturesProp.value).decodeToString()).asJsonObject
            val skinUrl =
                json
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url")
                    .asString

            // 2. load and crop
            val fullImg =
                ImageIO.read(URI.create(skinUrl).toURL()) ?: error("Failed to load skin image from URL: $skinUrl")
            val face = fullImg.getSubimage(FACE_X, FACE_Y, FACE_WIDTH, FACE_HEIGHT)

            // 3. scale & build MiniMessage
            val scale = FACE_WIDTH.toDouble() / size

            return buildString {
                for (y in 0 until size) {
                    for (x in 0 until size) {
                        val px = (x * scale).toInt().coerceAtMost(MAX_COORDINATE)
                        val py = (y * scale).toInt().coerceAtMost(MAX_COORDINATE)
                        val rgb = face.getRGB(px, py)
                        val a = (rgb ushr ALPHA_SHIFT) and COLOR_MASK
                        val r = (rgb shr RED_SHIFT) and COLOR_MASK
                        val g = (rgb shr GREEN_SHIFT) and COLOR_MASK
                        val b = rgb and COLOR_MASK

                        if (a == 0) {
                            append("<color:$BLACK_COLOR>$PIXEL_CHAR</color>")
                        } else {
                            append("<color:#%02x%02x%02x>$PIXEL_CHAR</color>".format(r, g, b))
                        }
                    }
                    append("\n")
                }
            }
        }

        /**
         * Get [org.bukkit.block.Container]s around a [Player] (3x3 [org.bukkit.Chunk] area).
         *
         * @return [Set] of [org.bukkit.block.Container]s around the player.
         */
        fun Player.getContainersAround(): Set<Container> =
            buildSet {
                for (chunk in getChunksAround()) {
                    for (state in chunk.tileEntities) {
                        if (state is Container) add(state)
                    }
                }
            }

        /**
         * Get [org.bukkit.Chunk]s around a [Player] (3x3 [org.bukkit.Chunk] area).
         *
         * @param range The [org.bukkit.Chunk] radius around the player (1 = 3x3 area).
         * @return [Set] of [org.bukkit.Chunk]s around the player.
         */
        fun Player.getChunksAround(range: Int = 1): Set<Chunk> {
            val (baseX, baseZ) = location.chunk.run { x to z }

            return buildSet {
                for (x in -range..range) {
                    for (z in -range..range) {
                        add(world.getChunkAt(baseX + x, baseZ + z))
                    }
                }
            }
        }

        /**
         * Gets the first leashed [org.bukkit.entity.Tameable] entity owned by a [Player] within the config radius.
         *
         * @param radius The radius within which to search for leashed entities.
         * @return The found [org.bukkit.entity.Tameable] entity or `null` if none exists.
         */
        fun Player.getLeashedEntity(radius: Double = 10.0): Tameable? =
            getNearbyEntities(radius, radius, radius)
                .filterIsInstance<Tameable>()
                .firstOrNull { it.isLeashed && it.leashHolder == this }

        /**
         * Applies the correct scoreboard to a [Player] based on their visibility preference.
         */
        fun Player.applyScoreboard() {
            scoreboard =
                if (scoreboardVisibility) {
                    instance.server.scoreboardManager.newScoreboard
                } else {
                    instance.server.scoreboardManager.mainScoreboard
                }
        }

        /**
         * Modifies the colour of a [Player]'s waypoint based on the specified parameters.
         *
         * @param color The optional [net.kyori.adventure.text.format.TextColor] to apply to the waypoint.
         */
        fun Player.locator(color: TextColor? = null) {
            waypointColor = color?.let { Color.fromRGB(it.value()) }
            sendActionBar(Component.text("Locator color changed!", color))
        }

        /** Sets the display name of the player based on their nickname. */
        fun Player.setNickname() = displayName(MM.deserialize(nickname))

        /** Returns an [ItemStack] of this player's head with their skin profile applied. */
        @Suppress("UnstableApiUsage")
        fun Player.head(): ItemStack =
            ItemStack.of(Material.PLAYER_HEAD).apply {
                setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(playerProfile))
            }
    }
}
