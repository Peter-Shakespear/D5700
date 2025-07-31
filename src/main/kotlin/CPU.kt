package org.example
class CPU (
    var rom: ROM,
    var registers: Registers,
    var ram: RAM
    ) {

    fun executeProgram(path: String) {
        rom.loadProgram(path)

    }
}