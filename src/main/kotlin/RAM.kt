
package org.example

class RAM {
    private val memory = IntArray(4096)

    fun readMemory(address: Int): Int {
        if (address >= 0 && address < memory.size) {
            return memory[address]
        }
        return 0
    }

    fun writeMemory(address: Int, value: Int) {
        if (address >= 0 && address < memory.size) {
            memory[address] = value
        }
    }

    fun getMemorySize(): Int {
        return memory.size
    }

    fun dumpMemory(startAddress: Int = 0, length: Int = 16) {
        val endAddress = minOf(startAddress + length, memory.size)
        for (i in startAddress until endAddress) {
            print("${memory[i].toString(16).padStart(4, '0')} ")
            if ((i - startAddress + 1) % 8 == 0) println()
        }
        println()
    }
}