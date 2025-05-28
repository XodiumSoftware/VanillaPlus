/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import kotlinx.coroutines.*
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class DiscordModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DiscordModule.ENABLED

    private val token = System.getenv("DISCORD_BOT_TOKEN")

    private var kord: Kord? = null
    private var job: Job? = null

    init {
        require(!token.isNullOrBlank()) { "Discord bot token is not set!" }
        start(token)
    }

    /**
     * Starts the Discord bot with the provided token.
     * @param token The Discord bot token.
     */
    private fun start(token: String) {
        job = CoroutineScope(Dispatchers.Default).launch {
            kord = Kord(token)
            kord?.createGlobalChatInputCommand("whitelist", "Manage the whitelist") {
                string("action", "Add or remove the player from the whitelist") {
                    required = true
                    choice("add", "add")
                    choice("remove", "remove")
                }
                string("player", "The player name to whitelist") { required = true }
            }
            kord?.on<ChatInputCommandInteractionCreateEvent> {
                if (interaction.command.rootName == "whitelist") {
                    val guildId = interaction.invokedCommandGuildId
                    if (guildId != null) {
                        val requiredRoleId = "" //TODO: replace with actual role ID
                        val member = interaction.user.asMember(guildId)
                        if (requiredRoleId !in member.roleIds.map { it.toString() }) {
                            interaction.respondEphemeral {
                                content = "You do not have permission to use this command."
                            }
                            return@on
                        }
                    } else {
                        interaction.respondEphemeral {
                            content = "This command can only be used in a server."
                        }
                        return@on
                    }

                    val action = interaction.command.strings["action"] ?: ""
                    val playerName = interaction.command.strings["player"] ?: ""
                    if (playerName.isNotEmpty() && (action == "add" || action == "remove")) {
                        withContext(Dispatchers.IO) {
                            instance.server.scheduler.runTask(instance, Runnable {
                                val offlinePlayer = instance.server.getOfflinePlayer(playerName)
                                if (action == "add") instance.server.whitelistedPlayers.add(offlinePlayer)
                                if (action == "remove") instance.server.whitelistedPlayers.remove(offlinePlayer)
                            })
                        }
                        interaction.respondEphemeral {
                            content = when (action) {
                                "add" -> "Player `$playerName` has been whitelisted."
                                "remove" -> "Player `$playerName` has been removed from the whitelist."
                                else -> "Unknown action."
                            }
                        }
                    } else {
                        interaction.respondEphemeral {
                            content = "Please provide a valid player name and action (add/remove)."
                        }
                    }
                }
            }
        }
    }
}