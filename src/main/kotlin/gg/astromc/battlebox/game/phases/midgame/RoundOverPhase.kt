package gg.astromc.battlebox.game.phases.midgame

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.maps.helpers.WoolObjectiveArea
import gg.astromc.battlebox.game.phases.GameOverPhase
import gg.astromc.battlebox.game.players.BattleboxGamePlayers
import gg.astromc.battlebox.game.teams.BattleboxTeamContainer
import gg.astromc.gamelib.AbstractPhase
import io.github.bloepiloepi.pvp.events.FinalAttackEvent
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.server.sound.SoundEvent
import net.minestom.server.utils.time.TimeUnit
import world.cepi.kstom.Manager
import world.cepi.kstom.event.listenOnly
import java.time.Duration

class RoundOverPhase(
    private val winningTeam: BattleboxTeamContainer?,
    private val battleboxGame: BattleboxGame,
    parentNode: EventNode<Event>
): AbstractPhase("roundOverPhase", parentNode) {
    private val winsRequiredToWin = 3 // TODO: Configurable?

    override val nextPhase: () -> AbstractPhase? = nextPhase@{
        println(battleboxGame.teamWins)
        for ((team, wins) in battleboxGame.teamWins)
            if (wins >= winsRequiredToWin)
                return@nextPhase GameOverPhase(team, battleboxGame, parentNode)
        return@nextPhase KitSelectionPhase(battleboxGame, parentNode)
    }

    override fun startPhase() {
        //TODO: EXTRACT DUPLICATE
        eventNode.listenOnly<PlayerBlockPlaceEvent> {
            if (!WoolObjectiveArea.isPositionWithin(battleboxGame, blockPosition)) {
                isCancelled = true
                return@listenOnly
            }

            Manager.scheduler.buildTask {
                val playerContainer = BattleboxGamePlayers.get(player.uuid) ?: return@buildTask
                val teamContainer = playerContainer.battleboxPlayer.teamContainer ?: return@buildTask
                val hasWonRound = WoolObjectiveArea.hasCompletedObjective(battleboxGame.currentArena, teamContainer)

                if (hasWonRound) {
                    endPhase()
                }
            }.delay(Duration.of(1, TimeUnit.SERVER_TICK)).schedule()
        }

        eventNode.listenOnly<EntityDamageEvent> {
            isCancelled = true
        }

        if (winningTeam != null) {
            val team = winningTeam.team
            val component = Component.text(team.teamName, TextColor.color(team.color))
                .append(Component.text(" has won the round!", NamedTextColor.GRAY))

            battleboxGame.teamWins[team] = battleboxGame.teamWins.getOrDefault(team, 0) + 1

            battleboxGame.playSound(Sound.sound(
                Key.key("block.bell.resonate"),
                Sound.Source.BLOCK,
                5f,
                5f
            ))

            battleboxGame.showTitle(
                Title.title(
                    Component.empty(), component,
                    Title.Times.of(
                        Duration.ZERO,
                        Duration.of(2, TimeUnit.SECOND),
                        Duration.of(1, TimeUnit.SECOND)
                    )
                )
            )
        }

        Manager.scheduler.buildTask {
            endPhase()
        }.delay(Duration.of(5, TimeUnit.SECOND)).schedule()
    }

}