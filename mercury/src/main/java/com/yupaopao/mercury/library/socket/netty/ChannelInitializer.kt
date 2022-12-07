package com.yupaopao.mercury.library.socket.netty

import com.yupaopao.mercury.library.common.ServerMessageDecoder
import com.yupaopao.mercury.library.common.ServerMessageEncoder
import com.yupaopao.mercury.library.socket.Socket
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel

class ChannelInitializer(private val socket: Socket) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(ServerMessageEncoder())
        pipeline.addLast(ServerMessageDecoder())
        pipeline.addLast(ChannelInboundHandlerAdapter(socket))
    }
}