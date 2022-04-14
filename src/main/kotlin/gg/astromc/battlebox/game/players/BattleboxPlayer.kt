package gg.astromc.battlebox.game.players

import gg.astromc.battlebox.game.teams.BattleboxTeamContainer
import java.util.*

data class BattleboxPlayer(
    val identifier: UUID,
    var kills: Int = 0,
    var deaths: Int = 0,
    var currentKit: String? = null,
    var teamContainer: BattleboxTeamContainer? = null,
    var playerState: PlayerState = PlayerState.PLAYING
)