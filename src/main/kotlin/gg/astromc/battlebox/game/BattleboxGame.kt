package gg.astromc.battlebox.game

import gg.astromc.battlebox.BattleboxExtension
import gg.astromc.battlebox.game.maps.arena.BattleboxArenaLoader
import gg.astromc.battlebox.game.phases.PregamePhase
import gg.astromc.battlebox.game.players.BattleboxGamePlayers
import gg.astromc.battlebox.game.players.PlayerPersistence
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.gamelib.Team
import io.github.bloepiloepi.pvp.events.PlayerExhaustEvent
import io.github.bloepiloepi.pvp.events.PlayerSpectateEvent
import io.github.bloepiloepi.pvp.events.ProjectileHitEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.adventure.audience.PacketGroupingAudience
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.item.Material
import net.minestom.server.listener.SpectateListener
import net.minestom.server.listener.manager.PacketListenerConsumer
import net.minestom.server.listener.manager.PacketListenerManager
import net.minestom.server.network.packet.client.play.ClientSpectatePacket
import world.cepi.kstom.Manager
import world.cepi.kstom.adventure.asMini
import world.cepi.kstom.event.listen
import world.cepi.kstom.event.listenOnly

// TODO: When a player re-joins the game or when they die... move to spectator? Move back when next round begins.
// TODO: I have to change the gamemodes in a couple areas... move this to a single area
// TODO: I have to change the 2nd phase `GameStartedPhase` in 2 areas... move this to a single area
// TODO: Maybe instead of using 'setSpectator' and manually setting teams to re-initialize players... there should be a method called reinitialize
class BattleboxGame(
    private val battleboxExtension: BattleboxExtension,
    private val battleboxArenaLoader: BattleboxArenaLoader
) : PacketGroupingAudience {
    private val initialPhase = PregamePhase(this, battleboxExtension.eventNode)
    var currentArena = battleboxArenaLoader.createRandomArena() ?: error("Can't load map!")

    // TODO: Move this
    val teamWins = mutableMapOf<Team, Int>()

    fun startGame() {
        initialPhase.startPhase()

        //TODO: REPLACE WITH CHAT MESSAGE THING THAT NO ONE IS DOING OR WORKING ON
        battleboxExtension.eventNode.listenOnly<PlayerChatEvent> {
            setChatFormat {
                if(BattleboxGamePlayers.get(player.uuid) != null && player.team != null) {
                    Component.text("${player.username}: ", TextColor.color(player.team.teamColor))
                        .append(Component.text(message, NamedTextColor.GRAY))
                } else {
                    "<gray>${player.username}: $message".asMini()
                }
            }
        }

        battleboxExtension.eventNode.listen<PlayerPacketEvent> {
            handler {
                if(packet::class.java == ClientSpectatePacket::class.java) {
                    isCancelled = true
                }
            }
        }

        battleboxExtension.eventNode.listen<PlayerSpectateEvent> {
            handler {
                isCancelled = true
            }
        }

        battleboxExtension.eventNode.listenOnly<PlayerExhaustEvent> {
            player.foodSaturation = 0.0f
            isCancelled = true
        }

        battleboxExtension.eventNode.listenOnly<InventoryPreClickEvent> {
            if(clickedItem.material == Material.LEATHER_BOOTS) {
                isCancelled = true
            }
        }

        battleboxExtension.eventNode.listenOnly<ProjectileHitEvent.ProjectileBlockHitEvent> {
            entity.remove()
        }

        battleboxExtension.eventNode.listenOnly<ItemDropEvent> {
            isCancelled = true
        }

        battleboxExtension.eventNode.listen<PlayerLoginEvent> {
            handler {
                setSpawningInstance(currentArena.instance)
                currentArena.mapConfiguration.let { player.respawnPoint = it.pregameSpawnPosition }
            }
        }
    }

    fun endGame() {
        // TODO: this.
    }

    fun setMap(mapName: String) {
        currentArena = (battleboxArenaLoader.createArena(mapName) ?: return)

        Manager.connection.onlinePlayers.forEach {
            it.setInstance(currentArena.instance, currentArena.mapConfiguration.pregameSpawnPosition)
        }
    }

    // TODO: improve this?
    override fun getPlayers(): MutableCollection<Player> = BattleboxGamePlayers.getAll().map { it.player }.toMutableList()
}