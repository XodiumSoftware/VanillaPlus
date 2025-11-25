package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.features.BooksFeature
import org.xodium.vanillaplus.features.CauldronFeature

@Serializable
internal data class CentralConfigData(
    val booksFeature: BooksFeature.Config = BooksFeature.config,
    val cauldronFeature: CauldronFeature.Config = CauldronFeature.config,
)
