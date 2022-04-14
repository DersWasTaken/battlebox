package gg.astromc.battlebox.game.phases.midgame

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.kits.KitGiver
import gg.astromc.battlebox.game.kits.KitSelector
import gg.astromc.battlebox.game.maps.helpers.PreRoundEntranceBarrier
import gg.astromc.battlebox.game.maps.helpers.WoolObjectiveArea
import gg.astromc.battlebox.game.players.*
import gg.astromc.battlebox.game.teams.strategies.healthSidebar
import gg.astromc.gamelib.AbstractPhase
import gg.astromc.gamelib.utils.Countdown
import io.github.bloepiloepi.pvp.events.FinalAttackEvent
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.timer.Task
import net.minestom.server.utils.time.TimeUnit
import world.cepi.kstom.Manager
import world.cepi.kstom.event.listen
import world.cepi.kstom.event.listenOnly
import java.time.Duration

class KitSelectionPhase(
    private val battleboxGame: BattleboxGame,
    parentNode: EventNode<Event>
) : AbstractPhase("kit-selection", parentNode) {
    private val kitSelector = KitSelector(battleboxGame.currentArena)
    private var countdownTask: Task? = null

    override val nextPhase: () -> AbstractPhase? = {
        MidRoundPhase(battleboxGame, parentNode)
    }

    // TODO: Refactor repeated code.
    override fun startPhase() {
        PreRoundEntranceBarrier.placeBarriers(battleboxGame.currentArena)

        WoolObjectiveArea.setObjectiveTo(
            battleboxGame.currentArena,
            WoolObjectiveArea.getObjectiveMaterialFromConfig(battleboxGame.currentArena.mapConfiguration)
        )

        eventNode.listenOnly<FinalAttackEvent> {
            isCancelled = true
        }

        kitSelector.initSelection()

        val playingPlayers = getPlayingPlayers()

        playingPlayers.forEach {
            it.battleboxPlayer.currentKit = null
            it.player.reset()

            val teamContainer = it.battleboxPlayer.teamContainer ?: return@forEach
            val teamConfiguration = teamContainer.teamConfiguration ?: return@forEach
            it.player.teleport(teamConfiguration.kitSelectionPosition)
        }

        countdownTask = Countdown.create(
            countdownSeconds = 20,
            tickRate = Duration.of(1, TimeUnit.SECOND),
            countdownFunction = { countdownIteration ->
                val component = Component.text("Round starting in ", NamedTextColor.GRAY)
                    .append(Component.text(countdownIteration - 1, NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text(" seconds..", NamedTextColor.GRAY))
                battleboxGame.sendActionBar(component)

                if (countdownIteration-1 in 1..5) {
                    battleboxGame.playSound(
                        Sound.sound(
                            Key.key("entity.experience_orb.pickup"),
                            Sound.Source.BLOCK,
                            5f,
                            5f
                        ))
                }
            },
            cancelCondition = { false },
            onFinish = {
                battleboxGame.sendActionBar(Component.empty())
                endThisPhase()
            }
        )

        Manager.connection.onlinePlayers.forEach {
            healthSidebar.updateScore(it, 20)
        }

        eventNode.listen<PlayerBlockInteractEvent> {
            filters += { block.namespace() == Block.STONE_BUTTON.namespace() }
            handler {
                val playerContainer = BattleboxGamePlayers.get(player.uuid) ?: return@handler  //battleboxGame.getPlayer(player.uuid) ?: return@handler
                val battleboxPlayer = playerContainer.battleboxPlayer

                if (battleboxPlayer.playerState != PlayerState.PLAYING) return@handler

                val kitConfiguration = kitSelector.handleKitButtonPress(battleboxGame, instance, Pos.fromPoint(blockPosition), playerContainer) ?: return@handler
                battleboxPlayer.currentKit = kitConfiguration.name

                val preRoundWaitingRoomPosition = battleboxPlayer.teamContainer?.teamConfiguration?.preRoundWaitingRoomPosition
                preRoundWaitingRoomPosition?.let { player.teleport(it) }
            }
        }

        eventNode.listenOnly<PlayerDisconnectEvent> {
            // TODO: Free up kit they had selected
        }

        eventNode.listen<PlayerSpawnEvent> {
            handler {
                PlayerPersistence.persist(player) { playerContainer ->
                    val player = playerContainer.player
                    val battleboxPlayer = playerContainer.battleboxPlayer

                    if (battleboxPlayer.playerState == PlayerState.PLAYING) {
                        player.gameMode = GameMode.ADVENTURE
                        player.team = battleboxPlayer.teamContainer?.team?.scoreboardTeam

                        val currentKit = battleboxPlayer.currentKit

                        if (currentKit == null) {
                            val kitSelectionPosition = battleboxPlayer.teamContainer?.teamConfiguration?.kitSelectionPosition
                            kitSelectionPosition?.let { player.teleport(it) }
                        } else {
                            val preRoundWaitingRoomPosition = battleboxPlayer.teamContainer?.teamConfiguration?.preRoundWaitingRoomPosition
                            preRoundWaitingRoomPosition?.let { player.teleport(it) }

                            val firstKit = battleboxGame.currentArena
                                .mapConfiguration
                                .kits
                                .firstOrNull { it.name == currentKit }

                            firstKit?.let { KitGiver.giveKitTo(battleboxGame, playerContainer, it) }
                        }
                    }
                }
            }
        }
    }

    private fun endThisPhase() {
        endPhase {
            getPlayingPlayers()
                .filter { it.battleboxPlayer.currentKit == null }
                .forEach { (player, battleboxPlayer) ->
                    val kitSelectionPosition = battleboxPlayer.teamContainer?.teamConfiguration?.preRoundWaitingRoomPosition
                    kitSelectionPosition?.let { player.teleport(it) }
                }
            
            kitSelector.selectRandomKits(battleboxGame)
            PreRoundEntranceBarrier.removeBarriers(battleboxGame.currentArena)
            countdownTask?.cancel()
        }
    }

    private fun getPlayingPlayers(): List<PlayerContainer> {
        return BattleboxGamePlayers.getAll().filter { it.battleboxPlayer.playerState == PlayerState.PLAYING }
    }
}