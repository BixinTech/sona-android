package com.yupaopao.mercury.library.tunnel

import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.common.Header
import com.yupaopao.mercury.library.tunnel.model.MercuryMessage


class RemoteMessageController(
    val tunnel: Tunnel,
    mListener: MessageListener
) : MessageController(mListener) {


    override fun request(
        cmd: Int,
        data: String?,
        headers: List<Header>?,
        twoWay: Boolean,
        timeout: Long,
        clientRequestHandler: ClientRequestHandler
    ) {
        val request = MessageBuilder.request(cmd, data, headers,twoWay)
        sendMessage(request)
        if(twoWay){
            mSends[request.id] =
                ClientRequestHandlerProxy(request.id, request.cmd, timeout, clientRequestHandler)
        }else{
            clientRequestHandler.resolve("success")
        }
    }

    override fun handleRequest(request: MercuryMessage){
        if(request.needResponse()){
            mListener?.onRequest(
                request,
                object : ServerRequestHandler {
                    override fun accept(data: String?) {
                        sendMessage(
                            MessageBuilder.response(request.origin, 0, data)
                        )

                    }

                    override fun reject(code: String, errorReason: String?) {
                        sendMessage(
                            MessageBuilder.response(request.origin, -1, errorReason)
                        )
                    }
                })
        }else{
            mListener?.onRequest(request,null)
        }
    }


    override fun handleResponse(response: MercuryMessage) {
        val sent = mSends.remove(response.id)
        if (sent == null) {
            return
        }
        sent.close()
        if (response.isSuccess()) {
            sent.resolve(response.responseData ?: "")
        } else {
            sent.reject(response.code, response.responseData)
        }
    }

    override fun sendMessage(message: AccessMessage) {
        tunnel.send(message)
    }
}