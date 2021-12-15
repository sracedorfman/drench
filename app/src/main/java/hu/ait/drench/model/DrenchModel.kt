package hu.ait.drench.model

import kotlin.random.Random

object DrenchModel {

    var size = 14
    var gameOver = true
    var gameWon = false

    const val STARTING_MOVES_INIT = 30
    var startingMoves = STARTING_MOVES_INIT
    var movesLeft = startingMoves

    private var boxMatrix = Array(size) {Array(size) {Box(Random.nextInt(6))} }
    private var recursionMatrix = Array(size) {Array(size) {false} }
    init {
        setupBoard()
    }

    private fun setupBoard() {
        for (i in 1 until size) {
            for (j in 1 until size) {
                boxMatrix[i][j].color = boxMatrix[i-getProbability()][j-getProbability()].color
            }
        }
        gameOver = false
        gameWon = false
        colorPicked(boxMatrix[0][0].color)
    }

    private fun getProbability() : Int {
        val rand = Random.nextFloat()
        return if (rand > (1.1f*(startingMoves - 6))/100f) 0 else 1
    }

    fun resetGame() {
        movesLeft = startingMoves
        boxMatrix = Array(size) {Array(size) {Box(Random.nextInt(6))} }
        recursionMatrix = Array(size) {Array(size) {false} }
        setupBoard()
    }

    fun getBox(i: Int, j: Int): Box {
        return boxMatrix[i][j]
    }

    fun colorPicked(color: Int) {
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (recursionMatrix[i][j])
                    boxMatrix[i][j].color = color
            }
        }
        recursionMatrix = Array(size) {Array(size) {false} }
        checkSurroundings(0,0)
    }

    private fun checkSurroundings(r: Int, c: Int) {
        if (!recursionMatrix[r][c]) {
            recursionMatrix[r][c] = true
            for (i in r - 1..r + 1) {
                if ((i != r) && (i in 0 until size)) {
                    if (boxMatrix[i][c].color == boxMatrix[r][c].color) {
                        checkSurroundings(i, c)
                    }
                }
            }
            for (j in c - 1..c + 1) {
                if ((j != c) && (j in 0 until size)) {
                    if (boxMatrix[r][j].color == boxMatrix[r][c].color) {
                        checkSurroundings(r, j)
                    }
                }
            }
        }
    }

    fun checkForWin(): Boolean {
        recursionMatrix.forEach { arr ->
            arr.forEach {
                if (!it)
                    return false
            }
        }
        gameOver = true
        return true
    }
}