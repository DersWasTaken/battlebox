package gg.astromc.battlebox.game.phases

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.teams.TeamAssigner
import gg.astromc.battlebox.game.commands.MapCommand
import gg.astromc.battlebox.game.commands.SpectateCommand
import gg.astromc.battlebox.game.phases.midgame.KitSelectionPhase
import gg.astromc.battlebox.game.players.BattleboxGamePlayers
import gg.astromc.battlebox.game.players.BattleboxPlayer
import gg.astromc.battlebox.game.players.PlayerContainer
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.battlebox.game.teams.strategies.NoTeamAssignmentStrategy
import gg.astromc.gamelib.AbstractPhase
import gg.astromc.gamelib.utils.Countdown
import io.github.bloepiloepi.pvp.events.FinalAttackEvent
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.minestom.server.entity.GameMode
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.Task
import net.minestom.server.utils.time.TimeUnit
import world.cepi.kstom.Manager
import world.cepi.kstom.event.listen
import world.cepi.kstom.event.listenOnly
import java.time.Duration

class PregamePhase(
    private val battleboxGame: BattleboxGame,
    parentNode: EventNode<Event>
): AbstractPhase("pre-game", parentNode) {
    private val minimumPlayersToStart = 2 // TODO: configurable.
    private val countdownSeconds = 15 // TODO: configurable.
    
    private var countdown: Task? = null

    override val nextPhase: () -> AbstractPhase? = {
        KitSelectionPhase(battleboxGame, parentNode)
    }

    override fun startPhase() {
        // TODO: registerCommand(ForceStartCommand(battleboxGame).command)
        registerCommand(SpectateCommand(battleboxGame).command)
        registerCommand(MapCommand(battleboxGame).command)

        eventNode.listen<PlayerSpawnEvent> {
            filters += { isFirstSpawn }

            handler {
                val playersOnline = Manager.connection.onlinePlayers.size

                BattleboxGamePlayers.insert(player.uuid,
                    PlayerContainer(
                        player = player,
                        battleboxPlayer = BattleboxPlayer(
                            identifier = player.uuid,
                            playerState = PlayerState.PLAYING
                        )
                    )
                )

                player.gameMode = GameMode.ADVENTURE

                val playerContainer = BattleboxGamePlayers.get(player.uuid) ?: return@handler
                TeamAssigner.assignPlayerTo(playerContainer, NoTeamAssignmentStrategy)

                if (playersOnline < minimumPlayersToStart) return@handler
                if (countdown?.isAlive == true) return@handler

                battleboxGame.playSound(Sound.sound(
                    Key.key("entity.experience_orb.pickup"),
                    Sound.Source.BLOCK,
                    5f,
                    5f
                ))

                battleboxGame.showTitle(
                    Title.title(
                        Component.empty(), Component.text("Game starting in ", NamedTextColor.GRAY)
                            .append(Component.text( 15, NamedTextColor.RED, TextDecoration.BOLD))
                            .append(Component.text(" seconds..", NamedTextColor.GRAY)),
                        Title.Times.of(
                            Duration.ZERO,
                            Duration.of(2, TimeUnit.SECOND),
                            Duration.ZERO
                        )
                    )
                )

                eventNode.listenOnly<EntityDamageEvent> {
                    isCancelled = true
                }

                countdown = Countdown.create(
                    // TODO: Really stupid fix, find out why last second doesn't last very long
                    countdownSeconds = countdownSeconds+1,
                    tickRate = Duration.of(1, TimeUnit.SECOND),
                    countdownFunction = { currentCountdownNum ->
                        val component = Component.text("Game starting in ", NamedTextColor.GRAY)
                            .append(Component.text(currentCountdownNum - 1, NamedTextColor.RED, TextDecoration.BOLD))
                            .append(Component.text(" seconds..", NamedTextColor.GRAY))

                        if (currentCountdownNum-1 in 1..5) {
                            battleboxGame.showTitle(
                                Title.title(
                                    Component.empty(), component,
                                    Title.Times.of(
                                        Duration.ZERO,
                                        Duration.of(2, TimeUnit.SECOND),
                                        Duration.ZERO
                                    )
                                )
                            )

                            battleboxGame.playSound(Sound.sound(
                                Key.key("entity.experience_orb.pickup"),
                                Sound.Source.BLOCK,
                                5f,
                                5f
                            ))
                        }
                    },
                    cancelCondition = {
                        BattleboxGamePlayers.getAll().count { it.battleboxPlayer.teamContainer == null } == 1 ||
                                Manager.connection.onlinePlayers.size < minimumPlayersToStart
                    },
                    onFinish = {
                        battleboxGame.showTitle(
                            Title.title(
                                Component.empty(),
                                Component.text("Game starting now", NamedTextColor.RED),
                                Title.Times.of(
                                    Duration.ZERO,
                                    Duration.of(1, TimeUnit.SECOND),
                                    Duration.of(1, TimeUnit.SECOND)
                                )
                            )
                        )

                        battleboxGame.playSound(Sound.sound(
                            Key.key("entity.experience_orb.pickup"),
                            Sound.Source.BLOCK,
                            5f,
                            5f
                        ))

                        val players = BattleboxGamePlayers.getAll().filter { it.battleboxPlayer.playerState == PlayerState.PLAYING }
                        val teams = battleboxGame.currentArena.mapConfiguration.teams
                        TeamAssigner.balanceTeams(teams, players)

                        endPhase()
                    }
                )
            }
        }
    }
}