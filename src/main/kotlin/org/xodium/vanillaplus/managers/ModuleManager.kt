package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.jar.JarFile

/** Manages modules. */
internal object ModuleManager {
    private val MODULE_PACKAGE: String
        get() = instance::class.java.packageName + ".modules"

    private val modules = mutableListOf<ModuleInterface>()

    /** Discover and register modules in the modules package. */
    fun load() {
        val jarFile =
            runCatching {
                val path =
                    instance.javaClass.protectionDomain.codeSource.location
                        .toURI()
                        .path
                JarFile(path)
            }.getOrNull() ?: return

        jarFile
            .entries()
            .asSequence()
            .filter { it.name.startsWith(MODULE_PACKAGE.replace('.', '/')) && it.name.endsWith(".class") }
            .map { it.name.removeSuffix(".class").replace('/', '.') }
            .forEach { className ->
                val clazz =
                    runCatching { instance.javaClass.classLoader.loadClass(className) }.getOrNull() ?: return@forEach
                val annotation = clazz.getAnnotation(org.xodium.vanillaplus.Module::class.java) ?: return@forEach

                if (!annotation.enabled) return@forEach

                val moduleInstance =
                    clazz.kotlin.objectInstance
                        ?: runCatching { clazz.getDeclaredConstructor().newInstance() }.getOrNull()

                if (moduleInstance is ModuleInterface) modules += moduleInstance
            }

        val enabledModules = modules.filter { it.isEnabled }
        val time = enabledModules.sumOf { it.register() }

        instance.logger.info("Registered: ${enabledModules.size} module(s) | Took ${time}ms")
    }
}
