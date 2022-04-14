package gg.astromc.battlebox.game.maps.configuration

import kotlinx.serialization.Serializable
import net.minestom.server.item.ItemStack
import world.cepi.kstom.serializer.ItemStackSerializer

@Serializable
data class KitConfiguration(
    val name: String,
    val contents: List<@Serializable(with = ItemStackSerializer::class) ItemStack>,
)