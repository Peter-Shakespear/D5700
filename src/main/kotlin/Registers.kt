package org.example

class Registers (
    r0: Int = 0,
    r1: Int = 0,
    r2: Int = 0,
    r3: Int = 0,
    r4: Int = 0,
    r5: Int = 0,
    r6: Int = 0,
    r7: Int = 0,
    T: Int = 0,
    A: Int = 0,
    M: Int = 0,
) {
    var r0: Int = r0
        private set
    var r1: Int = r1
        private set
    var r2: Int = r2
        private set
    var r3: Int = r3
        private set
    var r4: Int = r4
        private set
    var r5: Int = r5
        private set
    var r6: Int = r6
        private set
    var r7: Int = r7
        private set
    var T: Int = T
        private set
    var A: Int = A
        private set
    var M: Int = M
        private set

    fun setT(value: Int) { T = value }
    fun setA(value: Int) { A = value }
    fun setM(value: Int) { M = value }

    fun getValue(registerNumber: Int): Int {
        return when (registerNumber) {
            0 -> r0
            1 -> r1
            2 -> r2
            3 -> r3
            4 -> r4
            5 -> r5
            6 -> r6
            7 -> r7
            else -> 0
        }
    }

    fun setValue(registerNumber: Int, value: Int) {
        when (registerNumber) {
            0 -> r0 = value
            1 -> r1 = value
            2 -> r2 = value
            3 -> r3 = value
            4 -> r4 = value
            5 -> r5 = value
            6 -> r6 = value
            7 -> r7 = value
        }
    }
}