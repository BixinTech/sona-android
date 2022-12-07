package com.yupaopao.mercury.library.common;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author qinwei
 */
public class ServerMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int flag = in.getByte(in.readerIndex());
        if (flag != 0 && flag != 1) {
            throw new Exception("unknown flag, flag=" + in.getByte(in.readerIndex()));
        }
//        if (in.readableBytes() < 6) {
//            throw new Exception("current ByteBuf length less than meta, len=" + in.readableBytes());
//        }
        AccessMessage message = MessageCodec.decode(in);
        out.add(message);
    }

}
