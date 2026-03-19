package org.xodium.vanillaplus.managers

import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.managers.ConfigManager.data
import org.xodium.vanillaplus.managers.ConfigManager.decodeWith
import org.xodium.vanillaplus.strategies.CapitalizedStrategy
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.ScheduleUtils.runAsync
import org.xodium.vanillaplus.utils.ScheduleUtils.runSync
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.prefix
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.measureTime

/** Manages plugin configuration as a dynamic JSON object, keyed by module class name. */
@OptIn(ExperimentalSerializationApi::class)
internal object ConfigManager {
    /** The [Json] instance used for serialization and deserialization. */
    val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            namingStrategy = CapitalizedStrategy
        }

    /** The current configuration data as a raw [JsonObject]. */
    var data: JsonObject = JsonObject(emptyMap())
        private set

    private val reloadListeners = mutableListOf<() -> Unit>()
    private val registeredKeys = mutableSetOf<String>()

    /**
     * Registers a [listener] to be called whenever the configuration is reloaded.
     * @param listener The callback to invoke on reload.
     */
    fun onReload(listener: () -> Unit) {
        reloadListeners.add(listener)
    }

    /** Notifies all registered reload listeners that the configuration has changed. */
    fun notifyReload() {
        reloadListeners.forEach { it() }
    }

    /**
     * Loads the configuration from [fileName] into [data].
     * Creates the data folder if it does not exist. If the file is absent, [data] is left empty.
     * @param fileName The name of the JSON file inside the plugin data folder.
     */
    fun load(fileName: String) {
        val path = instance.dataFolder.toPath()
        val file = path / fileName

        if (!path.exists()) path.createDirectories()

        val existed = file.exists()
        val ms =
            measureTime {
                data = if (existed) json.decodeFromString<JsonObject>(file.readText()) else JsonObject(emptyMap())
            }.inWholeMilliseconds

        instance.logger.info("${if (existed) "Loaded" else "Created default"} $fileName | Took ${ms}ms")
    }

    /**
     * Saves the current [data] to [fileName] inside the plugin data folder.
     * @param fileName The name of the JSON file to write.
     */
    fun save(fileName: String) {
        val path = instance.dataFolder.toPath()
        val file = path / fileName

        if (!path.exists()) path.createDirectories()

        file.writeText(json.encodeToString(JsonObject(data.toSortedMap())))
    }

    /**
     * Merges a single [element] into [data] under the given [key], replacing any existing value.
     * @param key The top-level JSON key.
     * @param element The [JsonElement] to store.
     */
    fun merge(
        key: String,
        element: JsonElement,
    ) {
        data = JsonObject(data + (key to element))
    }

    /**
     * Decodes the value stored at [key] using [serializer], falling back to [default] if absent or
     * malformed, then merges the (possibly default-filled) result back into [data].
     * @param key The top-level JSON key identifying the module's config section.
     * @param serializer The [KSerializer] for type [T].
     * @param default The fallback value when the key is missing or cannot be decoded.
     * @return The decoded (or default) config value.
     */
    fun <T> decodeWith(
        key: String,
        serializer: KSerializer<T>,
        default: T,
    ): T {
        registeredKeys.add(key)
        val decoded =
            data[key]
                ?.let { runCatching { json.decodeFromJsonElement(serializer, it) }.getOrDefault(default) }
                ?: default
        merge(key, json.encodeToJsonElement(serializer, decoded))
        return decoded
    }

    /**
     * Removes any keys from [data] that were not registered via [decodeWith].
     * Call this before saving to clean up stale module entries.
     */
    fun prune() {
        data = JsonObject(data.filterKeys { it in registeredKeys })
    }

    /** The permission required to execute the reload command. */
    val reloadPermission: Permission by lazy {
        Permission(
            "${instance.javaClass.simpleName}.reload".lowercase(),
            "Allows use of the reload command",
            PermissionDefault.OP,
        )
    }

    /** The command data for the `/vanillaplus reload` command. */
    val reloadCommand: CommandData by lazy {
        CommandData(
            Commands
                .literal("vanillaplus")
                .requires { it.sender.hasPermission(reloadPermission) }
                .then(
                    Commands
                        .literal("reload")
                        .executesCatching {
                            val sender = it.source.sender
                            runAsync {
                                load("config.json")
                                notifyReload()
                                prune()
                                save("config.json")
                                runSync {
                                    if (sender is Player) {
                                        sender.sendMessage(
                                            MM.deserialize("${instance.prefix} <green>Configuration reloaded!"),
                                        )
                                    } else {
                                        instance.logger.info("Configuration reloaded!")
                                    }
                                }
                            }
                        },
                ),
            "Allows plugin specific admin commands",
            listOf("vp"),
        )
    }
}
