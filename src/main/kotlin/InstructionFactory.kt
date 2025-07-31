package org.example

class InstructionFactory {

    fun createInstruction(
        instructionHex: String,
        registers: Registers,
        rom: ROM,
        ram: RAM,
        screen: Screen
    ): Instruction {
        val firstDigit = instructionHex[0]

        return when (firstDigit) {
            '0' -> Store(instructionHex, registers, rom, ram)
            '1' -> Add(instructionHex, registers, rom, ram)
            '2' -> Subtract(instructionHex, registers, rom, ram)
            '3' -> Read(instructionHex, registers, rom, ram)
            '4' -> Write(instructionHex, registers, rom, ram)
            '5' -> Jump(instructionHex, registers, rom, ram)
            '6' -> ReadKeyboard(instructionHex, registers, rom, ram)
            '7' -> SwitchMemory(instructionHex, registers, rom, ram)
            '8' -> SkipEqual(instructionHex, registers, rom, ram)
            '9' -> SkipNotEqual(instructionHex, registers, rom, ram)
            'A' -> SetA(instructionHex, registers, rom, ram)
            'B' -> SetT(instructionHex, registers, rom, ram)
            'C' -> ReadT(instructionHex, registers, rom, ram)
            'D' -> ConvertToBaseTen(instructionHex, registers, rom, ram)
            'E' -> ConvertByteToAscii(instructionHex, registers, rom, ram)
            'F' -> Draw(instructionHex, registers, rom, ram, screen)
            else -> throw RuntimeException("Unknown instruction: $instructionHex")
        }
    }
}