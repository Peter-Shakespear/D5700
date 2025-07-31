package org.example

class CPU(
    var rom: ROM,
    var registers: Registers,
    var ram: RAM
) {
    private val screen = Screen()

    fun executeProgram(path: String) {
        rom.loadProgram(path)

        while (rom.getProgramCounter() < rom.getProgramSize()) {
            val currentInstruction = rom.readMemory(rom.getProgramCounter())
            val instructionHex = String.format("%04X", currentInstruction)

            println("PC: ${rom.getProgramCounter()}, Instruction: $instructionHex")

            if (currentInstruction == 0) {
                println("Program halted on instruction 0000")
                break
            }

            val instruction = createInstruction(instructionHex)

            try {
                instruction.execute()
            } catch (e: RuntimeException) {
                println("Program terminated due to error: ${e.message}")
                break
            }

            if (instructionHex.startsWith("F")) {
                println("Current screen:")
                screen.display()
            }
        }

        screen.display()
    }

    private fun createInstruction(instructionHex: String): Instruction {
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