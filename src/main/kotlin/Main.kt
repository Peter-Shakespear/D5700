package org.example

fun main() {
   val registers = Registers()
   val rom = ROM()
   val ram = RAM()
   val cpu = CPU(rom, registers, ram)

   print("Type in the path to the rom file: ")
   val romFilePath = readLine() ?: ""

   try {
      cpu.executeProgram(romFilePath)
   } catch (e: Exception) {
      println("Error executing program: ${e.message}")
   }
}