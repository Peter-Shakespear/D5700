package org.example

fun main() {
   println("Type in the path to the rom file:")
   val romPath = readLine()

   if (romPath != null && romPath.isNotEmpty()) {
      val cpu = CPU()
      cpu.executeProgram(romPath)
   } else {
      println("No file path provided")
   }
}