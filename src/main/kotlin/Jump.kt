package org.example

class Jump(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var address: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        address = hex and 0xFFF
    }

    override fun performOp() {
        if (address % 2 != 0) {
            println("ERROR: Jump address $address (0x${address.toString(16).uppercase()}) is not divisible by 2. Program terminated.")
            throw RuntimeException("Invalid jump address: $address is not divisible by 2")
        }

        // Set the program counter to the target address
        rom.setProgramCounter(address)
    }

    override fun updatePC() {
        return
    }
}