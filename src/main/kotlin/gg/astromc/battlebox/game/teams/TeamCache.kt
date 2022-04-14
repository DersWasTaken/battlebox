package gg.astromc.battlebox.game.teams

import gg.astromc.battlebox.game.maps.configuration.TeamConfiguration
import gg.astromc.gamelib.Team

object TeamCache {
    private val teams = hashMapOf<TeamConfiguration, Team>()

    fun insertTeam(teamConfiguration: TeamConfiguration, team: Team) {
        teams[teamConfiguration] = team
    }

    fun getTeam(teamConfiguration: TeamConfiguration): Team {
        return teams[teamConfiguration] ?:
            TeamFactory.createTeam(teamConfiguration).also { insertTeam(teamConfiguration, it) }
    }
}