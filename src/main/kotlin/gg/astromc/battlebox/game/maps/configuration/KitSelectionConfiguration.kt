package gg.astromc.battlebox.game.maps.configuration

import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.item.Material
import net.minestom.server.utils.Rotation
import world.cepi.kstom.serializer.MaterialSerializer
import world.cepi.kstom.serializer.PositionSerializer

@Serializable
data class KitSelectionConfiguration(
    val kitName: String,

    @Serializable(with = PositionSerializer::class)
    val backgroundStart: Pos,

    @Serializable(with = PositionSerializer::class)
    val backgroundEnd: Pos,

    @Serializable(with = MaterialSerializer::class)
    val itemFrameDisplay: Material,

    @Serializable(with = PositionSerializer::class)
    val itemFrameLocation: Pos,

    @Serializable(with = PositionSerializer::class)
    val selectionButtonPos: Pos
)