/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.kord.common.Color
import dev.kord.common.entity.*
import dev.kord.common.entity.optional.firstOrNull
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling discord mechanics within the system. */
class DiscordModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DiscordModule.ENABLED

    private val token = dotenv()["DISCORD_BOT_TOKEN"]
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val whitelist
        get() = instance.server.whitelistedPlayers
    private val httpClient = HttpClient()

    private var kord: Kord? = null

    private companion object {
        private const val DEFAULT_EMBED_COLOR = 0x00FF00
        private const val EVENT_CHANNEL_ID = 1285516564153761883L
    }

    init {
        if (enabled()) {
            if (!token.isNullOrBlank())
                bot(token) else instance.logger.warning("Warning: Discord bot token is not set!")
        }
    }

    /**
     * Starts the Discord bot and registers commands.
     * @param token The Discord bot token.
     */
    private fun bot(token: String) {
        scope.launch {
            try {
                kord = Kord(token)
                kord?.let {
                    it.registerCommands()
                    it.registerEvents()
                    it.login {}
                }
            } catch (e: Exception) {
                instance.logger.severe("Error: Failed to start Discord bot: ${e.message}")
            }
        }
    }

    /** Registers the Discord commands for the bot. */
    private suspend fun Kord.registerCommands() {
        val gid = guilds.firstOrNull()?.id
        if (gid == null) {
            instance.logger.warning("Warning: Bot has not been added to a Guild.")
            return
        }
        createGuildChatInputCommand(gid, "whitelist", "Manage the whitelist") {
            defaultMemberPermissions = Permissions(Permission.Administrator)
        }
        createGuildChatInputCommand(gid, "online", "Online players") {}
    }

    /** Registers the event listeners for the Discord bot. */
    private fun Kord.registerEvents() {
        on<ComponentInteractionCreateEvent> {
            try {
                when (interaction.componentId) {
                    "whitelist_add_button" -> {
                        interaction.modal("Add to Whitelist", "whitelist_add_modal") {
                            actionRow {
                                textInput(TextInputStyle.Short, "player_name", "Player Name") {
                                    required = true
                                }
                            }
                        }
                    }

                    "whitelist_remove_button" -> {
                        if (whitelist.isNotEmpty()) {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "\uD83D\uDCDC Remove from Whitelist",
                                        "Select a player to remove from the whitelist."
                                    )
                                )
                                components = mutableListOf(
                                    ActionRowBuilder().apply {
                                        stringSelect("whitelist_remove_select") {
                                            placeholder = "Select player"
                                            whitelist.mapNotNull { it.name }.forEach { option(it, it) }
                                        }
                                    }
                                )
                            }
                        } else {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "❌ No Whitelisted Players",
                                        "There are no players in the whitelist to remove."
                                    )
                                )
                            }
                        }
                    }

                    "whitelist_remove_select" -> {
                        val player = interaction.data.data.values.firstOrNull { it.isNotEmpty() } ?: ""
                        if (player.isBlank()) {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "❌ Invalid Selection",
                                        "Please select a valid player.",
                                        color = 0xFF0000
                                    )
                                )
                            }
                        } else {
                            updateWhitelist(interaction.user, "remove", player)
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "\uD83D\uDCDC Whitelist Update",
                                        "Player `$player` has been removed from the whitelist."
                                    )
                                )
                            }
                        }
                    }

                    "whitelist_list_button" -> {
                        interaction.respondEphemeral {
                            embeds = mutableListOf(
                                embed(
                                    "\uD83D\uDCDC Whitelisted Players",
                                    whitelist.joinToString("\n") { it.name.toString() }
                                        .ifEmpty { "No players are whitelisted." })
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                tryCatchMsg(interaction, e)
            }
        }

        on<ChatInputCommandInteractionCreateEvent> {
            try {
                when (interaction.command.rootName) {
                    "whitelist" -> {
                        interaction.respondEphemeral {
                            embeds =
                                mutableListOf(embed("\uD83D\uDCDC Whitelist Management", "Choose an action:"))
                            components = mutableListOf(
                                ActionRowBuilder().apply {
                                    interactionButton(ButtonStyle.Success, "whitelist_add_button") {
                                        label = "Add"
                                    }
                                    interactionButton(ButtonStyle.Danger, "whitelist_remove_button") {
                                        label = "Remove"
                                        disabled = whitelist.isEmpty()
                                    }
                                    interactionButton(ButtonStyle.Primary, "whitelist_list_button") {
                                        label = "List"
                                        disabled = whitelist.isEmpty()
                                    }
                                }
                            )
                        }
                    }

                    "online" -> {
                        val players = instance.server.onlinePlayers.joinToString(", ") { it.name }
                        interaction.respondEphemeral {
                            embeds = mutableListOf(
                                embed(
                                    "⚡ Online Players",
                                    players.ifEmpty { "No players are currently online." }
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                tryCatchMsg(interaction, e)
            }
        }

        on<ModalSubmitInteractionCreateEvent> {
            try {
                when (interaction.modalId) {
                    "whitelist_add_modal" -> {
                        val player = interaction.textInputs["player_name"]?.value ?: ""
                        when {
                            player.isBlank() -> {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed(
                                            "❌ Invalid Input",
                                            "Please provide a valid player name.",
                                            color = 0xFF0000
                                        )
                                    )
                                }
                            }

                            whitelist.any { it.name.equals(player, ignoreCase = true) } -> {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed(
                                            "❌ Player Already Whitelisted",
                                            "Player `$player` is already in the whitelist.",
                                            color = 0xFF0000
                                        )
                                    )
                                }
                            }

                            !isValidMinecraftUsername(player) -> {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed(
                                            "❌ Invalid Player Name",
                                            "The player name `$player` does not exist in Minecraft.",
                                            color = 0xFF0000
                                        )
                                    )
                                }
                            }

                            else -> {
                                updateWhitelist(interaction.user, "add", player)
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed(
                                            "\uD83D\uDCDC Whitelist Update",
                                            "Player `$player` has been added to the whitelist."
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                tryCatchMsg(interaction, e)
            }
        }
    }

    private suspend fun tryCatchMsg(interaction: ActionInteraction, e: Exception) {
        instance.logger.severe("Discord interaction error: ${e.message}\n${e.stackTraceToString()}")
        interaction.respondEphemeral {
            embeds = mutableListOf(
                embed(
                    title = "❌ Error",
                    description = "An error occurred while processing your command: ${e.message}",
                    color = 0xFF0000
                )
            )
        }
    }

    /**
     * Validates a Minecraft username by checking if it exists in Mojang's API.
     * @param username The Minecraft username to validate.
     * @return True if the username is valid, false otherwise.
     */
    private suspend fun isValidMinecraftUsername(username: String): Boolean {
        return httpClient.get("https://api.mojang.com/users/profiles/minecraft/$username").status == HttpStatusCode.OK
    }

    /**
     * Updates the whitelist.
     * @param discordUser The Discord user performing the action.
     * @param action The action to perform ("add" or "remove").
     * @param playerName The name of the player to update.
     */
    private suspend fun updateWhitelist(discordUser: User, action: String, playerName: String): BukkitTask {
        return withContext(Dispatchers.IO) {
            instance.server.scheduler.runTask(instance, Runnable {
                instance.server.getOfflinePlayer(playerName).isWhitelisted = (action == "add")
                instance.logger.info("Whitelist: ${discordUser.username} (${discordUser.id.value}) performed: $action $playerName")
            })
        }
    }

    /**
     * Creates an embed builder with the specified title, description, and color.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @param url An optional URL for the embed.
     * @param color The color of the embed in hexadecimal format.
     * @return An EmbedBuilder instance with the specified properties.
     */
    private fun embed(
        title: String? = null,
        description: String? = null,
        url: String? = null,
        color: Int? = null,
    ): EmbedBuilder {
        return EmbedBuilder().apply {
            this.title = title
            this.description = description
            this.url = url
            this.color = Color(color ?: DEFAULT_EMBED_COLOR)
        }
    }

    /**
     * Sends an embed message in response to an interaction.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @param url An optional URL for the embed.
     * @param color The color of the embed in hexadecimal format.
     */
    suspend fun sendEventEmbed(
        title: String? = null,
        description: String? = null,
        url: String? = null,
        color: Int? = DEFAULT_EMBED_COLOR
    ) {
        kord?.getChannelOf<TextChannel>(Snowflake(EVENT_CHANNEL_ID))?.createMessage {
            embeds = mutableListOf(embed(title, description, url, color))
        }
    }

    /** Disables the Discord module, canceling the coroutine scope and nullifying the Kord instance. */
    suspend fun disable() {
        scope.cancel()
        kord?.shutdown()
        kord = null
    }
}