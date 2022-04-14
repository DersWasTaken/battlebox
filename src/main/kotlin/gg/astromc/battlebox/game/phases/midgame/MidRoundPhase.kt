package gg.astromc.battlebox.game.phases.midgame

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.maps.helpers.PreRoundEntranceBarrier
import gg.astromc.battlebox.game.maps.helpers.WoolObjectiveArea
import gg.astromc.battlebox.game.players.BattleboxGamePlayers
import gg.astromc.battlebox.game.players.PlayerPersistence
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.battlebox.game.teams.TeamCache
import gg.astromc.battlebox.game.teams.strategies.healthSidebar
import gg.astromc.battlebox.game.teams.strategies.playerKills
import gg.astromc.gamelib.AbstractPhase
import gg.astromc.gamelib.utils.BossbarTimer
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage
import io.github.bloepiloepi.pvp.damage.CustomIndirectEntityDamage
import io.github.bloepiloepi.pvp.events.EntityKnockbackEvent
import io.github.bloepiloepi.pvp.projectile.Arrow
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import net.minestom.server.color.Color
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerDeathEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.network.packet.server.play.PlayerInfoPacket
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.RemovePlayer
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.Task
import net.minestom.server.utils.time.TimeUnit
import world.cepi.kstom.Manager
import world.cepi.kstom.adventure.asMini
import world.cepi.kstom.event.listen
import world.cepi.kstom.event.listenOnly
import java.time.Duration
import java.time.Instant

// TODO: If current wins > X then go to end game phase.
// TODO: Actually end the phase when they win.
class MidRoundPhase(
    private val battleboxGame: BattleboxGame,
    parentNode: EventNode<Event>
): AbstractPhase("midRoundPhase", parentNode) {
    private var bossBarTask: Task? = null
    private var bossBar = BossBar.bossBar(Component.text(), 0f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_10)

    private var gameEndsInstant: Instant? = null

    override val nextPhase: () -> AbstractPhase? = {
        val winner = battleboxGame.players
            .mapNotNull { BattleboxGamePlayers.get(it.uuid)?.battleboxPlayer?.teamContainer }
            .firstOrNull { WoolObjectiveArea.hasCompletedObjective(battleboxGame.currentArena, it) }
        println("And the winner is: $winner")
        RoundOverPhase(winner, battleboxGame, parentNode)
    }

    override fun startPhase() {
        PreRoundEntranceBarrier.removeBarriers(battleboxGame.currentArena)
        gameEndsInstant = Instant.now().plus(5, TimeUnit.MINUTE)

        eventNode.listenOnly<EntityKnockbackEvent> {
            strength = 0.75f
        }

        eventNode.listen<PlayerSpawnEvent> {
            handler {
                PlayerPersistence.persist(player) {
                    if (it.battleboxPlayer.playerState == PlayerState.PLAYING) {
                        it.player.isInvisible = true
                        it.player.gameMode = GameMode.SPECTATOR
                    }
                }
            }
        }

        eventNode.listenOnly<EntityDamageEvent> {
            val player = entity as Player

            val health = player.health.toDouble() - damage.toInt()

            val component = Component.text("${player.username} ", NamedTextColor.GRAY)
                .append(
                    Component.text("${String.format("%.1f", health)}❤", NamedTextColor.RED)
                )

            val source = getSource(this) ?: return@listenOnly

            val attacked = BattleboxGamePlayers.get(player.uuid) ?: return@listenOnly
            val attacker = BattleboxGamePlayers.get(source.uuid) ?: return@listenOnly

            if (attacker.battleboxPlayer.teamContainer?.team == attacked.battleboxPlayer.teamContainer?.team) {
                isCancelled = true
            } else {
                source.sendActionBar(component)
            }

            healthSidebar.updateScore(player, health.toInt())
        }

        eventNode.listenOnly<PlayerDeathEvent> {
            val lastDamageSource = player.lastDamageSource ?: return@listenOnly
            val entityDamage = lastDamageSource as? CustomEntityDamage ?: return@listenOnly
            val playerDamager = entityDamage.entity as? Player ?: return@listenOnly

            val id = entityDamage.identifier

            val killedPlayerTeamColor = getColorForPlayer(player)
            val damagerTeamColor = getColorForPlayer(playerDamager)

            playerKills[playerDamager] = playerKills[playerDamager]?.plus(1) ?: return@listenOnly

            val playerContainer = BattleboxGamePlayers.get(playerDamager.uuid) ?: return@listenOnly
            val teamConfiguration = playerContainer.battleboxPlayer.teamContainer?.teamConfiguration ?: return@listenOnly

            val team = TeamCache.getTeam(teamConfiguration)

            playerDamager.displayName = Component.text(team.teamName.toUpperCase(), TextColor.color(team.color))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(playerDamager.username, TextColor.color(team.color)))
                .append("<dark_gray> |<yellow> Kills: ${playerKills[playerDamager]!!}".asMini())

            if(id == "player") {
                chatMessage = Component.text(playerDamager.username, damagerTeamColor)
                    .append(Component.text(" ⚔ ", NamedTextColor.GOLD))
                    .append(Component.text(player.username, killedPlayerTeamColor))
            } else if(id == "arrow") {
                chatMessage = Component.text(playerDamager.username, damagerTeamColor)
                    .append(Component.text(" 🏹 ", NamedTextColor.GOLD))
                    .append(Component.text(player.username, killedPlayerTeamColor))
            }

            battleboxGame.sendActionBar(chatMessage as TextComponent)

            val soundEffectPacket = SoundEffectPacket(
                SoundEvent.BLOCK_ANVIL_LAND,
                Sound.Source.MASTER,
                player.position,
                1000f,
                5f
            )

            playerDamager.sendPacket(soundEffectPacket)
            player.sendPacket(soundEffectPacket)

            player.isEnableRespawnScreen = false

            player.gameMode = GameMode.SPECTATOR
            player.isInvisible = true

            player.showTitle(
                Title.title(
                    Component.empty(), "<red>You Died!".asMini(),
                    Title.Times.of(
                        Duration.ZERO,
                        Duration.of(2, TimeUnit.SECOND),
                        Duration.ZERO
                    )
                )
            )

            player.inventory.clear()

            //player.hidePlayer()

        }

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
    }

    private fun updateBossbar() {
        // TODO: this.
        bossBarTask?.cancel()

        // TODO: Duration should be renamed to tickSpeed
        bossBarTask = BossbarTimer.start(
            bossBar = bossBar,
            duration = Duration.of(1, TimeUnit.SECOND),
            totalAmount = Duration.between(Instant.now(), gameEndsInstant),
            onFinish = {
                // TODO: this.
            },
            cancelCondition = { false }
        )
    }

    private fun getColorForPlayer(player: Player): TextColor? {
        val playerContainer = BattleboxGamePlayers.get(player.uuid) ?: return null
        val teamContainer = playerContainer.battleboxPlayer.teamContainer ?: return null
        return TextColor.color(teamContainer.teamConfiguration?.teamColor?.color ?: Color(255, 255, 255))
    }

    private fun getSource(event: EntityDamageEvent) : Player? {
        if (event.damageType.identifier == "player") {
            return ((event.damageType as CustomEntityDamage).directEntity as Player)
        }
        if (event.damageType.identifier == "arrow") {
            return ((event.damageType as CustomIndirectEntityDamage).directEntity as Arrow).shooter as Player
        }
        return null
    }

}