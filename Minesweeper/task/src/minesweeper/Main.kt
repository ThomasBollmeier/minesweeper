package minesweeper

import kotlin.random.Random
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    val numRows = 9
    val numCols = 9
    val numMines = readNumMines(scanner)
    val field = Field(FieldSize(numRows, numCols), numMines)

    printField(field)

    // Game loop
    while (!field.gameOver()) {
        val action = readUserAction(scanner)
        when (action.command) {
            Command.MINE -> {
                if (!field.isExplored(action.row, action.col)) {
                    field.toggleMark(action.row, action.col)
                }
            }
            Command.FREE -> {
                if (!field.isExplored(action.row, action.col)) {
                    field.explore(action.row, action.col)
                }
            }
        }
        printField(field)
    }

    if (field.steppedOnMine) {
        println("You stepped on a mine and failed!")
    } else {
        println("Congratulations! You found all the mines!")
    }

}


fun readNumMines(scanner: Scanner): Int {
    print("How many mines do you want on the field? >")
    return scanner.nextInt()
}

fun readUserAction(scanner: Scanner): UserAction {
    print("Set/unset mine marks or claim a cell as free: >")
    val col = scanner.nextInt() - 1
    val row = scanner.nextInt() - 1
    val command = when (scanner.next()) {
        "mine" -> Command.MINE
        "free" -> Command.FREE
        else -> throw Error("Unknown command")
    }
    return UserAction(row, col, command)
}

fun printField(field: Field) {
    println()
    print(" |")
    for (col in 1..field.size.numCols) {
        print("$col")
    }
    println("|")
    print("-|")
    for (col in 1..field.size.numCols) {
        print("-")
    }
    println("|")
    for (row in 0 until field.size.numRows) {
        print("${row + 1}|")
        for (col in 0 until field.size.numCols) {
            print(if (!field.isExplored(row, col)) {
                if (!field.steppedOnMine) {
                    if (field.isMarked(row, col)) "*" else "."
                } else {
                    if (field.hasMine(row, col)) "X" else "."
                }
            } else if (field.hasMine(row, col)) {
                "X"
            } else {
                val cnt = field.numNeighboringMines(row, col)
                if (cnt > 0) {
                    "$cnt"
                } else {
                    "/"
                }
            })
        }
        println("|")
    }
    print("-|")
    for (col in 1..field.size.numCols) {
        print("-")
    }
    println("|")
}

enum class Command {
    MINE,
    FREE
}

data class UserAction(val row: Int, val col: Int, val command: Command)

data class FieldSize(val numRows: Int, val numCols: Int)

data class Cell(var hasMine: Boolean, var isMarked: Boolean, var isExplored: Boolean)

class Field(val size: FieldSize, private val numMines: Int) {

    var steppedOnMine = false
      private set

    private val cells: Array<Array<Cell>> = Array(size.numRows) {
        Array(size.numCols) {
            Cell(hasMine = false, isMarked = false, isExplored = false)
        }
    }

    init {
        placeMines()
    }

    fun hasMine(row: Int, col: Int) = cells[row][col].hasMine

    fun isMarked(row: Int, col: Int) = cells[row][col].isMarked

    fun isExplored(row: Int, col: Int) = cells[row][col].isExplored

    fun toggleMark(row: Int, col: Int) {
        cells[row][col].isMarked = !cells[row][col].isMarked
    }

    fun explore(row: Int, col: Int) {

        val frontier = mutableSetOf<Pair<Int, Int>>()
        frontier.add(Pair(row, col))

        while (frontier.isNotEmpty()) {
            val (r, c) = frontier.first()
            frontier.remove(frontier.first())
            setExplored(r, c)
            if (hasMine(r, c)) {
                steppedOnMine = true
            } else {
                val numMines = numNeighboringMines(r, c)
                if (numMines == 0) {
                    val unexploredNeighbors = neighbors(r, c)
                            .filter { it !in frontier }
                            .filter { !isExplored(it.first, it.second) }
                    frontier.addAll(unexploredNeighbors)
                }
            }
        }

    }

    fun numNeighboringMines(row: Int, col: Int) =
            neighbors(row, col).count {
                cells[it.first][it.second].hasMine
            }

    fun gameOver(): Boolean {

        return steppedOnMine || allMinesMarked() || allSafeCellsExplored()

    }

    private fun allMinesMarked(): Boolean {

        var numMarks = 0
        var numMinesMarked = 0

        for (row in 0 until size.numRows) {
            for (col in 0 until size.numCols) {
                val cell = cells[row][col]
                if (cell.isMarked) {
                    numMarks++
                    if (cell.hasMine) {
                        numMinesMarked++
                    }
                }
            }
        }

        return (numMarks == numMines && numMinesMarked == numMarks)

    }

    private fun allSafeCellsExplored() : Boolean {

        for (row in 0 until size.numRows) {
            for (col in 0 until size.numCols) {
                val cell = cells[row][col]
                if (!cell.hasMine && !cell.isExplored) {
                    return false
                }
            }
        }

        return true
    }

    private fun neighbors(row: Int, col: Int): List<Pair<Int, Int>> {
        return listOf(
                row-1 to col-1,
                row-1 to col,
                row-1 to col+1,
                row to col-1,
                row to col+1,
                row+1 to col-1,
                row+1 to col,
                row+1 to col+1
        ).filter {
            it.first in 0 until size.numRows &&
                    it.second in 0 until size.numCols
        }
    }

    private fun placeMines() {
        for (i in 1..numMines) {
            while (true) {
                val row = Random.nextInt(size.numRows)
                val col = Random.nextInt(size.numCols)
                val cell = cells[row][col]
                if (!cell.hasMine) {
                    cell.hasMine = true
                    break
                }
            }
        }
    }

    private fun setExplored(row: Int, col: Int) {
        cells[row][col].isExplored = true
    }
}
