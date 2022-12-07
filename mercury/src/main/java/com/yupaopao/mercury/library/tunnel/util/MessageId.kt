package com.yupaopao.mercury.library.tunnel.util

import java.util.concurrent.atomic.AtomicInteger

object MessageId {
    private val atomicInteger = AtomicInteger(0)

    fun get(): Int {
        if (atomicInteger.get() >= 8000000) {
            atomicInteger.set(0)
        }
        return (atomicInteger.incrementAndGet())
    }
}