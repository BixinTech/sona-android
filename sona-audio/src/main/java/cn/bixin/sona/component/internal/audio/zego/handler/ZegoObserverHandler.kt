package cn.bixin.sona.component.internal.audio.zego.handler

import android.annotation.SuppressLint
import android.text.TextUtils
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.AudioMixBuffer
import cn.bixin.sona.component.internal.audio.AudioReportCode
import cn.bixin.sona.component.internal.audio.AudioSession
import cn.bixin.sona.component.internal.audio.SteamType
import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.component.internal.audio.zego.ZegoSampleCallback
import cn.bixin.sona.data.entity.UserData
import cn.bixin.sona.plugin.entity.PluginEntity
import cn.bixin.sona.util.SonaConfigManager
import cn.bixin.sona.util.SonaLogger
import com.zego.zegoliveroom.constants.ZegoConstants
import com.zego.zegoliveroom.entity.ZegoAudioFrame
import com.zego.zegoliveroom.entity.ZegoExtPrepSet
import com.zego.zegoliveroom.entity.ZegoPlayStreamQuality
import com.zego.zegoliveroom.entity.ZegoStreamInfo

/**
 * 注册监听
 */
class ZegoObserverHandler(component: ZegoAudio) : BaseZegoHandler(component) {

    fun addObservers() {
        addAudioPreObserver()
        addPullStreamObserver()
        addPushStreamObserver()
        addRoomObserver()
    }

    /**
     * 监听音频前处理
     */
    private fun addAudioPreObserver() { // 开启音频前处理，并设置预处理参数
        val config = ZegoExtPrepSet() // 不需要编码前处理后的数据，输出 PCM 数据
        config.encode = false
        config.sampleRate = 44100
        config.channel = 2
        config.samples = 441
        getLiveRoom()?.setAudioPrepCallback({ inFrame -> // inFrame 为 SDK 传回的待处理音频数据
            val outFrame = ZegoAudioFrame()
            outFrame.frameType = inFrame.frameType
            outFrame.samples = inFrame.samples
            outFrame.bytesPerSample = inFrame.bytesPerSample
            outFrame.channels = inFrame.channels
            outFrame.sampleRate = inFrame.sampleRate
            outFrame.timeStamp = inFrame.timeStamp
            outFrame.configLen = inFrame.configLen
            outFrame.bufLen = inFrame.bufLen
            outFrame.buffer = inFrame.buffer.duplicate()
            val data = ByteArray(outFrame.bufLen)
            outFrame.buffer[data]
            dispatchMessage(ComponentMessage.AUDIO_RECEIVE_FRAME, AudioMixBuffer(data))
            outFrame.buffer.clear()
            outFrame.buffer.put(data)
            outFrame.buffer.flip()
            outFrame
        }, config)
    }


    /**
     * 监听拉流
     */
    private fun addPullStreamObserver() {
        getLiveRoom()?.setZegoLivePlayerCallback(object : ZegoSampleCallback.PullStreamObserver() {
            override fun onPlayStateUpdate(code: Int, streamId: String) {
                val pullStreamCode = if (getStreamHandler()?.sessionType() === AudioSession.MULTI) {
                    AudioReportCode.ZEGO_PULL_STREAM_FAIL_CODE
                } else {
                    AudioReportCode.ZEGO_PULL_MIX_STREAM_FAIL_CODE
                }
                if (code != 0) { // 失败重试
                    if (!TextUtils.isEmpty(streamId)) {
                        getStreamRetryHandler().addStream(
                            streamId,
                            PullStreamRetryEntity(
                                streamId,
                                0,
                                code,
                                getStreamHandler()?.sessionType() === AudioSession.MIX
                            )
                        )
                    }
                    if (getStreamHandler()?.sessionType() === AudioSession.MIX) {
                        SonaConfigManager.getInstance().getConfig()
                    }
                    SonaLogger.log(
                        content = "拉流回调失败 streamId $streamId", code = pullStreamCode, sdkCode = code
                    )
                    return
                }
                val audioStream = getStreamHandler()?.findAudioStream(streamId)
                if (audioStream != null) {
                    dispatchMessage(ComponentMessage.AUDIO_REV_ADD_STREAM, audioStream)
                }
                SonaLogger.log(
                    content = "拉流回调成功 streamId $streamId", code = pullStreamCode, sdkCode = code
                )
            }

            override fun onPlayQualityUpdate(
                s: String, zegoPlayStreamQuality: ZegoPlayStreamQuality
            ) {
                super.onPlayQualityUpdate(s, zegoPlayStreamQuality)
                val quality = zegoPlayStreamQuality.quality
                if (quality == -1 || quality >= 3) {
                    val sb = StringBuilder()
                    sb.append("streamId=").append(s).append("&quality=")
                        .append(quality) // 当前的网络质量，0-优，1-良，2-中，3-差
                        .append("&rtt=")
                        .append(zegoPlayStreamQuality.rtt) // 设备到 ZEGO Server 的往返延时（ms）
                        .append("&pktLostRate=")
                        .append(zegoPlayStreamQuality.pktLostRate) // 设备下行丢包数（[0,255]），丢包率 = 丢包数 / 255
                        .append("&vdjFps=").append(zegoPlayStreamQuality.vdjFps).append("&akbps=")
                        .append(zegoPlayStreamQuality.akbps) // 实际接收的音频码率（Kbps）
                    SonaLogger.log(content = "onPlayQualityUpdate $sb")
                }
            }

            override fun onRecvRemoteAudioFirstFrame(streamID: String) {
                getReportHandler().reportPullStreamCostTime(streamID)
            }
        })
    }

