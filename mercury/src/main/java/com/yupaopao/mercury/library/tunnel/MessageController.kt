package com.yupaopao.mercury.library.tunnel

import android.os.Handler
import android.os.Looper
import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.common.Header
import com.yupaopao.mercury.library.tunnel.model.MercuryMessage
import java.util.HashMap


abstract class MessageController(
    val mListener: MessageListener?
) {
    val mSends: MutableMap<Int, ClientRequestHandlerProxy> = HashMap()

    companion object  {
        const val TIME_OUT = 5000L

    }

    private val mTimerCheckHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    interface ServerRequestHandler {
        fun accept(data: String? = null)
        fun reject(code: String, errorReason: String?)
    }

    interface ClientRequestHandler {
        fun resolve(data: String)
        fun reject(code: Int, errorReason: String?)
    }

    inner class ClientRequestHandlerProxy(
        var mRequestId: Int,
        var cmd: Int,
        timeoutDelayMillis: Long,
        var mClientRequestHandler: ClientRequestHandler
    ) : ClientRequestHandler, Runnable {
        init {
            mTimerCheckHandler.postDelayed(this, timeoutDelayMillis)
        }

        override fun run() {
            mSends.remove(mRequestId)
            mClientRequestHandler.reject(-1, "request timeout")
        }

        override fun resolve(data: String) {
            mClientRequestHandler.resolve(data)
        }

        override fun reject(code: Int, errorReason: String?) {
            mClientRequestHandler.reject(code, errorReason)
        }

        fun close() {
            mTimerCheckHandler.removeCallbacks(this)
        }
    }

    //本地发送的请求只做记录
    abstract fun request(
        cmd: Int,
        data: String?,
        headers: List<Header>? = null,
        toWay:Boolean,
        timeout: Long = TIME_OUT,
        clientRequestHandler: ClientRequestHandler
    )

    fun onMessage(message: MercuryMessage) {
        if (message.isRequest()) {
            handleRequest(message)
        } else if (message.isResponse()) {
            handleResponse(message)
        }
    }

    abstract fun handleRequest(request: MercuryMessage)
    abstract fun handleResponse(response: MercuryMessage)

    fun close() {
        for (proxy in mSends.values) {
            proxy.close()
        }
        mSends.clear()
    }

    abstract fun sendMessage(message: AccessMessage)

    interface MessageListener {
        fun onRequest(request: MercuryMessage, handler: ServerRequestHandler?)
    }

}