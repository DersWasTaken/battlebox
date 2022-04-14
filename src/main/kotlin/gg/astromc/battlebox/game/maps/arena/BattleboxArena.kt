package gg.astromc.battlebox.game.maps.arena

import gg.astromc.battlebox.game.maps.configuration.MapConfiguration
import net.minestom.server.instance.Instance

data class BattleboxArena(
    val instance: Instance,
    val mapConfiguration: MapConfiguration
)