package org.example

class Draw(instruction: String, registers: Registers, rom: ROM, ram: RAM, private val screen: Screen) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0
    private var row: Int = 0
    private var column: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
        row = (hex shr 4) and 0xF
        column = hex and 0xF
    }

    override fun performOp() {
        val character = registers.getValue(rX)

        if (character > 127) {
            throw RuntimeException("DRAW error: ASCII value $character in r$rX is greater than 7F (127)")
        }

        if (row < 0 || row > 7 || column < 0 || column > 7) {
            throw RuntimeException("Screen position out of bounds: row=$row, column=$column")
        }

        screen.writeCharacter(column, row, character)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}