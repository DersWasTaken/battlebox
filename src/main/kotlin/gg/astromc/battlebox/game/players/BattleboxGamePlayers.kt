package gg.astromc.battlebox.game.players

import java.util.*

object BattleboxGamePlayers {
    private val players = hashMapOf<UUID, PlayerContainer>()

    fun insert(uuid: UUID, playerContainer: PlayerContainer) {
        players[uuid] = playerContainer
    }

    fun get(uuid: UUID): PlayerContainer? {
        return players[uuid]
    }

    fun getAll(): List<PlayerContainer> {
        return players.values.toList()
    }

    fun filter(predicate: (PlayerContainer) -> Boolean): List<PlayerContainer> {
        return players.values.filter(predicate)
    }
}