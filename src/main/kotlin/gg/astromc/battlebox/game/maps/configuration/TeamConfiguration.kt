package gg.astromc.battlebox.game.maps.configuration

import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Pos
import world.cepi.kstom.serializer.PositionSerializer

@Serializable
data class TeamConfiguration(
    val name: String,
    val teamColor: TeamColor,

    @Serializable(with = PositionSerializer::class)
    val kitSelectionPosition: Pos,

    @Serializable(with = PositionSerializer::class)
    val preRoundWaitingRoomPosition: Pos,

    val kitSelectionList: List<KitSelectionConfiguration>
)