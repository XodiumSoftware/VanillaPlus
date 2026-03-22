@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import io.papermc.paper.registry.TypedKey
import kotlinx.serialization.serializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.delegates.ModuleConfigDelegate
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*

/** General utilities. */
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [VanillaPlus] messages. */
    val VanillaPlus.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /** Extension function to convert snake_case to Proper Case with spaces. */
    fun String.snakeToProperCase(): String =
        split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    /** Extension function specifically for enchantment keys */
    fun TypedKey<Enchantment>.displayName(): Component = MM.deserialize(value().snakeToProperCase())

    /** Extension function to convert CamelCase to snake_case, removing a specified suffix. */
    inline fun <reified T> Class<*>.toRegistryKeyFragment(): String =
        simpleName
            .removeSuffix(T::class.simpleName ?: "")
            .split(Regex("(?=[A-Z])"))
            .filter { it.isNotEmpty() }
            .joinToString("_") { it.lowercase() }

    /** Extension function to convert a UUID to a 4-element integer array representation. */
    fun UUID.toIntArray(): IntArray {
        val most = mostSignificantBits
        val least = leastSignificantBits
        return intArrayOf((most shr 32).toInt(), most.toInt(), (least shr 32).toInt(), least.toInt())
    }

    /**
     * Returns the i18n string matching the current weather state of this world.
     * @param thundering The string to return when it is thundering.
     * @param storm The string to return when there is a storm.
     * @param clear The string to return when the weather is clear.
     */
    fun World.weather(
        thundering: String,
        storm: String,
        clear: String,
    ): String =
        when {
            isThundering -> thundering
            hasStorm() -> storm
            else -> clear
        }

    /**
     * Creates a [ModuleConfigDelegate] for this [ModuleInterface], using the implementing class's
     * simple name as the JSON key and [default] as the fallback config factory.
     * @param C The module config type, which must implement [ModuleConfigInterface].
     * @param default A factory that produces the default [C] instance.
     * @return A [ModuleConfigDelegate] that autoloads and caches the module's config section.
     */
    internal inline fun <reified C : ModuleConfigInterface> ModuleInterface.configDelegate(
        noinline default: () -> C,
    ): ModuleConfigDelegate<C> = ModuleConfigDelegate(this::class.simpleName!!, serializer<C>(), default)
}
