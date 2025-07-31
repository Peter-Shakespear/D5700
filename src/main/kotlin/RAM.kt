
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
}