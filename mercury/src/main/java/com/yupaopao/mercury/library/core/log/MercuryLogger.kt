package com.yupaopao.mercury.library.core.log

import com.yupaopao.mercury.library.Common

object MercuryLogger {

    fun log(instance: Any, type: Int, log: String) {
        val hashCode = System.identityHashCode(instance)
        Common.logCallback?.invoke(type, "Mercury@$hashCode: $log")
    }
}