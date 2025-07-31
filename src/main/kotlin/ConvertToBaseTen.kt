package org.example

class ConvertToBaseTen(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF  // Second hex digit (register number)
    }

    override fun performOp() {
        val value = registers.getValue(rX)  // Get value from register rX
        val addressA = registers.A  // Get the base address from A register

        // Convert to decimal and extract digits
        val hundreds = value / 100
        val tens = (value % 100) / 10
        val ones = value % 10

        // Store digits in memory at addresses A, A+1, A+2
        if (registers.M == 1) {
            // Write to ROM if M = 1
            rom.writeMemory(addressA, hundreds)
            rom.writeMemory(addressA + 1, tens)
            rom.writeMemory(addressA + 2, ones)
        } else {
            // Write to RAM if M = 0
            ram.writeMemory(addressA, hundreds)
            ram.writeMemory(addressA + 1, tens)
            ram.writeMemory(addressA + 2, ones)
        }
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}