package com.yupaopao.mercury.library.socket.log

import com.yupaopao.mercury.library.socket.Socket

object SocketLogger {
    fun log(socket: Socket, log: String) {
        val hashCode = System.identityHashCode(socket)
        socket.logCallback?.invoke("Socket@$hashCode: $log")
    }
}