package org.xodium.vanillaplus

/**
 * Marks a class as a VanillaPlus module.
 * @property enabled Whether the module should be loaded.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class Module(
    val enabled: Boolean = true,
)
