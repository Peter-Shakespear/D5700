package org.example

class Draw(instruction: String, registers: Registers, rom: ROM, ram: RAM, private val screen: Screen) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0
    private var rY: Int = 0
    private var rZ: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF   // Second hex digit (ASCII character register)
        rY = (hex shr 4) and 0xF   // Third hex digit (row register)
        rZ = hex and 0xF           // Fourth hex digit (column register)
    }

    override fun performOp() {
        val asciiValue = registers.getValue(rX)
        val row = registers.getValue(rY)
        val column = registers.getValue(rZ)

        // Check if ASCII value is valid (must be <= 127)
        if (asciiValue > 0x7F) {
            println("ERROR: ASCII value in r$rX ($asciiValue) is greater than 7F (127). Program terminated.")
            throw RuntimeException("Invalid ASCII value: $asciiValue > 127")
        }

        // Check if row and column are within screen bounds (0-7 for 8x8 screen)
        if (row < 0 || row > 7 || column < 0 || column > 7) {
            println("ERROR: Position ($row, $column) is out of screen bounds (0-7). Program terminated.")
            throw RuntimeException("Screen position out of bounds: ($row, $column)")
        }

        // Write the ASCII character to screen's internal RAM
        screen.writeCharacter(column, row, asciiValue)

        println("Drawing character '${asciiValue.toChar()}' (ASCII $asciiValue) at row $row, column $column")
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}