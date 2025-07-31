package org.example

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class CPU(
    var rom: ROM,
    var registers: Registers,
    var ram: RAM
) {
    private val screen = Screen()
    private val instructionFactory = InstructionFactory()
    private val running = AtomicBoolean(false)
    private var scheduler: ScheduledExecutorService? = null

    fun executeProgram(path: String) {
        rom.loadProgram(path)
        running.set(true)

        scheduler = Executors.newSingleThreadScheduledExecutor()

        scheduler?.scheduleAtFixedRate({
            if (running.get()) {
                executeNextInstruction()
            }
        }, 0, 2, TimeUnit.MILLISECONDS)

        while (running.get()) {
            Thread.sleep(100)
        }

        scheduler?.shutdown()
        screen.display()
    }

    private fun executeNextInstruction() {
        if (rom.getProgramCounter() >= rom.getProgramSize()) {
            running.set(false)
            return
        }

        val currentInstruction = rom.readMemory(rom.getProgramCounter())
        val instructionHex = String.format("%04X", currentInstruction)

        if (currentInstruction == 0) {
            running.set(false)
            return
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
            running.set(false)
            return
        }

        if (instructionHex.startsWith("F")) {
            screen.display()
        }
    }
}