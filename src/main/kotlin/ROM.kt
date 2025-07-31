package org.example

import java.io.File

class ROM {
    private val memory = IntArray(4096)
    private var programCounter = 0
    private var isWritable = false


    fun loadProgram(filename: String) {
        val bytes = File(filename).readBytes()

        for (i in bytes.indices step 2) {
            if (i + 1 < bytes.size) {
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

    fun readMemory(address: Int): Int {
        if (address >= 0 && address < memory.size) {
            return memory[address]
        }
        return 0
    }

    fun incrementProgramCounter() {
        programCounter++
    }

    fun setProgramCounter(address: Int) {
        programCounter = address
    }

    fun getProgramCounter(): Int {
        return programCounter
    }

    fun getProgramSize(): Int {
        return memory.size
    }

    fun writeMemory(address: Int, value: Int): Boolean {
        return if (isWritable && address >= 0 && address < memory.size) {
            memory[address] = value
            true
        } else {
            false
        }
    }

    fun setWritable(writable: Boolean) {
        isWritable = writable
    }

    fun isWritable(): Boolean {
        return isWritable
    }
}