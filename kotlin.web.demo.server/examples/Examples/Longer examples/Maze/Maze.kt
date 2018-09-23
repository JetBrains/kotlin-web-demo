/**
 * Let's Walk Through a Maze.
 *
 * Imagine there is a maze whose walls are the big 'O' letters.
 * Now, I stand where a big 'I' stands and some cool prize lies
 * somewhere marked with a '$' sign. Like this:
 *
 *    OOOOOOOOOOOOOOOOO
 *    O               O
 *    O$  O           O
 *    OOOOO           O
 *    O               O
 *    O  OOOOOOOOOOOOOO
 *    O           O I O
 *    O               O
 *    OOOOOOOOOOOOOOOOO
 *
 * I want to get the prize, and this program helps me do so as soon
 * as I possibly can by finding a shortest path through the maze.
 */
package maze


/**
 * Declare a point class.
 */
data class Point(val row: Int, val col: Int)

/**
 * This function looks for a path from maze.start to maze.end through
 * free space (a path does not go through walls). One can move only
 * straight up, down, left or right, no diagonal moves allowed.
 */
fun findPath(maze: Maze): List<Point>? {
    val previous = hashMapOf<Point, Point>()

    val queue = java.util.ArrayDeque<Point>()
    val visited = hashSetOf<Point>()

    queue.offer(maze.start)
    visited.add(maze.start)
    while (!queue.isEmpty()) {
        val cell = queue.poll()
        if (cell == maze.end) break

        for (newCell in maze.neighbors(cell)) {
            if (newCell in visited) continue
            previous[newCell] = cell
            queue.offer(newCell)
            visited.add(newCell)
        }
    }

    val pathToStart =
            generateSequence(previous[maze.end]) { cell -> previous[cell] }
                    .takeWhile { cell -> cell != maze.start }
                    .toList()
                    .ifEmpty { return null }
    return pathToStart.reversed()
}

fun Maze.neighbors(cell: Point): List<Point> = neighbors(cell.row, cell.col)
/**
 * Find neighbors of the ([row], [col]) cell that are not walls and not outside the maze
 */
fun Maze.neighbors(row: Int, col: Int): List<Point> = listOfNotNull(
        cellIfFree(row - 1, col),
        cellIfFree(row, col - 1),
        cellIfFree(row + 1, col),
        cellIfFree(row, col + 1)
)

fun Maze.cellIfFree(row: Int, col: Int): Point? {
    if (row !in 0 until height) return null
    if (col !in 0 until width) return null
    if (walls[row][col]) return null

    return Point(row, col)
}

fun Maze.hasWallAt(point: Point) = walls[point.row][point.col]

/**
 * A data class that represents a maze
 */
class Maze(
        // Number or columns
        val width: Int,
        // Number of rows
        val height: Int,
        // true for a wall, false for free space
        val walls: Array<BooleanArray>,
        // The starting point (must not be a wall)
        val start: Point,
        // The target point (must not be a wall)
        val end: Point
)

/** A few maze examples here */
fun main(args: Array<String>) {
    walkThroughMaze("I  $")
    walkThroughMaze("I O $")
    walkThroughMaze("""
    O  $
    O
    O
    O
    O           I
  """)
    walkThroughMaze("""
    OOOOOOOOOOO
    O $       O
    OOOOOOO OOO
    O         O
    OOOOO OOOOO
    O         O
    O OOOOOOOOO
    O        OO
    OOOOOO   IO
  """)
    walkThroughMaze("""
    OOOOOOOOOOOOOOOOO
    O               O
    O$  O           O
    OOOOO           O
    O               O
    O  OOOOOOOOOOOOOO
    O           O I O
    O               O
    OOOOOOOOOOOOOOOOO
  """)
}

// UTILITIES

fun walkThroughMaze(input: String) {
    val maze = makeMaze(input)

    println("Maze:")
    val path = findPath(maze)
    for (row in 0 until maze.height) {
        for (col in 0 until maze.width) {
            val cell = Point(row, col)
            print(when {
                maze.hasWallAt(cell) -> "O"
                cell == maze.start -> "I"
                cell == maze.end -> "$"
                path != null && cell in path -> "*"
                else -> " "
            })
        }
        println("")
    }
    println("Result: " + if (path == null) "No path" else "Path found")
    println("")
}


/**
 * A maze is encoded in the string s: the big 'O' letters are walls.
 * I stand where a big 'I' stands and the prize is marked with
 * a '$' sign.
 *
 * Example:
 *
 *    OOOOOOOOOOOOOOOOO
 *    O               O
 *    O$  O           O
 *    OOOOO           O
 *    O               O
 *    O  OOOOOOOOOOOOOO
 *    O           O I O
 *    O               O
 *    OOOOOOOOOOOOOOOOO
 */
fun makeMaze(input: String): Maze {
    val lines = input.split('\n')
    val longestLine = lines.maxBy { it.length }!!
    val data = Array(lines.size) { BooleanArray(longestLine.length) }

    var start: Point? = null
    var end: Point? = null

    for (row in lines.indices) {
        for (col in lines[row].indices) {
            when (
                val cell = lines[row][col]) {
                'O' -> data[row][col] = true
                'I' -> start = Point(row, col)
                '$' -> end = Point(row, col)
            }
        }
    }

    return Maze(longestLine.length, lines.size, data,
            start ?: throw IllegalArgumentException("No starting point in the maze (should be indicated with 'I')"),
            end ?: throw IllegalArgumentException("No goal point in the maze (should be indicated with a '$' sign)"))
}
