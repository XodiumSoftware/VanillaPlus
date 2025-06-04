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
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.cdimascio.dotenv.dotenv
import io.papermc.paper.ban.BanListType
import kotlinx.coroutines.*
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.DiscordData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.time.Instant
import java.util.*

class DiscordModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DiscordModule.ENABLED

    private val guildId = Snowflake(691029695894126623)
    private val configId = UUID.nameUUIDFromBytes(guildId.value.toString().toByteArray())
    private val token = dotenv()["DISCORD_BOT_TOKEN"]
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val whitelist = instance.server.whitelistedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }
    private val blacklist = instance.server.bannedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }

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
        createGuildChatInputCommand(guildId, "whitelist", "Manage the whitelist") {
            string("action", "Add, remove, or list players on the whitelist") {
                required = true
                choice("add", "add")
                choice("remove", "remove")
                choice("list", "list")
            }
            string("player", "The player name to whitelist") { required = false }
            defaultMemberPermissions = Permissions(Permission.Administrator)
        }
        createGuildChatInputCommand(guildId, "blacklist", "Manage the blacklist") {
            string("action", "Add or remove the player from the blacklist") {
                required = true
                choice("add", "add")
                choice("remove", "remove")
                choice("list", "list")
            }
            string("player", "The player name to blacklist") { required = false }
            defaultMemberPermissions = Permissions(Permission.Administrator)
        }
        createGuildChatInputCommand(guildId, "setup", "Setup the Discord bot") {
            defaultMemberPermissions = Permissions(Permission.Administrator)
        }
    }

    /** Registers the event listeners for the Discord bot. */
    private fun Kord.registerEvents() {
        on<ComponentInteractionCreateEvent> {
            logInteraction(this)
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
                        interaction.modal("whitelist_add_modal", "Add to Whitelist") {
                            actionRow {
                                textInput(TextInputStyle.Short, "player_name", "Player Name") {
                                    required = true
                                }
                            }
                        }
                    }

                    "whitelist_remove_button" -> {
                        interaction.modal("whitelist_remove_modal", "Remove from Whitelist") {
                            actionRow {
                                textInput(TextInputStyle.Short, "player_name", "Player Name") {
                                    required = true
                                }
                            }
                        }
                    }

                    "whitelist_list_button" -> {
                        interaction.respondEphemeral {
                            embeds = mutableListOf(
                                embed(
                                    "Whitelisted Players",
                                    whitelist.ifEmpty { "No players are whitelisted." })
                            )
                        }
                    }

                    "blacklist_add_button" -> {
                        interaction.modal("blacklist_add_modal", "Add to Blacklist") {
                            actionRow {
                                textInput(TextInputStyle.Short, "player_name", "Player Name") {
                                    required = true
                                }
                            }
                        }
                    }

                    "blacklist_remove_button" -> {
                        interaction.modal("blacklist_remove_modal", "Remove from Blacklist") {
                            actionRow {
                                textInput(TextInputStyle.Short, "player_name", "Player Name") {
                                    required = true
                                }
                            }
                        }
                    }

                    "blacklist_list_button" -> {
                        interaction.respondEphemeral {
                            embeds = mutableListOf(
                                embed(
                                    "Blacklisted Players",
                                    blacklist.ifEmpty { "No players are blacklisted." })
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
            logInteraction(this)
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
                            embeds = mutableListOf(embed("Whitelist Management", "Choose an action:"))
                            components = mutableListOf(
                                ActionRowBuilder().apply {
                                    interactionButton(ButtonStyle.Primary, "whitelist_add_button") {
                                        label = "Add"
                                    }
                                    interactionButton(ButtonStyle.Primary, "whitelist_remove_button") {
                                        label = "Remove"
                                    }
                                    interactionButton(ButtonStyle.Secondary, "whitelist_list_button") {
                                        label = "List"
                                    }
                                }
                            )
                        }
                    }

                    "blacklist" -> {
                        if (!isChannelAllowed(this)) return@on
                        interaction.respondEphemeral {
                            embeds = mutableListOf(embed("Blacklist Management", "Choose an action:"))
                            components = mutableListOf(
                                ActionRowBuilder().apply {
                                    interactionButton(ButtonStyle.Primary, "blacklist_add_button") {
                                        label = "Add"
                                    }
                                    interactionButton(ButtonStyle.Primary, "blacklist_remove_button") {
                                        label = "Remove"
                                    }
                                    interactionButton(ButtonStyle.Secondary, "blacklist_list_button") {
                                        label = "List"
                                    }
                                }
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
            logInteraction(this)
            try {
                when (interaction.modalId) {
                    "whitelist_add_modal" -> {
                        val playerName = interaction.textInputs["player_name"]?.value ?: ""
                        if (playerName.isBlank()) {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Invalid Input",
                                        "Please provide a valid player name.",
                                        color = 0xFF0000
                                    )
                                )
                            }
                        } else {
                            updateList("whitelist", "add", playerName)
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Whitelist Update",
                                        "Player `$playerName` has been added to the whitelist."
                                    )
                                )
                            }
                        }
                    }

                    "whitelist_remove_modal" -> {
                        val playerName = interaction.textInputs["player_name"]?.value ?: ""
                        if (playerName.isBlank()) {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Invalid Input",
                                        "Please provide a valid player name.",
                                        color = 0xFF0000
                                    )
                                )
                            }
                        } else {
                            updateList("whitelist", "remove", playerName)
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Whitelist Update",
                                        "Player `$playerName` has been removed from the whitelist."
                                    )
                                )
                            }
                        }
                    }

                    "blacklist_add_modal" -> {
                        val playerName = interaction.textInputs["player_name"]?.value ?: ""
                        if (playerName.isBlank()) {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Invalid Input",
                                        "Please provide a valid player name.",
                                        color = 0xFF0000
                                    )
                                )
                            }
                        } else {
                            updateList("blacklist", "add", playerName)
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Blacklist Update",
                                        "Player `$playerName` has been blacklisted."
                                    )
                                )
                            }
                        }
                    }

                    "blacklist_remove_modal" -> {
                        val playerName = interaction.textInputs["player_name"]?.value ?: ""
                        if (playerName.isBlank()) {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Invalid Input",
                                        "Please provide a valid player name.",
                                        color = 0xFF0000
                                    )
                                )
                            }
                        } else {
                            updateList("blacklist", "remove", playerName)
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Blacklist Update",
                                        "Player `$playerName` has been removed from the blacklist."
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
     * Updates the whitelist or blacklist for a player.
     * @param command The command type ("whitelist" or "blacklist").
     * @param action The action to perform ("add" or "remove").
     * @param playerName The name of the player to update.
     */
    private suspend fun updateList(command: String, action: String, playerName: String) = withContext(Dispatchers.IO) {
        instance.server.scheduler.runTask(instance, Runnable {
            val offlinePlayer = instance.server.getOfflinePlayer(playerName)
            val banList = instance.server.getBanList(BanListType.PROFILE)
            when (command) {
                "whitelist" -> offlinePlayer.isWhitelisted = (action == "add")
                "blacklist" -> if (action == "add")
                    banList.addBan(offlinePlayer.playerProfile, "", Date.from(Instant.MAX), "")
                else
                    banList.pardon(offlinePlayer.playerProfile)
            }
        })
    }

    /**
     * Logs the interaction event to the console.
     * @param event The interaction event to log.
     */
    private fun logInteraction(event: InteractionCreateEvent) {
        val user = event.interaction.user
        val userId = user.id.value
        val username = user.username
        val commandName = when (event) {
            is ChatInputCommandInteractionCreateEvent -> event.interaction.command.rootName
            is ComponentInteractionCreateEvent -> event.interaction.componentId
            else -> "Unknown Command"
        }

        instance.logger.info("Discord: User $username ($userId) used command '$commandName'")
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