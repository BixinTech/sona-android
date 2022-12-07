package com.yupaopao.mercury.library.tunnel.log

import com.yupaopao.mercury.library.Common
import com.yupaopao.mercury.library.tunnel.Tunnel

object TunnelLogger {
    fun log(tunnel: Tunnel, log: String) {
        val hashCode = System.identityHashCode(tunnel)
        tunnel.logCallback?.invoke("Tunnel@$hashCode: $log")
    }
}