package gg.astromc.battlebox.game.maps.configuration

import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Pos
import net.minestom.server.item.Material
import world.cepi.kstom.serializer.MaterialSerializer
import world.cepi.kstom.serializer.PositionSerializer

@Serializable
data class BarrierConfiguration(
    @Serializable(with = PositionSerializer::class)
    val barrierStart: Pos,

    @Serializable(with = PositionSerializer::class)
    val barrierEnd: Pos,

    @Serializable(with = MaterialSerializer::class)
    val barrierMaterial: Material,
)