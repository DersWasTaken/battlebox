package gg.astromc.battlebox.game.maps.configuration

import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Pos
import net.minestom.server.item.ItemStack
import world.cepi.kstom.serializer.ItemStackSerializer
import world.cepi.kstom.serializer.PositionSerializer

@Serializable
data class ItemSpawn(
    @Serializable(with = ItemStackSerializer::class)
    val itemStack: ItemStack,

    @Serializable(with = PositionSerializer::class)
    val position: Pos
)