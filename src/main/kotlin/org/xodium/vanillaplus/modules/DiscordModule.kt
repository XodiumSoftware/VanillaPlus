/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.cdimascio.dotenv.dotenv
import io.papermc.paper.ban.BanListType
import kotlinx.coroutines.*
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.time.Instant
import java.util.*

class DiscordModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DiscordModule.ENABLED

    private val token = dotenv()["DISCORD_BOT_TOKEN"]
    private val guildId = Snowflake(691029695894126623)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val whitelist = instance.server.whitelistedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }
    private val blacklist = instance.server.bannedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }

    private var kord: Kord? = null

    init {
        if (token.isNullOrBlank()) instance.logger.warning("Warning: Discord bot token is not set!") else bot(token)
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

    /**
     * Registers the Discord commands for the bot.
     * This includes commands for managing the whitelist, blacklist, and opening the online server map.
     */
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
        createGuildChatInputCommand(guildId, "map", "Open the online server map") {}
    }

    /**
     * Registers the event listeners for the Discord bot.
     * This includes handling command interactions.
     */
    private fun Kord.registerEvents() {
        on<ChatInputCommandInteractionCreateEvent> {
            val action = interaction.command.strings["action"] ?: ""
            val playerName = interaction.command.strings["player"] ?: ""

            when (interaction.command.rootName) {
                "whitelist" -> {
                    when (action) {
                        "list" -> {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Whitelisted Players",
                                        whitelist.ifEmpty { "No players are whitelisted." }
                                    )
                                )
                            }
                        }

                        "add" -> {
                            if (playerName.isBlank()) {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed("Invalid Input", "Please provide a valid player name.")
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

                        "remove" -> {
                            if (playerName.isBlank()) {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed("Invalid Input", "Please provide a valid player name.")
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
                    }
                }

                "blacklist" -> {
                    when (action) {
                        "list" -> {
                            interaction.respondEphemeral {
                                embeds = mutableListOf(
                                    embed(
                                        "Blacklisted Players",
                                        blacklist.ifEmpty { "No players are blacklisted." }
                                    )
                                )
                            }
                        }

                        "add" -> {
                            if (playerName.isBlank()) {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed("Invalid Input", "Please provide a valid player name.")
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

                        "remove" -> {
                            if (playerName.isBlank()) {
                                interaction.respondEphemeral {
                                    embeds = mutableListOf(
                                        embed("Invalid Input", "Please provide a valid player name.")
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
                }

                "map" -> {
                    interaction.respondEphemeral {
                        embeds = mutableListOf(
                            embed(
                                "Open the Online Server Map",
                                "Click the title above to open the map.",
                                "https://illyria.xodium.org/"
                            )
                        )
                    }
                }
            }
        }
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
     * Creates an embed builder with the specified title, description, and color.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @param color The color of the embed in hexadecimal format.
     * @param url An optional URL for the embed.
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
    fun disable() {
        scope.cancel()
        kord = null
    }
}