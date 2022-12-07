package com.yupaopao.mercury.library.socket.netty

import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.socket.Socket
import com.yupaopao.mercury.library.socket.log.SocketLogger
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.atomic.AtomicBoolean

class Client(private val socket: Socket) {

    enum class Result {
        CANCEL, FAIL, SUCCESS
    }
    private var group: NioEventLoopGroup = NioEventLoopGroup(1)
    private var channel: Channel? = null

    private val connecting = AtomicBoolean(false)

    fun connect(ip: String, port: Int, completed: (result: Result) -> Unit) {
        SocketLogger.log(socket, "connect to $ip:$port")
        if (group.isShutdown || group.isShuttingDown || group.isTerminated) {
            SocketLogger.log(socket, "create group")
            group = NioEventLoopGroup(1)
            connecting.getAndSet(false)
        }
        socket.socketVersion.incrementAndGet()
        if (connecting.get()) {
            SocketLogger.log(socket, "already in connecting")
            return
        } else {
            connecting.compareAndSet(false, true)
            SocketLogger.log(socket, "connecting")
        }
        val bootstrap = Bootstrap()
        bootstrap.group(group)
            .channel(NioSocketChannel::class.java)
            .handler(ChannelInitializer(socket))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .option(ChannelOption.TCP_NODELAY, true)
        val channelFuture = bootstrap.connect(
            ip,
            port
        )
        channelFuture.addListener {
            it as ChannelFuture
            if (it.isCancelled) {
                completed.invoke(Result.CANCEL)
                SocketLogger.log(socket, "connection attempt cancelled by user")
            } else if (!it.isSuccess) {
                completed.invoke(Result.FAIL)
                it.cause()
                    .printStackTrace()
                SocketLogger.log(socket, "connection in failure")
                SocketLogger.log(socket, it.cause().message.toString())
                socket.exceptionCallback?.invoke("connect", it.cause())
            } else {
                channel = it.sync().channel()
                completed.invoke(Result.SUCCESS)
                SocketLogger.log(socket, "connection established successfully")
            }
            connecting.compareAndSet(true, false)
        }
    }

    fun disconnect(shutdownGroup: Boolean) {
        connecting.getAndSet(false)
        channel?.closeFuture()
        channel?.close()
        if (shutdownGroup) {
            try {
                group.shutdownGracefully()
            } catch (exception: Exception) {
                SocketLogger.log(socket, exception.message.toString())
            } catch (error: Error) {
                SocketLogger.log(socket, error.message.toString())
            }
        }

        SocketLogger.log(socket, "disconnected")
    }

    fun send(accessMessage: AccessMessage) {
        channel?.writeAndFlush(accessMessage)
    }
}