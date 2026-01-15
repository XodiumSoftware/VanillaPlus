package org.xodium.vanillaplus.interfaces

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import net.kyori.adventure.key.Key
import org.xodium.vanillaplus.VanillaPlus.Companion.configData
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
import org.xodium.vanillaplus.data.ConfigData

/** Represents a contract for dialogs within the system. */
@Suppress("UnstableApiUsage")
internal interface DialogInterface {
    /**
     * Retrieves the configuration data associated with the module.
     * @return A [ConfigData] object representing the configuration for the module.
     */
    val config: ConfigData get() = configData

    /**
     * The unique typed key identifies this dialog in the registry.
     * @see TypedKey
     * @see RegistryKey.DIALOG
     */
    val key: TypedKey<Dialog>
        get() =
            TypedKey.create(
                RegistryKey.DIALOG,
                Key.key(
                    INSTANCE,
                    javaClass
                        .simpleName
                        .removeSuffix("Dialog")
                        .split(Regex("(?=[A-Z])"))
                        .filter { it.isNotEmpty() }
                        .joinToString("_") { it.lowercase() },
                ),
            )

    /**
     * Configures the properties of the dialog using the provided builder.
     * @param invoke The builder used to define the dialog properties.
     * @return The builder for method chaining.
     */
    fun invoke(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder = builder

    /**
     * Retrieves the dialog from the registry.
     * @return The [Dialog] instance corresponding to the key.
     * @throws NoSuchElementException if the dialog is not found in the registry.
     */
    fun get(): Dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).getOrThrow(key)
}
