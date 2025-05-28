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

    private lateinit var kord: Kord

    init {
        if (token.isNullOrBlank()) instance.logger.warning("Warning: Discord bot token is not set!") else start(token)
    }

    /**
     * Starts the Discord bot and registers commands.
     * @param token The Discord bot token.
     */
    private fun start(token: String) {
        scope.launch {
            try {
                kord = Kord(token)
                kord.createGuildChatInputCommand(guildId, "whitelist", "Manage the whitelist") {
                    string("action", "Add, remove, or list players on the whitelist") {
                        required = true
                        choice("add", "add")
                        choice("remove", "remove")
                        choice("list", "list")
                    }
                    string("player", "The player name to whitelist") { required = false }
                    defaultMemberPermissions = Permissions(Permission.Administrator)
                }
                kord.createGuildChatInputCommand(guildId, "blacklist", "Manage the blacklist") {
                    string("action", "Add or remove the player from the blacklist") {
                        required = true
                        choice("add", "add")
                        choice("remove", "remove")
                        choice("list", "list")
                    }
                    string("player", "The player name to blacklist") { required = false }
                    defaultMemberPermissions = Permissions(Permission.Administrator)
                }
                kord.createGuildChatInputCommand(guildId, "map", "Open the online server map") {}
                kord.on<ChatInputCommandInteractionCreateEvent> { handleListCommand(this) }
                kord.login {}
            } catch (e: Exception) {
                instance.logger.severe("Failed to start Discord bot: ${e.message}")
            }
        }
    }

    /**
     * Handles the whitelist and blacklist commands.
     * @param event The interaction event containing the command details.
     */
    private suspend fun handleListCommand(event: ChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val commandName = interaction.command.rootName
        if (commandName != "whitelist" && commandName != "blacklist" && commandName != "map") return

        instance.logger.info(
            "Discord command executed: /$commandName by ${interaction.user.username}#${interaction.user.discriminator} (ID: ${interaction.user.id.value}) " +
                    "with options: action=${interaction.command.strings["action"]}, player=${interaction.command.strings["player"]}"
        )

        val action = interaction.command.strings["action"] ?: ""
        val playerName = interaction.command.strings["player"] ?: ""

        when (commandName) {
            "map" -> {
                event.interaction.respondEphemeral {
                    embeds = mutableListOf(
                        EmbedBuilder().apply {
                            title = "Open the Online Server Map"
                            url = "https://illyria.xodium.org/"
                            description = "Click the title above to open the map."
                            color = Color(0x00FF00)
                        }
                    )
                }
                return
            }
        }

        if (commandName == "whitelist" && action == "list") {
            val whitelisted =
                instance.server.whitelistedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }
            interaction.respondEphemeral {
                embeds = mutableListOf(
                    EmbedBuilder().apply {
                        title = "Whitelisted Players"
                        description = whitelisted.ifEmpty { "No players are whitelisted." }
                        color = Color(0x00FF00)
                    }
                )
            }
            return
        }

        if (commandName == "blacklist" && action == "list") {
            val blacklisted = instance.server.bannedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }
            interaction.respondEphemeral {
                embeds = mutableListOf(
                    EmbedBuilder().apply {
                        title = "Blacklisted Players"
                        description = blacklisted.ifEmpty { "No players are blacklisted." }
                        color = Color(0x00FF00)
                    }
                )
            }
            return
        }

        if (playerName.isEmpty() || (action != "add" && action != "remove")) {
            interaction.respondEphemeral {
                embeds = mutableListOf(
                    EmbedBuilder().apply {
                        title = "Invalid Input"
                        description = "Please provide a valid player name and action (add/remove)."
                        color = Color(0x00FF00)
                    }
                )
            }
            return
        }

        withContext(Dispatchers.IO) {
            instance.server.scheduler.runTask(instance, Runnable {
                val offlinePlayer = instance.server.getOfflinePlayer(playerName)
                val banList = instance.server.getBanList(BanListType.PROFILE)
                when (commandName) {
                    "whitelist" -> when (action) {
                        "add" -> offlinePlayer.isWhitelisted = true
                        "remove" -> offlinePlayer.isWhitelisted = false
                    }

                    "blacklist" -> when (action) {
                        "add" -> banList.addBan(offlinePlayer.playerProfile, "", Date.from(Instant.MAX), "")
                        "remove" -> banList.pardon(offlinePlayer.playerProfile)
                    }
                }
            })
        }

        interaction.respondEphemeral {
            embeds = mutableListOf(
                EmbedBuilder().apply {
                    title = when (commandName) {
                        "whitelist" -> "Whitelist Update"
                        "blacklist" -> "Blacklist Update"
                        else -> "Update"
                    }
                    description = when (commandName) {
                        "whitelist" -> when (action) {
                            "add" -> "Player `$playerName` has been whitelisted."
                            "remove" -> "Player `$playerName` has been removed from the whitelist."
                            else -> "Unknown action."
                        }

                        "blacklist" -> when (action) {
                            "add" -> "Player `$playerName` has been blacklisted."
                            "remove" -> "Player `$playerName` has been removed from the blacklist."
                            else -> "Unknown action."
                        }

                        else -> "Unknown command."
                    }
                    color = Color(0x00FF00)
                }
            )
        }
    }

    /**
     * Sends an embed message in response to an interaction.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @param color The color of the embed in hexadecimal format.
     */
    suspend fun sendEventEmbed(
        title: String,
        description: String,
        color: Int
    ) {
        kord.getChannelOf<TextChannel>(Snowflake(1285516564153761883))?.createMessage {
            embeds = mutableListOf(
                EmbedBuilder().apply {
                    this.title = title
                    this.description = description
                    this.color = Color(color)
                }
            )
        }
    }
}