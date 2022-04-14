package gg.astromc.battlebox.game.utils

fun <T> List<T>.betterIterator() = BetterIterator(this)

class BetterIterator<T>(private val list: List<T>) {
    private var currentElement = 0

    fun hasNext(): Boolean {
        return currentElement < list.size
    }

    fun next(): T {
        return list[++currentElement]
    }

    fun reset(): T {
        currentElement = 0
        return list[currentElement]
    }
}