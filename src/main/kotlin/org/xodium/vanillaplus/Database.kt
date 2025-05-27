/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.jetbrains.exposed.sql.Database as ExposedDatabase

/** Handles database connection. */
object Database {
    private val databaseFile = instance.dataFolder.resolve("vanillaplus.db")
    private const val DRIVER = "org.sqlite.JDBC"
    private val connUrl = "jdbc:sqlite:${databaseFile.absolutePath}"

    init {
        databaseFile.parentFile.apply { if (!exists()) mkdirs() }
        ExposedDatabase.connect(connUrl, driver = DRIVER)
        instance.logger.info("Opened: Exposed Database Connection")
    }
}