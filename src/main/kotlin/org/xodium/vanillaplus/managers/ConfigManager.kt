@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.managers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.DataInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.modules.*
import org.xodium.vanillaplus.utils.ExtUtils.key
import kotlin.io.path.readText

/** Manages module configs on disk and in-memory. */
internal object ConfigManager : DataInterface<String, ModuleInterface.Config> {
    override val cache: MutableMap<String, ModuleInterface.Config> = mutableMapOf()
    override val keySerializer: KSerializer<String> = String.serializer()
    override val valueSerializer: KSerializer<ModuleInterface.Config> =
        PolymorphicSerializer(ModuleInterface.Config::class)
    override val fileName: String = "config.json"
    override val serializersModule: SerializersModule =
        SerializersModule {
            polymorphic(ModuleInterface.Config::class) {
                subclass(BooksModule.Config::class)
                subclass(CauldronModule.Config::class)
                subclass(ChatModule.Config::class)
                subclass(DimensionsModule.Config::class)
                subclass(EntityModule.Config::class)
                subclass(InvModule.Config::class)
                subclass(LocatorModule.Config::class)
                subclass(MotdModule.Config::class)
                subclass(OpenableModule.Config::class)
                subclass(PetModule.Config::class)
                subclass(PlayerModule.Config::class)
                subclass(RecipiesModule.Config::class)
                subclass(ScoreBoardModule.Config::class)
                subclass(SignModule.Config::class)
                subclass(SilkTouchModule.Config::class)
                subclass(SitModule.Config::class)
                subclass(TabListModule.Config::class)
                subclass(TreesModule.Config::class)
            }
        }

    /**
     * Updates module configurations by merging existing file values into
     * in-code defaults. File/user values take precedence over defaults.
     * @param modules list of modules to update.
     */
    fun update(modules: List<ModuleInterface<ModuleInterface.Config>>) {
        modules.forEach { module ->
            val key = module.key
            val fileConfig = readFileConfig(key, module)
            val mergedConfig = fileConfig ?: module.config
            set(key, mergedConfig)
        }
        if (modules.isNotEmpty()) instance.logger.info("Config updated successfully")
    }

    /**
     * Reads a module's configuration from the JSON config file.
     * @param key The unique identifier for the module used in the config file.
     * @param module The module instance used to determine the correct configuration type.
     * @return The parsed configuration object from the file, or `null` if not found, or an error occurred.
     * @see ConfigManager.update
     */
    private fun readFileConfig(
        key: String,
        module: ModuleInterface<ModuleInterface.Config>,
    ): ModuleInterface.Config? =
        try {
            if (filePath.toFile().exists()) {
                val jsonTree = json.parseToJsonElement(filePath.readText()) as? JsonObject
                jsonTree?.get(key)?.let { element ->
                    json.decodeFromJsonElement(serializer(module.config::class.java), element) as ModuleInterface.Config
                }
            } else {
                null
            }
        } catch (e: Exception) {
            instance.logger.warning("Failed to read config section for $key: ${e.message}")
            null
        }
}
