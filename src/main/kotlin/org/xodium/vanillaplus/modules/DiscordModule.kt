/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.*
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.net.HttpURLConnection
import java.net.URI

class DiscordModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DiscordModule.ENABLED

    private val token = dotenv()["DISCORD_BOT_TOKEN"]
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
                kord.createGlobalChatInputCommand("whitelist", "Manage the whitelist") {
                    string("action", "Add or remove the player from the whitelist") {
                        required = true
                        choice("add", "add")
                        choice("remove", "remove")
                    }
                    string("player", "The player name to whitelist") { required = true }
                    defaultMemberPermissions = Permissions(Permission.Administrator)
                }
                kord.createGlobalChatInputCommand("blacklist", "Manage the blacklist") {
                    string("action", "Add or remove the player from the blacklist") {
                        required = true
                        choice("add", "add")
                        choice("remove", "remove")
                    }
                    string("player", "The player name to blacklist") { required = true }
                    defaultMemberPermissions = Permissions(Permission.Administrator)
                }
                kord.on<ChatInputCommandInteractionCreateEvent> {
                    handleListCommand(this)
                }
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
        if (commandName != "whitelist" && commandName != "blacklist") return

        val guildId = interaction.invokedCommandGuildId
        if (guildId == null) {
            interaction.respondEphemeral { content = "This command can only be used in a server." }
            return
        }

        val action = interaction.command.strings["action"] ?: ""
        val playerName = interaction.command.strings["player"] ?: ""
        if (playerName.isEmpty() || (action != "add" && action != "remove")) {
            interaction.respondEphemeral { content = "Please provide a valid player name and action (add/remove)." }
            return
        }

        if (!isValidMinecraftUsername(playerName)) {
            interaction.respondEphemeral { content = "The username `$playerName` does not exist on Minecraft." }
            return
        }

        withContext(Dispatchers.IO) {
            instance.server.scheduler.runTask(instance, Runnable {
                val offlinePlayer = instance.server.getOfflinePlayer(playerName)
                when (commandName) {
                    "whitelist" -> when (action) {
                        "add" -> instance.server.whitelistedPlayers.add(offlinePlayer)
                        "remove" -> instance.server.whitelistedPlayers.remove(offlinePlayer)
                    }

                    "blacklist" -> when (action) {
                        "add" -> instance.server.bannedPlayers.add(offlinePlayer)
                        "remove" -> instance.server.bannedPlayers.remove(offlinePlayer)
                    }
                }
            })
        }
        interaction.respondEphemeral {
            content = when (commandName) {
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
        }
    }

    /**
     * Checks if a given Minecraft username is valid by querying the Mojang API.
     * @param username The Minecraft username to validate.
     * @return True if the username is valid, false otherwise.
     */
    private suspend fun isValidMinecraftUsername(username: String): Boolean {
        return withContext(Dispatchers.IO) {
            val url = URI("https://api.mojang.com/users/profiles/minecraft/$username").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.connect()
            val exists = connection.responseCode == 200
            connection.disconnect()
            exists
        }
    }
}