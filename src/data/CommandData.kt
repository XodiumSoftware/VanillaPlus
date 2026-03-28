package org.xodium.vanillaplus.data

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

/** Represents the data structure for a command definition in the game, including its builder, description and aliases. */
internal data class CommandData(
    /** The [LiteralArgumentBuilder] used to define the command logic and structure. */
    val builder: LiteralArgumentBuilder<CommandSourceStack>,
    /** A description of the command, providing context or information about its purpose. */
    val description: String,
    /** A list of aliases that can be used as alternative command triggers. Defaults to an empty list if no aliases are provided. */
    val aliases: List<String> = emptyList(),
)
