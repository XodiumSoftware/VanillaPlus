package org.xodium.vanillaplus.delegates

import kotlinx.serialization.KSerializer
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.managers.ConfigManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A [ReadOnlyProperty] delegate that caches a module's decoded config and refreshes it on reload.
 * @param C The module config type, which must implement [ModuleConfigInterface].
 * @property key The top-level JSON key used to identify this module's config section.
 * @property serializer The [KSerializer] for [C].
 * @property default A factory that produces the default [C] instance.
 */
internal class ModuleConfigDelegate<C : ModuleConfigInterface>(
    private val key: String,
    private val serializer: KSerializer<C>,
    private val default: () -> C,
) : ReadOnlyProperty<Any?, C> {
    @Volatile
    private var cached: C = ConfigManager.decodeWith(key, serializer, default())

    init {
        ConfigManager.onReload { cached = ConfigManager.decodeWith(key, serializer, default()) }
    }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): C = cached
}
