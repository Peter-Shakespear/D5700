package org.example

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class Timer(private val registers: Registers, private val screen: Screen) {
    private var scheduler: ScheduledExecutorService? = null
    private val running = AtomicBoolean(false)
    private var tickCount = 0

    fun start() {
        if (running.get()) return

        running.set(true)
        scheduler = Executors.newSingleThreadScheduledExecutor()

        scheduler?.scheduleAtFixedRate({
            if (running.get()) {
                tick()
            }
        }, 0, 16, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        running.set(false)
        scheduler?.shutdown()
        scheduler = null
        tickCount = 0
    }

    private fun tick() {
        if (registers.T > 0) {
            registers.setT(registers.T - 1)
        }

        tickCount++

        if (tickCount >= 60) {
            println("Timer: T = ${registers.T}")
            screen.display()
            tickCount = 0
        }
    }
}