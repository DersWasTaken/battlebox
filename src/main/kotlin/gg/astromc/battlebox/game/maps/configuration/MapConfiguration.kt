package gg.astromc.battlebox.game.maps.configuration

import net.minestom.server.coordinate.Pos
import world.cepi.kstom.serializer.PositionSerializer
import kotlinx.serialization.Serializable

@Serializable
data class MapConfiguration(
    val name: String,
    val authors: List<String>,
    val teams: List<TeamConfiguration>,
    val kits: List<KitConfiguration>,
    val barriers: List<BarrierConfiguration>,

    @Serializable(with = PositionSerializer::class)
    val pregameSpawnPosition: Pos,

    val mapObjective: MapObjective,
    val itemSpawns: List<ItemSpawn>
)