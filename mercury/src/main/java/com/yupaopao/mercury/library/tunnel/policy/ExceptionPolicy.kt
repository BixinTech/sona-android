package com.yupaopao.mercury.library.tunnel.policy

class ExceptionPolicy {
    companion object {
        const val MAX_EXCEPTION_CACHE = 50
        const val UPLOAD_INTERVAL = 30
    }

    private val exceptionCache = ArrayList<Triple<Long, String, Throwable>>()

    private val lock = Any()

    fun add(tripple: Triple<Long, String, Throwable>): Boolean {
        synchronized(lock) {
            exceptionCache.add(tripple)
            return exceptionCache.size == MAX_EXCEPTION_CACHE
        }
    }

    fun get(): ArrayList<Triple<Long, String, Throwable>> {
        synchronized(lock) {
            return exceptionCache
        }
    }

    fun clear() {
        synchronized(lock) {
            exceptionCache.clear()
        }
    }
}