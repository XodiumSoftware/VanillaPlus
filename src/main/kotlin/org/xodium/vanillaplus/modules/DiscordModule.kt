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

    private var kord: Kord? = null
    private var job: Job? = null

    init {
        start(TODO("setup way to get token"))
    }

    /**
     * Starts the Discord bot with the provided token.
     * @param token The Discord bot token.
     */
    private fun start(token: String) {
        job = CoroutineScope(Dispatchers.Default).launch {
            kord = Kord(token)
            kord?.createGlobalChatInputCommand("whitelist", "Whitelist a player") {
                string("player", "The player name to whitelist") { required = true }
            }
            kord?.on<ChatInputCommandInteractionCreateEvent> {
                if (interaction.command.rootName == "whitelist") {
                    val playerName = interaction.command.strings["player"] ?: ""
                    if (playerName.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            instance.server.scheduler.runTask(instance, Runnable {
                                val offlinePlayer = instance.server.getOfflinePlayer(playerName)
                                instance.server.whitelistedPlayers.add(offlinePlayer)
                            })
                        }
                        interaction.respondEphemeral {
                            content = "Player `$playerName` has been whitelisted."
                        }
                    } else {
                        interaction.respondEphemeral {
                            content = "Please provide a valid player name."
                        }
                    }
                }
            }
        }
    }
}