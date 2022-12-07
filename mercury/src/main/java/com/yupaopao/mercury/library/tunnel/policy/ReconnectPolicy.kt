package com.yupaopao.mercury.library.tunnel.policy

import java.util.concurrent.atomic.AtomicInteger

class ReconnectPolicy {
    private var current = AtomicInteger(0)
    private val fibonacciArray = arrayOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89)

    private val lock = Any()

    fun get(): Long {
        synchronized(lock) {
            return fibonacciArray[current.get()] * 1000L
        }
    }

    fun increment() {
        synchronized(lock) {
            if (current.get() < fibonacciArray.size - 1) {
                current.incrementAndGet()
            }
        }
    }

    fun reset() {
        synchronized(lock) {
            current.set(0)
        }
    }
}