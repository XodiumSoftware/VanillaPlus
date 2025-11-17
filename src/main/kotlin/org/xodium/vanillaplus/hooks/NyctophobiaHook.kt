package org.xodium.vanillaplus.hooks

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import org.xodium.vanillaplus.utils.ExtUtils.mm
import java.net.URI

/** A utility object for checking datapack availability and handling related dependencies. */
object NyctophobiaHook {
    val PACK_INFO =
        ResourcePackInfo
            .resourcePackInfo()
            .uri(
                URI.create(
                    "https://cdn.modrinth.com/data/Q2HFmuJV/versions/xe548JsZ/Nyctophobia%20Resourcepack%20V1.5%20%5BOPTIFINE%5D.zip",
                ),
            ).build()

    /**
     * Sends the Nyctophobia resource pack to the specified audience.
     * @param target The audience to send the resource pack to.
     */
    fun sendResourcePack(target: Audience) {
        target.sendResourcePacks(
            ResourcePackRequest
                .resourcePackRequest()
                .packs(PACK_INFO)
                .prompt("This Resource Pack is required to play on this server.".mm())
                .required(true)
                .build(),
        )
    }
}
