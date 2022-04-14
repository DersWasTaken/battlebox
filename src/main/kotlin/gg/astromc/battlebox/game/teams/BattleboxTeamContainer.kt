package gg.astromc.battlebox.game.teams

import gg.astromc.battlebox.game.maps.configuration.TeamConfiguration
import gg.astromc.gamelib.Team

data class BattleboxTeamContainer(
    val team: Team,

    // TODO: I only have to do this because of spectators... need to fix!
    val teamConfiguration: TeamConfiguration?
)