/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.kord.common.Color
import dev.kord.common.entity.*
import dev.kord.common.entity.optional.firstOrNull
import dev.kord.common.entity.optional.map
import dev.kord.common.entity.optional.orEmpty
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.*
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.DiscordData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*

class DiscordModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DiscordModule.ENABLED

    private val guildId = Snowflake(691029695894126623)
    private val configId = UUID.nameUUIDFromBytes(guildId.value.toString().toByteArray())
    private val token = dotenv()["DISCORD_BOT_TOKEN"]
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val whitelist: String
        get() = instance.server.whitelistedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }

    private var kord: Kord? = null
    private var channelIds: List<Snowflake>? = emptyList()

    init {
        if (enabled()) {
            if (!token.isNullOrBlank()) {
                DiscordData.createTable()
                DiscordData.getData().firstOrNull { it.id == configId }?.let { channelIds = it.allowedChannels }
                bot(token)
            } else instance.logger.warning("Warning: Discord bot token is not set!")
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
                instance.logger.severe("Failed to start Discord bot: ${e.message}")
            }
        }
    }

    /** Registers the Discord commands for the bot. */
    private suspend fun Kord.registerCommands() {
        createGuildChatInputCommand(guildId, "setup", "Setup the Discord bot") {
            defaultMemberPermissions = Permissions(Permission.Administrator)
        }
        createGuildChatInputCommand(guildId, "whitelist", "Manage the whitelist") {
            defaultMemberPermissions = Permissions(Permission.Administrator)
        }
        createGuildChatInputCommand(guildId, "online", "Online players") {}
    }

    /** Registers the event listeners for the Discord bot. */
    private fun Kord.registerEvents() {
        on<ComponentInteractionCreateEvent> {
            log()
            try {
                when (interaction.componentId) {
                    "setup_select" -> {
                        when (interaction.data.data.values.firstOrNull { it.isNotEmpty() }) {
                            "channels" -> {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed(
                                            "⚙\uFE0F Setup | Allowed Channels",
                                            "Select the allowed channels:"
                                        )
                                    )
                                    components = mutableListOf(
                                        ActionRowBuilder().apply {
                                            channelSelect("channel_select") {
                                                placeholder = "Choose allowed channels"
                                                channelTypes = mutableListOf(ChannelType.GuildText)
                                                allowedValues = 1..25
                                                channelIds?.let { defaultChannels.addAll(it) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    "channel_select" -> {
                        channelIds = interaction.data.data.values.map { it.map(::Snowflake) }.orEmpty()
                        DiscordData.setData(DiscordData(id = configId, allowedChannels = channelIds))
                        interaction.respondEphemeral {
                            embeds = mutableListOf(
                                embed(
                                    "⚙\uFE0F Setup | Allowed Channels",
                                    "Allowed channels updated successfully."
                                )
                            )
                        }
                    }

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
                        val players = instance.server.whitelistedPlayers.mapNotNull { it.name }
                        if (players.isNotEmpty()) {
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
                                            players.forEach { option(it, it) }
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
                                    embed("❌ Invalid Selection", "Please select a valid player.", color = 0xFF0000)
                                )
                            }
                        } else {
                            updateWhitelist("remove", player)
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
                                    whitelist.ifEmpty { "No players are whitelisted." })
                            )
                        }
                    }
                }
            } catch (e: Exception) {
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
        }

        on<ChatInputCommandInteractionCreateEvent> {
            log()
            try {
                when (interaction.command.rootName) {
                    "setup" -> {
                        interaction.respondEphemeral {
                            embeds = mutableListOf(embed("⚙\uFE0F Setup", "Configure channels or roles:"))
                            components = mutableListOf(
                                ActionRowBuilder().apply {
                                    stringSelect("setup_select") {
                                        placeholder = "Choose what to setup"
                                        option("Channels", "channels")
                                    }
                                }
                            )
                        }
                    }

                    "whitelist" -> {
                        if (!isChannelAllowed(this)) return@on
                        interaction.respondEphemeral {
                            embeds = mutableListOf(embed("\uD83D\uDCDC Whitelist Management", "Choose an action:"))
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
                        if (!isChannelAllowed(this)) return@on
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
        }

        on<ModalSubmitInteractionCreateEvent> {
            log()
            try {
                when (interaction.modalId) {
                    "whitelist_add_modal" -> {
                        val player = interaction.textInputs["player_name"]?.value ?: ""
                        if (player.isBlank()) {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "❌ Invalid Input",
                                        "Please provide a valid player name.",
                                        color = 0xFF0000
                                    )
                                )
                            }
                        } else {
                            updateWhitelist("add", player)
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
            } catch (e: Exception) {
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
        }
    }

    /**
     * Checks if the interaction is in an allowed channel.
     * If not, responds with an ephemeral message indicating the command can only be used in designated channels.
     * @param event The interaction event to check.
     * @return True if the channel is allowed, false otherwise.
     */
    private suspend fun isChannelAllowed(event: ChatInputCommandInteractionCreateEvent): Boolean {
        channelIds?.let {
            if (it.isEmpty()) {
                event.interaction.respondEphemeral {
                    embeds = mutableListOf(
                        embed(
                            title = "❌ Channel Restriction",
                            description = "No allowed channels are configured. Please use the `/setup channels` command to select allowed channels.",
                            color = 0xFFA500
                        )
                    )
                }
                return false
            }
        }
        channelIds?.let { ids ->
            if (event.interaction.channelId !in ids) {
                val allowedMentions = channelIds?.joinToString(", ") { "<#${it.value}>" }
                event.interaction.respondEphemeral {
                    embeds = mutableListOf(
                        embed(
                            title = "❌ Channel Restriction",
                            description = "This command can only be executed in the designated channel(s): \n$allowedMentions",
                            color = 0xFF0000
                        )
                    )
                }
                return false
            }
        }
        return true
    }

    /**
     * Updates the whitelist.
     * @param action The action to perform ("add" or "remove").
     * @param playerName The name of the player to update.
     */
    private suspend fun updateWhitelist(action: String, playerName: String) = withContext(Dispatchers.IO) {
        instance.server.scheduler.runTask(instance, Runnable {
            instance.server.getOfflinePlayer(playerName).isWhitelisted = (action == "add")
        })
    }

    /**
     * Logs the interaction event to the console.
     * @param enabled Whether logging is enabled (default is true).
     */
    private fun InteractionCreateEvent.log(enabled: Boolean = true) {
        if (!enabled) return
        val user = interaction.user
        val userId = user.id.value
        val username = user.username
        val commandName = when (this) {
            is ChatInputCommandInteractionCreateEvent -> interaction.command.rootName
            is ComponentInteractionCreateEvent -> interaction.componentId
            is ModalSubmitInteractionCreateEvent -> interaction.modalId
            else -> "Unknown Command"
        }
        val value = when (this) {
            is ChatInputCommandInteractionCreateEvent -> interaction.command.data.options.firstOrNull { true }?.value
            is ComponentInteractionCreateEvent -> interaction.data.data.values.firstOrNull { true }
            is ModalSubmitInteractionCreateEvent -> interaction.textInputs["player_name"]?.value
            else -> null
        }
        val valuePart = if (value != null && value.toString().isNotBlank()) " and changed value to '$value'" else ""

        instance.logger.info("Discord: User $username ($userId) used command '$commandName'$valuePart")
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
            this.color = Color(color ?: 0x00FF00)
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
        color: Int? = 0x00FF00
    ) {
        kord?.getChannelOf<TextChannel>(Snowflake(1285516564153761883))?.createMessage {
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