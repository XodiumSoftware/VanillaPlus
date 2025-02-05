/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*


/**
 * Provides utility functions for directory creation and file copying within the plugin.
 */
object Utils {
    private val logger = instance.logger
    val MM: MiniMessage = MiniMessage.miniMessage()
    fun String.mm() = MM.deserialize(this)
    fun List<String>.mm() = map { it.mm() }

    /**
     * Copies all files from a directory inside the plugin JAR to a target directory in the filesystem.
     *
     * This function searches for any files under the [source] directory inside the JAR and copies them
     * to the [target] directory in the filesystem, maintaining the same directory structure.
     *
     * @param source The path to the source directory inside the JAR.
     * @param target The path to the target directory in the filesystem where the files will be copied.
     */
    fun copyResourcesFromJar(source: Path, target: Path) {
        try {
            FileSystems.newFileSystem(
                URI.create("jar:file:${instance.javaClass.protectionDomain.codeSource.location.toURI().path}"),
                emptyMap<String, Any>()
            ).use { it ->
                val sourcePath = it.getPath(source.toString())
                Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .forEach {
                        val targetPath = target.resolve(sourcePath.relativize(it).toString())
                        Files.createDirectories(targetPath.parent)
                        Files.copy(it, targetPath, StandardCopyOption.REPLACE_EXISTING)
                    }
            }
        } catch (ex: Exception) {
            logger.severe("Failed to copy resources from JAR source '$source' to target '$target': ${ex.message}")
        }
    }

    /**
     * Collects all files with a specific extension from a given directory.
     *
     * This function searches recursively in the specified [path] directory for files
     * that end with the given [extension] and returns a list of such files.
     *
     * @param path The directory path to search for files.
     * @param extension The file extension to filter by.
     * @return A list of paths for all files found with the specified extension.
     */
    private fun collectFiles(path: Path, extension: String): List<Path> {
        return try {
            Files.walk(path).use { stream ->
                stream.filter { Files.isRegularFile(it) && it.toString().endsWith(extension, ignoreCase = true) }
                    .toList()
            }
        } catch (ex: Exception) {
            logger.warning("Error processing path ${path.toAbsolutePath()}: ${ex.message}")
            emptyList()
        }
    }

    /**
     * Parses the input to identify directories or files to be collected based on the specified extension.
     *
     * This function handles different types of input (List or String) to determine the paths to search for files.
     * It collects files from each resolved path that match the given [extension].
     *
     * @param input The input specifying directories or files. Can be a List of directories or a single directory path as a String.
     * @param basePath The base path to resolve relative paths from the input.
     * @param extension The file extension to filter by.
     * @return A list of paths for all files found with the specified extension.
     */
    fun parseFiles(input: Any, basePath: Path, extension: String): List<Path> {
        return try {
            when (input) {
                is List<*> -> input.mapNotNull { it?.toString() }
                    .flatMap { collectFiles(basePath.resolve(it), extension) }

                is String -> collectFiles(basePath.resolve(input), extension)
                else -> {
                    logger.warning("Invalid value type: $input")
                    emptyList()
                }
            }
        } catch (ex: Exception) {
            logger.warning("Error parsing files from input $input: ${ex.message}")
            emptyList()
        }
    }

    /**
     * Plays a sound at the location of the specified block.
     *
     * @param block The block at whose location the sound will be played.
     * @param sound The name of the sound to play. If null or the sound is not found, the fallback sound will be used.
     * @param fallbackSound The sound to play if the specified sound is not found or is null.
     * @param volume The volume at which to play the sound. This should be a positive integer.
     * @param pitch The pitch at which to play the sound. This should be a positive integer.
     */
    fun playSound(block: Block, sound: String?, fallbackSound: Sound, volume: Int, pitch: Int) {
        try {
            block.world.playSound(
                block.location,
                sound
                    ?.lowercase(Locale.getDefault())
                    ?.let(NamespacedKey::minecraft)
                    ?.let(Registry.SOUNDS::get)
                    ?: fallbackSound,
                volume.toFloat(),
                pitch.toFloat()
            )
        } catch (ex: Exception) {
            logger.severe("Failed to play sound '${sound ?: fallbackSound}' at block '${block.location}': ${ex.message}")
            ex.printStackTrace()
        }
    }

    /**
     * A helper function to wrap command execution with standardized error handling.
     *
     * @param ctx The CommandContext used to obtain the CommandSourceStack.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    @Suppress("UnstableApiUsage")
    fun tryCatch(ctx: CommandContext<CommandSourceStack>, action: (CommandSourceStack) -> Unit): Int {
        try {
            action(ctx.source)
        } catch (e: Exception) {
            instance.logger.severe("An Error has occured: ${e.message}")
            e.printStackTrace()
            (ctx.source.sender as Player).sendMessage("${VanillaPlus.PREFIX}<red>An Error has occured. Check server logs for details.".mm())
        }
        return Command.SINGLE_SUCCESS
    }
}