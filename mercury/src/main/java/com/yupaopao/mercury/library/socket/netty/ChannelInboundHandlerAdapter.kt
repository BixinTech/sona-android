package com.yupaopao.mercury.library.socket.netty

import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.common.BatchResolve
import com.yupaopao.mercury.library.misc.Misc
import com.yupaopao.mercury.library.socket.Socket
import com.yupaopao.mercury.library.socket.log.SocketLogger
import com.yupaopao.mercury.library.socket.model.SocketStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter


class ChannelInboundHandlerAdapter(private val socket: Socket) : ChannelInboundHandlerAdapter() {
    var attachSocketVersion = 0

    init {
        attachSocketVersion = socket.socketVersion.get()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        if (attachSocketVersion != socket.socketVersion.get()) {
            return
        }
        socket.networkStatusCallback?.invoke(SocketStatus.ACTIVE)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        if (attachSocketVersion != socket.socketVersion.get()) {
            return
        }
        socket.networkStatusCallback?.invoke(SocketStatus.INACTIVE)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        super.channelRead(ctx, msg)
        if (attachSocketVersion != socket.socketVersion.get()) {
            return
        }
        val accessMessage = msg as AccessMessage
        if (accessMessage.isHeartbeat) {
            if(!accessMessage.isTwoWay){
                socket.pong?.invoke()
            }else{
                socket.rePing()
            }
        } else {
            BatchResolve.resolve(accessMessage).forEach {
                socket.receiveMessageCallback?.invoke(it)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
        if (attachSocketVersion != socket.socketVersion.get()) {
            return
        }
        socket.networkStatusCallback?.invoke(SocketStatus.EXCEPTION)
        SocketLogger.log(socket, "exception caught: " + Misc.exceptionToString(cause))
        socket.exceptionCallback?.invoke("system", cause)
    }
}