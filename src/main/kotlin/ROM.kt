package org.example

import java.io.File

class ROM {
    private val memory = IntArray(4096) // 4KB ROM
    private var programCounter = 0 // Points to the next instruction

    fun loadProgram(filename: String) {
        val bytes = File(filename).readBytes()

        // Convert pairs of bytes to hex instructions and load into ROM
        for (i in bytes.indices step 2) {
            if (i + 1 < bytes.size) {
                // Combine two bytes into one 16-bit instruction
                val highByte = bytes[i].toInt() and 0xFF
                val lowByte = bytes[i + 1].toInt() and 0xFF
                val instruction = (highByte shl 8) or lowByte

                memory[i / 2] = instruction
            }
        }

        programCounter = 0
    }

    fun getHexInstruction(address: Int): String {
        return memory[address].toString(16).uppercase().padStart(4, '0')
    }

    fun getCurrentInstruction(): String {
        return getHexInstruction(programCounter)
    }

    fun fetchNextInstruction(): String {
        val instruction = getHexInstruction(programCounter)
        programCounter++
        return instruction
    }

    fun getProgramCounter(): Int {
        return programCounter
    }

    fun setProgramCounter(address: Int) {
        programCounter = address
    }

    fun incrementProgramCounter() {
        programCounter++
    }
}