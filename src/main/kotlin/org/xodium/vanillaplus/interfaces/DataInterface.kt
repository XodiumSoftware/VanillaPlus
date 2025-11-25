package org.xodium.vanillaplus.interfaces

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.File
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

/** Represents a contract for data within the system. */
internal interface DataInterface {
    companion object {
        private const val CONFIG_FILE = "config.json"
        private val features = mutableListOf<FeatureInterface>()

        fun registerFeature(feature: FeatureInterface) {
            features.add(feature)
        }
    }

    val json: Json
        get() =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }

    /** Loads configuration from JSON file. */
    fun load() {
        val config = File(instance.dataFolder, CONFIG_FILE)

        if (!config.exists()) {
            instance.dataFolder.mkdirs()
            save()
            instance.logger.info("Created new config file.")
        } else {
            try {
                val jsonObject = json.parseToJsonElement(config.readText()).jsonObject

                features.forEach { feature ->
                    val featureName = feature::class.simpleName ?: return@forEach

                    jsonObject[featureName]?.let { configElement ->
                        val configProperty = feature::class.memberProperties.find { it.name == "config" }
                        val configType = configProperty?.returnType ?: return@forEach
                        val deserializer = json.serializersModule.serializer(configType)
                        val loadedConfig = json.decodeFromJsonElement(deserializer, configElement)

                        (configProperty as? KMutableProperty<*>)?.setter?.call(feature, loadedConfig)
                    }
                }

                instance.logger.info("Loaded configs from file.")
            } catch (e: Exception) {
                instance.logger.warning("Failed to load configs: ${e.message}, using defaults")
                save()
            }
        }
    }

    /** Saves configuration to JSON file. */
    fun save() {
        try {
            val jsonMap =
                buildMap {
                    features.forEach { feature ->
                        val featureName = feature::class.simpleName ?: return@forEach
                        val configProperty =
                            feature::class.memberProperties.find { it.name == "config" } ?: return@forEach

                        configProperty.getter.call(feature)?.let { config ->
                            try {
                                val serializer =
                                    json.serializersModule.serializer(config::class.java.kotlin.createType())
                                val jsonElement = json.encodeToJsonElement(serializer, config)

                                put(featureName, jsonElement)
                            } catch (e: Exception) {
                                instance.logger.warning("Failed to serialize $featureName: ${e.message}")
                            }
                        }
                    }
                }

            val jsonObject = JsonObject(jsonMap)

            File(instance.dataFolder, CONFIG_FILE).writeText(
                json.encodeToString(JsonObject.serializer(), jsonObject),
            )
            instance.logger.info("Saved configs")
        } catch (e: Exception) {
            instance.logger.warning("Failed to save configs: ${e.message}")
            e.printStackTrace()
        }
    }
}
