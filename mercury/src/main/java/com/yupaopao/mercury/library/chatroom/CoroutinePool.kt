package com.yupaopao.mercury.library.chatroom

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

object CoroutinePool {
    private val threadPool = Executors.newFixedThreadPool(1, object : ThreadFactory {
        private val threadCount = AtomicInteger(1)
        override fun newThread(r: Runnable?): Thread =
            Thread(r, "mercury-chatroom-${threadCount.getAndIncrement()}")
    })
    val scope = CoroutineScope(threadPool.asCoroutineDispatcher())
}