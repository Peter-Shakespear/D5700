package org.example

class CPU(
    var rom: ROM,
    var registers: Registers,
    var ram: RAM
) {
    private val screen = Screen()
    private val instructionFactory = InstructionFactory()

    fun executeProgram(path: String) {
        rom.loadProgram(path)

        while (rom.getProgramCounter() < rom.getProgramSize()) {
            val currentInstruction = rom.readMemory(rom.getProgramCounter())
            val instructionHex = String.format("%04X", currentInstruction)

            println("PC: ${rom.getProgramCounter()}, Instruction: $instructionHex")

            // Terminate on 0000 instruction (halt)
            if (currentInstruction == 0) {
                println("Program halted on instruction 0000")
                break
            }

            val instruction = instructionFactory.createInstruction(
                instructionHex,
                registers,
                rom,
                ram,
                screen
            )

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
}