package org.example

abstract class Instruction (
    val instruction: String,
    protected val registers: Registers,
    protected val rom: ROM

) {
    fun execute() {
        organizeBytes()
        performOp()
        updatePC()
    }

    abstract fun organizeBytes()

    abstract fun performOp()

    abstract fun updatePC()
}