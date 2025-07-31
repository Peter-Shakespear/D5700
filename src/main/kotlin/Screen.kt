package org.example

class Screen {
    // 8x8 screen = 64 bytes of frame buffer
    private val frameBuffer = IntArray(64) { 32 } // Initialize with space characters (ASCII 32)

    // Screen dimensions
    private val width = 8
    private val height = 8

    /**
     * Write a character to the screen at the specified position
     * @param x X coordinate (0-7)
     * @param y Y coordinate (0-7)
     * @param asciiValue ASCII value of the character to display
     */
    fun writeCharacter(x: Int, y: Int, asciiValue: Int) {
        if (isValidPosition(x, y)) {
            val index = y * width + x
            frameBuffer[index] = asciiValue
        }
    }

    /**
     * Write a character to the screen using linear addressing
     * @param address Linear address (0-63)
     * @param asciiValue ASCII value of the character to display
     */
    fun writeCharacter(address: Int, asciiValue: Int) {
        if (address >= 0 && address < frameBuffer.size) {
            frameBuffer[address] = asciiValue
        }
    }

    /**
     * Read a character from the screen at the specified position
     * @param x X coordinate (0-7)
     * @param y Y coordinate (0-7)
     * @return ASCII value of the character at that position
     */
    fun readCharacter(x: Int, y: Int): Int {
        return if (isValidPosition(x, y)) {
            val index = y * width + x
            frameBuffer[index]
        } else {
            0
        }
    }

    /**
     * Read a character from the screen using linear addressing
     * @param address Linear address (0-63)
     * @return ASCII value of the character at that address
     */
    fun readCharacter(address: Int): Int {
        return if (address >= 0 && address < frameBuffer.size) {
            frameBuffer[address]
        } else {
            0
        }
    }

    /**
     * Clear the entire screen (fill with spaces)
     */
    fun clear() {
        for (i in frameBuffer.indices) {
            frameBuffer[i] = 32 // ASCII space
        }
    }

    /**
     * Display the current screen contents to console
     */
    fun display() {
        println("┌────────┐")
        for (y in 0 until height) {
            print("│")
            for (x in 0 until width) {
                val asciiValue = readCharacter(x, y)
                val char = if (asciiValue in 32..126) {
                    asciiValue.toChar()
                } else {
                    '?' // Display unknown characters as ?
                }
                print(char)
            }
            println("│")
        }
        println("└────────┘")
    }

    /**
     * Get the frame buffer as a copy (for debugging or external access)
     */
    fun getFrameBuffer(): IntArray {
        return frameBuffer.copyOf()
    }

    /**
     * Set the entire frame buffer from an array
     */
    fun setFrameBuffer(buffer: IntArray) {
        if (buffer.size == frameBuffer.size) {
            for (i in frameBuffer.indices) {
                frameBuffer[i] = buffer[i]
            }
        }
    }

    /**
     * Convert x,y coordinates to linear address
     */
    fun coordinatesToAddress(x: Int, y: Int): Int {
        return if (isValidPosition(x, y)) {
            y * width + x
        } else {
            -1
        }
    }

    /**
     * Convert linear address to x,y coordinates
     */
    fun addressToCoordinates(address: Int): Pair<Int, Int> {
        return if (address >= 0 && address < frameBuffer.size) {
            val x = address % width
            val y = address / width
            Pair(x, y)
        } else {
            Pair(-1, -1)
        }
    }

    private fun isValidPosition(x: Int, y: Int): Boolean {
        return x >= 0 && x < width && y >= 0 && y < height
    }
}