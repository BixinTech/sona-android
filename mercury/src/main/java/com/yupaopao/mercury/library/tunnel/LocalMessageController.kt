package com.yupaopao.mercury.library.tunnel

import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.common.Header
import com.yupaopao.mercury.library.tunnel.model.MercuryMessage


class LocalMessageController() : MessageController(null) {
    override fun request(
        cmd: Int,
        data: String?,
        headers: List<Header>?,
        twoWay:Boolean,
        timeout: Long,
        clientRequestHandler: ClientRequestHandler
    ) {
        val request = MessageBuilder.request(cmd, data, headers,twoWay)
        mSends[request.cmd] =
            ClientRequestHandlerProxy(request.cmd, request.cmd, timeout, clientRequestHandler)
    }

    override fun handleRequest(request: MercuryMessage) {
    }

    override fun handleResponse(response: MercuryMessage) {
        val sent = mSends.remove(response.origin.cmd)
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

    }

}