    /**
     * 监听推流
     */
    private fun addPushStreamObserver() {
        getLiveRoom()?.setZegoLivePublisherCallback(object :
            ZegoSampleCallback.PushStreamObserver() {
            @SuppressLint("CheckResult")
            override fun onPublishStateUpdate(
                code: Int, streamId: String, hashMap: HashMap<String, Any>
            ) {
                super.onPublishStateUpdate(code, streamId, hashMap)
                if (code != 0) {
                    getStreamHandler()?.pushStreamSuccess = false
                    getSoundLevelHandler().captureSound(false)
                    getStreamRetryHandler().retryPushStream(streamId, code)
                    SonaLogger.log(
                        content = "推流回调失败 streamId $streamId, retry count = ${getStreamRetryHandler().mPushStreamRetryTimes}",
                        code = AudioReportCode.ZEGO_PUSH_STREAM_FAIL_CODE,
                        sdkCode = code
                    )
                    return
                }
                getSoundLevelHandler().captureSound(true)
                getStreamRetryHandler().removePushStreamMsg()
                SonaLogger.log(
                    content = "推流回调成功 streamId $streamId",
                    code = AudioReportCode.ZEGO_PUSH_STREAM_SUCCESS_CODE,
                    sdkCode = code
                )
            }
        })
    }

    /**
     * 监听房间
     */
    private fun addRoomObserver() {
        getLiveRoom()?.setZegoRoomCallback(object : ZegoSampleCallback.RoomObserver() {
            override fun onKickOut(reason: Int, roomID: String, customReason: String) {
                val uid = acquire(UserData::class.java)?.uid ?: ""
                val pluginEntity = PluginEntity(uid, roomID, reason)
                dispatchMessage(ComponentMessage.USER_KICK, pluginEntity)
                SonaLogger.log(content = "即构收到被踢", code = AudioReportCode.ZEGO_KICK_USER)
            }

            override fun onDisconnect(errCode: Int, roomId: String) { // 断开连接，不会再重连
                if (TextUtils.equals(getComponent().getAudioRoomId(), roomId)) {
                    getSoundLevelHandler().captureSound(false)
                    dispatchMessage(ComponentMessage.AUDIO_ERROR, errCode)
                    SonaLogger.log(
                        content = "房间彻底断开",
                        code = AudioReportCode.ZEGO_DISCONNECT_ERROR_CODE,
                        sdkCode = errCode
                    )
                }
            }

            override fun onReconnect(errCode: Int, roomId: String) { // 房间重新连接成功
                if (TextUtils.equals(getComponent().getAudioRoomId(), roomId)) {
                    dispatchMessage(ComponentMessage.AUDIO_RECONNECT, null)
                    SonaLogger.log(
                        content = "房间重连",
                        code = AudioReportCode.ZEGO_RECONNECT_CODE,
                        sdkCode = errCode
                    )
                }
            }

            override fun onTempBroken(errCode: Int, roomId: String) { // 房间断开，即构内部会进行自动重连
                if (TextUtils.equals(getComponent().getAudioRoomId(), roomId)) {
                    dispatchMessage(ComponentMessage.AUDIO_DISCONNECT, null)
                    SonaLogger.log(
                        content = "房间断开",
                        code = AudioReportCode.ZEGO_DISCONNECT_CODE,
                        sdkCode = errCode
                    )
                }
            }

            override fun onStreamUpdated(
                type: Int, streams: Array<ZegoStreamInfo>, roomId: String
            ) {
                if (TextUtils.equals(getComponent().getAudioRoomId(), roomId)) {
                    SonaLogger.print("zego room stream update type[$type]")
                    when (type) {
                        ZegoConstants.StreamUpdateType.Added -> getStreamHandler()?.let {
                            val multiStreams = ZegoStreamTransform.transform(streams)
                            it.providerStream(SteamType.AREMOTE, multiStreams)
                            if (getComponent().isAutoPullStream()) {
                                for (audioStream in multiStreams) {
                                    getReportHandler().recordPullStreamTime(audioStream.streamId)
                                    it.startPullStream(audioStream.streamId)
                                }
                            }
                        }
                        ZegoConstants.StreamUpdateType.Deleted -> getStreamHandler()?.let {
                            val multiStreams = ZegoStreamTransform.transform(streams)
                            it.providerStream(SteamType.DREMOTE, multiStreams)
                            for (audioStream in multiStreams) {
                                it.stopPullStream(audioStream.streamId)
                                dispatchMessage(
                                    ComponentMessage.AUDIO_REV_REMOVE_STREAM,
                                    audioStream
                                )
                            }
                            val remoteStream = it.acquireStream(SteamType.REMOTE)
                            if (remoteStream is List<*> && remoteStream.isEmpty()) {
                                SonaConfigManager.getInstance().getConfig()
                            }
                        }
                        else -> {

                        }
                    }
                    if (streams.isNotEmpty()) {
                        SonaLogger.log(
                            content = "房间内流变化:${
                                streams.joinToString {
                                    it.streamID
                                }
                            }",
                            code = AudioReportCode.ZEGO_STREAM_CHANGE,
                            sdkCode = type
                        )
                    }
                }
            }
        })
    }

    override fun unAssembling() {
        getLiveRoom()?.let {
            it.setAudioPrepCallback(null, null)
            it.setZegoLivePublisherCallback(null)
            it.setZegoLivePlayerCallback(null)
            it.setZegoRoomCallback(null)
        }
    }

}