package gg.astromc.battlebox.game.maps.configuration

import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Pos
import net.minestom.server.item.Material
import world.cepi.kstom.serializer.MaterialSerializer
import world.cepi.kstom.serializer.PositionSerializer

@Serializable
data class MapObjective(
    @Serializable(with = PositionSerializer::class)
    val objectivePosStart: Pos,
    @Serializable(with = PositionSerializer::class)
    val objectivePosEnd: Pos,

    @Serializable(with = MaterialSerializer::class)
    val objectiveMaterial: Material,

    val surroundingAreaMaterials: List<@Serializable(with = MaterialSerializer::class) Material>
)