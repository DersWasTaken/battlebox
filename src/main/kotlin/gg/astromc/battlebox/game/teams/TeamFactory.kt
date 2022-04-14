package gg.astromc.battlebox.game.teams

import gg.astromc.battlebox.game.maps.configuration.TeamConfiguration
import gg.astromc.gamelib.Team
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.network.packet.server.play.TeamsPacket
import world.cepi.kstom.adventure.asMini

object TeamFactory {
    fun createTeam(teamConfiguration: TeamConfiguration): Team {
        val team = Team(
            teamName = teamConfiguration.name,
            color = teamConfiguration.teamColor.color,
            collisionRule = TeamsPacket.CollisionRule.NEVER,
            nameTagVisibility = TeamsPacket.NameTagVisibility.ALWAYS,
            friendlyFire = false,
            canSeeInvisiblePlayers = false,
        )

        val scoreboardTeam = team.scoreboardTeam

        scoreboardTeam.prefix = Component.text(teamConfiguration.name.toUpperCase(), TextColor.color(teamConfiguration.teamColor.color))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))

        return team
    }
}