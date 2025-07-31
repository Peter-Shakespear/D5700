package org.example

fun main() {
   val registers = Registers()
   val rom = ROM()
   val ram = RAM()
   val screen = Screen()

   println("Testing DRAW instruction:")

   // Set up test values in registers
   registers.setValue(1, 'A'.code)  // r1 = ASCII 'A' (65)
   registers.setValue(2, 2)         // r2 = row 2
   registers.setValue(3, 3)         // r3 = column 3

   println("Before DRAW F123:")
   println("r1 (ASCII) = ${registers.getValue(1)} ('${registers.getValue(1).toChar()}')")
   println("r2 (row) = ${registers.getValue(2)}")
   println("r3 (column) = ${registers.getValue(3)}")
   screen.display()

   val drawInstruction = Draw("F123", registers, rom, ram, screen)
   drawInstruction.execute()

   println("\nAfter DRAW F123:")
   screen.display()

   // Test error case - ASCII value > 127
   println("\nTesting error case (ASCII > 127):")
   registers.setValue(4, 200)  // Invalid ASCII value
   registers.setValue(5, 1)    // row 1
   registers.setValue(6, 1)    // column 1

   try {
      val errorDrawInstruction = Draw("F456", registers, rom, ram, screen)
      errorDrawInstruction.execute()
   } catch (e: RuntimeException) {
      println("Caught expected error: ${e.message}")
   }
}