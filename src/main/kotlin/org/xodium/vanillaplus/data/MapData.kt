/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/**
 * Represents the data for a map, including its URL and dimensions.
 * @property url The URL of the map image.
 * @property x The x-coordinate of the map's position.
 * @property y The y-coordinate of the map's position.
 * @property scaleX The scale factor in the x direction.
 * @property scaleY The scale factor in the y direction.
 */
data class MapData(
    val url: String?,
    val x: Int,
    val y: Int,
    val scaleX: Int,
    val scaleY: Int
)