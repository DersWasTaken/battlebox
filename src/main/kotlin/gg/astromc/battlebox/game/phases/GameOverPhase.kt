package gg.astromc.battlebox.game.phases

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.gamelib.AbstractPhase
import gg.astromc.gamelib.Team
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
import net.minestom.server.utils.time.TimeUnit
import world.cepi.kstom.event.listenOnly
import java.time.Duration

class GameOverPhase(
    private val winningTeam: Team,
    private val battleboxGame: BattleboxGame,
    parentNode: EventNode<Event>
): AbstractPhase("gameover", parentNode) {
    override fun startPhase() {
        val component = Component.text( winningTeam.teamName, TextColor.color(winningTeam.color))
            .append(Component.text(" has won the game!", NamedTextColor.GRAY))
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

        eventNode.listenOnly<EntityDamageEvent> {
            isCancelled = true
        }

        battleboxGame.playSound(
            Sound.sound(
            Key.key("entity.ender_dragon.growl"),
            Sound.Source.HOSTILE,
            5f,
            5f
        ))
    }
}