package org.example

class ConvertByteToAscii(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {
    private var rX: Int = 0
    private var rY: Int = 0
    private var rZ: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
        rY = (hex shr 4) and 0xF
        rZ = hex and 0xF
    }

    override fun performOp() {
        val value = registers.getValue(rX)

        if (value > 15) {
            throw RuntimeException("CONVERT_BYTE_TO_ASCII error: Value $value in r$rX is greater than F (15)")
        }

        val asciiValue = when (value) {
            in 0..9 -> '0'.code + value
            in 10..15 -> 'A'.code + (value - 10)
            else -> throw RuntimeException("Invalid hex digit: $value")
        }

        registers.setValue(rY, asciiValue)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}