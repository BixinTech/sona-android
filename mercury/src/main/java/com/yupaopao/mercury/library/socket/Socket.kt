package com.yupaopao.mercury.library.socket

import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.socket.log.SocketLogger
import com.yupaopao.mercury.library.socket.model.SocketStatus
import com.yupaopao.mercury.library.socket.netty.Client
import com.yupaopao.mercury.library.tunnel.Tunnel
import com.yupaopao.mercury.library.tunnel.MessageBuilder
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class Socket(val tunnel: Tunnel) {

    private val client: Client
    val socketVersion = AtomicInteger(0)

    init {
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
        client = Client(this)
    }

    var networkStatusCallback: ((socketStatus: SocketStatus) -> Unit)? = null

    fun connect(ip: String, port: Int, completed: (result: Client.Result) -> Unit) {
        try {
            client.connect(ip, port, completed)
        } catch (exception: Exception) {
            exception.printStackTrace()
        } catch (error: Error) {
            error.printStackTrace()
        }
    }

    fun send(accessMessage: AccessMessage, completed: (success: Boolean) -> Unit) {
        SocketLogger.log(
            this,
            "send message:"
                    + accessMessage
        )
        client.send(accessMessage)
        completed.invoke(true)
    }

    var receiveMessageCallback: ((AccessMessage) -> Unit)? = null

    fun disconnect(shutdownGroup: Boolean) {
        client.disconnect(shutdownGroup)
    }

    fun ping() {
        val ping=MessageBuilder.ping()
        client.send(ping)
        SocketLogger.log(this, "ping="+ping.toString())
    }
    fun rePing(){
        val ping=MessageBuilder.rePing()
        client.send(ping)
        SocketLogger.log(this, "reping="+ping.toString())
    }

    var pong: (() -> Unit)? = null

    var logCallback: ((String) -> Unit)? = null

    var exceptionCallback: ((String, Throwable) -> Unit)? = null

    override fun toString(): String {
        val result = super.toString()
        return result.substring(result.lastIndexOf(".") + 1)
    }
}