
package org.example

class Add(instruction: String, registers: Registers, rom: ROM) : Instruction(instruction, registers, rom) {

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
        val valueX = registers.getValue(rX)
        val valueY = registers.getValue(rY)
        val result = valueX + valueY
        registers.setValue(rZ, result)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}