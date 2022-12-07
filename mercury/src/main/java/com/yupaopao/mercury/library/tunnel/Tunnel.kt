package com.yupaopao.mercury.library.tunnel

import com.yupaopao.mercury.library.Common
import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.common.CommandEnum
import com.yupaopao.mercury.library.common.Header
import com.yupaopao.mercury.library.core.Config
import com.yupaopao.mercury.library.socket.Socket
import com.yupaopao.mercury.library.socket.log.SocketLogger
import com.yupaopao.mercury.library.socket.model.SocketStatus
import com.yupaopao.mercury.library.tunnel.log.TunnelLogger
import com.yupaopao.mercury.library.tunnel.model.MercuryMessage
import com.yupaopao.mercury.library.tunnel.model.TunnelStatus
import com.yupaopao.mercury.library.tunnel.policy.ExceptionPolicy
import com.yupaopao.mercury.library.tunnel.policy.ReconnectPolicy
import com.yupaopao.mercury.library.tunnel.util.CoroutinePool
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume

class Tunnel(val type: Int, val uid: String) :
    MessageController.MessageListener {
    var networkStatusCallback: ((tunnelStatus: TunnelStatus) -> Unit)? = null

    var receiveMessageCallback: ((mercuryMessage: MercuryMessage) -> Unit)? = null

    var logCallback: ((String) -> Unit)? = null

    private val socket: Socket

    private var connectionTimeoutJob: Job? = null
    private val reconnectPolicy = ReconnectPolicy()

    private var handShakeTimeoutJob: Job? = null

    private var heartBeatJob: Job? = null
    private var heartBeatTimeoutJob: Job? = null

    var status = SocketStatus.INACTIVE
    private val exceptionPolicy = ExceptionPolicy()
    private val exceptionUploadJob: Job

    var messageListener: MessageController.MessageListener? = null

    val remoteMsgController: MessageController by lazy {
        RemoteMessageController(this, this)
    }
    val localMsgController: MessageController by lazy {
        LocalMessageController()
    }

    val tunnelMessageApi = TunnelMessageApi(this)


    init {
        networkStatusCallback?.invoke(TunnelStatus.IDLE)
        socket = Socket(this)
        socket.networkStatusCallback = { status ->
            this.status = status
            if (status == SocketStatus.ACTIVE) {
                localMsgController.onMessage(
                    MercuryMessage(
                        MessageBuilder.response(
                            AccessMessage().apply {
                                cmd = CommandEnum.LOCAL_CONNECT.command
                            },
                            0,
                            ""
                        )
                    )
                )
                networkStatusCallback?.invoke(TunnelStatus.CONNECTED)
            } else if (status == SocketStatus.INACTIVE) {
                TunnelLogger.log(this, "reconnect because SocketStatus.INACTIVE")
                closePingInterval()
                disconnectInGoroutine(false)
                connectInGoroutine()
                networkStatusCallback?.invoke(TunnelStatus.DISCONNECT)
            }
        }
        socket.receiveMessageCallback = { accessMessage ->
            TunnelLogger.log(this, "receiver: $accessMessage")
            try {
                val message = MercuryMessage(accessMessage)
                remoteMsgController.onMessage(message)
            } catch (e: Exception) {
                TunnelLogger.log(this, "error message: $accessMessage")
            }
        }

        socket.pong = {
            SocketLogger.log(socket, "pong")
            heartBeatTimeoutJob?.let {
                val message = "pong received, ping timeout canceled"
                TunnelLogger.log(this@Tunnel, message)
                it.cancel(CancellationException())
                heartBeatTimeoutJob = null
            }
        }

        socket.logCallback = {
            TunnelLogger.log(this, it)
        }

        socket.exceptionCallback = { key, exception ->
            val cacheFilled =
                exceptionPolicy.add(Triple(System.currentTimeMillis(), key, exception))
            if (cacheFilled) {
                Common.exceptionCallback?.invoke(type, exceptionPolicy.get())
                exceptionPolicy.clear()
            }
        }

        exceptionUploadJob = CoroutinePool.scope.launch {
            repeat(Int.MAX_VALUE) {
                delay(ExceptionPolicy.UPLOAD_INTERVAL * 1000L)
                Common.exceptionCallback?.invoke(type, exceptionPolicy.get())
                exceptionPolicy.clear()
            }
        }

    }

    private suspend fun login(): MercuryResponse {
        return tunnelMessageApi.login( uid)
    }

    suspend fun connect(ip: String, port: Int) {
        if (Config.closeMercury) {
            TunnelLogger.log(this, "mercury already closed, abandon this connect")
            return
        }

        if (Common.isBackground) {
            if (Config.closeBackgroundConnect) {
                TunnelLogger.log(this, "mercury close background connect")
                return
            }
        }

        socket.connect(ip, port) {

        }
    }

    fun closePingInterval() {
        heartBeatJob?.let {
            val message = "heart beat cancel due to channel inactive"
            TunnelLogger.log(this@Tunnel, message)
            it.cancel(CancellationException(message))
            heartBeatJob = null
        }
        heartBeatTimeoutJob?.let {
            val message = "heart beat timeout cancel due to channel inactive"
            TunnelLogger.log(this@Tunnel, message)
            it.cancel(CancellationException(message))
            heartBeatTimeoutJob = null
        }
    }

    fun pingInterval() {
        socket.ping()
        heartBeatJob?.cancel(CancellationException("existed heart beat job canceled"))
        heartBeatJob = CoroutinePool.scope.launch {
            repeat(Int.MAX_VALUE) {
                if (Common.isBackground) {
                    delay(Config.backgroundPingInterval)
                } else {
                    delay(Config.pingInterval)
                }

                heartBeatTimeoutJob?.cancel(CancellationException("existed heart beat timeout job canceled"))
                heartBeatTimeoutJob = CoroutinePool.scope.launch {
                    TunnelLogger.log(
                        this@Tunnel,
                        "heartbeat timeout started: " + Config.pingTimeout
                    )
                    delay(Config.pingTimeout)
                    TunnelLogger.log(this@Tunnel, "heartbeat timeout triggered disconnect")
                    disconnectInGoroutine(false)
                }
                socket.ping()
            }
        }
    }

    suspend fun request(
        cmd: Int,
        data: String? = null,
        headers: List<Header>? = null,
        twoWay:Boolean=true,
        timeout: Long = MessageController.TIME_OUT
    ): MercuryResponse = suspendCancellableCoroutine { cancellableContinuation ->
        remoteMsgController.request(
            cmd,
            data,
            headers,
            twoWay,
            timeout,
            object : MessageController.ClientRequestHandler {
                override fun resolve(result: String) {
                    cancellableContinuation.resume(MercuryResponse(0, result))
                }
                override fun reject(code: Int, errorReason: String?) {
                    cancellableContinuation.resume(MercuryResponse(code, null, errorReason))
                }
            })
    }

    suspend fun localRequest(
        cmd: Int,
        data: String? = null,
        headers: List<Header>? = null,
        twoWay:Boolean,
        timeout: Long = MessageController.TIME_OUT
    ): MercuryResponse = suspendCancellableCoroutine { cancellableContinuation ->
        localMsgController.request(
            cmd,
            data,
            headers,
            twoWay,
            timeout,
            object : MessageController.ClientRequestHandler {
                override fun resolve(result: String) {
                    cancellableContinuation.resume(MercuryResponse(0, result))
                }

                override fun reject(code: Int, errorReason: String?) {
                    cancellableContinuation.resume(MercuryResponse(code, null, errorReason))
                }
            })
    }


    fun send(accessMessage: AccessMessage) {
        CoroutinePool.scope.launch {
            socket.send(accessMessage) {

            }
        }
    }

    fun forceReconnect() {
        if (reconnectPolicy.get() > 1) {
            reconnectPolicy.reset()
            disconnectInGoroutine(false)
            connectInGoroutine()
        }
    }

    fun release() {
        socket.pong = null
        socket.networkStatusCallback = null
        socket.receiveMessageCallback = null
        socket.exceptionCallback = null
        networkStatusCallback = null
        receiveMessageCallback = null
        logCallback = null
        socket.logCallback = null
        tryRemoveAllJob()
        disconnectInGoroutine(true)
        localMsgController.close()
        remoteMsgController.close()
    }

    fun tryRemoveAllJob() {
        connectionTimeoutJob?.let {
            val message = "connection timeout cancel due to fast connect"
            TunnelLogger.log(this@Tunnel, message)
            it.cancel(CancellationException(message))
            connectionTimeoutJob = null
        }
        handShakeTimeoutJob?.let {
            val message = "handshake timeout cancel due to fast connect"
            TunnelLogger.log(this@Tunnel, message)
            it.cancel(CancellationException(message))
            handShakeTimeoutJob = null
        }
        heartBeatJob?.let {
            val message = "heart beat cancel due to fast connect"
            TunnelLogger.log(this@Tunnel, message)
            it.cancel(CancellationException(message))
            heartBeatJob = null
        }
        heartBeatTimeoutJob?.let {
            val message = "heart beat timeout cancel due to fast connect"
            TunnelLogger.log(this@Tunnel, message)
            it.cancel(CancellationException(message))
            heartBeatTimeoutJob = null
        }
    }

    fun disconnectInGoroutine(shutdownGroup: Boolean) {
        CoroutinePool.scope.launch {
            socket.disconnect(shutdownGroup)
        }
    }

    fun connectInGoroutine() {
        CoroutinePool.scope.launch {
            connect()
        }
    }

    suspend fun connect(): MercuryResponse {
        connect(Config.ip, Config.port)
        var response = tunnelMessageApi.connect()
        if (response.code == 0) {
            response = login()
        }
        if (response.code == 0) {
            networkStatusCallback?.invoke(TunnelStatus.LOGIN)
            reconnectPolicy.reset()
            pingInterval()
        } else {
            TunnelLogger.log(this@Tunnel, "connect fail: reconnect delay" + reconnectPolicy.get())
            delay(reconnectPolicy.get())
            reconnectPolicy.increment()
            TunnelLogger.log(this@Tunnel, "connection timeout triggered disconnect")
            disconnectInGoroutine(false)
            connectInGoroutine()
        }
        return response;
    }
    suspend fun appState(){
       tunnelMessageApi.appState()
    }
    override fun onRequest(
        request: MercuryMessage,
        handler: MessageController.ServerRequestHandler?
    ) {
        messageListener?.onRequest(request, handler)
    }

}

