package org.example

class Screen {
    private val frameBuffer = IntArray(64) { 32 }

    private val width = 8
    private val height = 8

    fun writeCharacter(x: Int, y: Int, asciiValue: Int) {
        if (isValidPosition(x, y)) {
            val index = y * width + x
            frameBuffer[index] = asciiValue
        }
    }

    fun readCharacter(x: Int, y: Int): Int {
        return if (isValidPosition(x, y)) {
            val index = y * width + x
            frameBuffer[index]
        } else {
            0
        }
    }

    fun clear() {
        for (i in frameBuffer.indices) {
            frameBuffer[i] = 32
        }
    }

    fun display() {
        println("┌────────┐")
        for (y in 0 until height) {
            print("│")
            for (x in 0 until width) {
                val asciiValue = readCharacter(x, y)
                val char = if (asciiValue in 32..126) {
                    asciiValue.toChar()
                } else {
                    '?'
                }
                print(char)
            }
            println("│")
        }
        println("└────────┘")
    }

    private fun isValidPosition(x: Int, y: Int): Boolean {
        return x >= 0 && x < width && y >= 0 && y < height
    }
}