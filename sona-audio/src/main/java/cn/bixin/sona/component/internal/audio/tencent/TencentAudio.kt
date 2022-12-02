package cn.bixin.sona.component.internal.audio.tencent

import android.os.Bundle
import android.text.TextUtils
import cn.bixin.sona.base.Sona
import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.AudioError
import cn.bixin.sona.component.audio.AudioMixBuffer
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.audio.IAudioPlayer
import cn.bixin.sona.component.internal.audio.*
import cn.bixin.sona.data.AudioDeviceModeEnum
import cn.bixin.sona.data.StreamModeEnum
import cn.bixin.sona.data.StreamSupplierEnum
import cn.bixin.sona.data.entity.AppInfo
import cn.bixin.sona.data.entity.RoomInfo
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.data.entity.UserData
import cn.bixin.sona.plugin.config.AudioConfig
import cn.bixin.sona.plugin.entity.SoundLevelInfoEntity
import cn.bixin.sona.report.SonaReport
import cn.bixin.sona.report.SonaReportEvent
import cn.bixin.sona.util.SonaConfigManager
import cn.bixin.sona.util.SonaLogger
import com.alibaba.fastjson.JSONObject
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudListener.TRTCAudioFrameListener
import java.util.*

/**
 *
 * @Author luokun
 * @Date 2020-03-03
 */
class TencentAudio(target: cn.bixin.sona.component.audio.AudioComponent) :
    AudioComponentWrapper(target) {

    private var mTRTCCloud: TRTCCloud? = null
    private var mTRTCCloudListener: TRTCCloudListener? = null
    private var mTRTCAudioFrameListenerImpl: TRTCAudioFrameListenerImpl? = null
    private var mStream: TencentStream? = null
    private var mPlayer = mutableMapOf<IAudioPlayer.Index, TencentPlayer>()
    private var mHandsfreePattern = false
    private val userList = Collections.synchronizedSet(HashSet<String>())
    private var shouldPushStream = false // 麦下到麦上需要先登录再推流，通过这个状态防止异步问题
    private val pullStreamStartTimeMap = HashMap<String, Long>()

    override fun unAssembling() {
        super.unAssembling()
        mStream?.stopPublishStream()
        mStream?.stopPullStream()
        mTRTCCloud?.enableAudioVolumeEvaluation(0)
        mTRTCCloud?.setListener(null)
        mTRTCCloudListener = null
        mTRTCCloud?.setAudioFrameListener(null)
        mTRTCAudioFrameListenerImpl = null
        mPlayer.forEach {
            it.value.release()
        }
        mTRTCCloud?.let {
            it.exitRoom()
            SonaLogger.log(
                content = "退出房间",
                code = AudioReportCode.TENCENT_LOGIN_OUT_SUCCESS_CODE
            )
            TRTCCloud.destroySharedInstance()
            SonaLogger.log(
                content = "反初始化SDK",
                code = AudioReportCode.TENCENT_UN_INIT_SDK_SUCCESS_CODE
            )
        }
    }

    override fun enter(
        roomId: String,
        streamConfig: RoomInfo.StreamConfig,
        componentCallback: ComponentCallback?
    ) {
        mStream = TencentStream(mTRTCCloud!!, this)
        var mixStream: String? = null
        if (StreamModeEnum.MIXED.modeName == streamConfig.pullMode && !TextUtils.isEmpty(
                streamConfig.streamUrl
            )
        ) {
            // 混流模式，且混流地址不为空
            mixStream = streamConfig.streamUrl
        }
        if (!TextUtils.isEmpty(mixStream)) {
            mStream?.setAudioSession(AudioSession.MIX)
            mStream?.providerStream(SteamType.MIX, AudioStream(mixStream!!, "", ""))
            mTRTCCloud?.enableAudioVolumeEvaluation(0)
            componentCallback?.executeSuccess()
            SonaLogger.print("混流地址: $mixStream")
        } else {
            login(roomId, streamConfig.streamId, streamConfig.appInfo, componentCallback)
        }
    }

    override fun switchMic(on: Boolean, componentCallback: ComponentCallback?) {
        mTRTCCloud?.muteLocalAudio(!on)
        componentCallback?.executeSuccess()
        SonaLogger.log(
            content = if (on) "打开麦克风成功" else "关闭麦克风成功",
            code = AudioReportCode.TENCENT_MIC_SUCCESS_CODE
        )
    }

    override fun switchHandsfree(on: Boolean, componentCallback: ComponentCallback?) {
        mHandsfreePattern = on
        mTRTCCloud?.setAudioRoute(if (on) TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER else TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE)
        componentCallback?.executeSuccess()
        SonaLogger.log(
            content = if (on) "打开免提成功" else "关闭免提成功",
            code = AudioReportCode.TENCENT_HANDSFREE_SUCCESS_CODE
        )
    }

    override fun silent(streamId: String?, on: Boolean, sonaCoreCallback: ComponentCallback?) {
        val result = mStream?.silentStream(streamId, on) ?: false
        if (result) {
            sonaCoreCallback?.executeSuccess()
        } else {
            sonaCoreCallback?.executeFailure(AudioError.SILENT_ERROR, if (on) "静音失败" else "取消静音失败")
        }
    }

    override fun pushStream(streamId: String?, componentCallback: ComponentCallback?) {
        shouldPushStream = true
        if (AudioSession.MIX == mStream?.sessionType()) {
            // 如果是混流模式，则停止拉流，并且设置为多路流模式
            mStream?.stopPullStream()
            mStream?.setAudioSession(AudioSession.MULTI)

            val streamRoomId = acquire(SonaRoomData::class.java)?.streamInfo?.streamRoomId?.get(
                StreamSupplierEnum.TENCENT.supplierName
            )
            if (TextUtils.isEmpty(streamRoomId)) {
                componentCallback?.executeFailure(AudioError.PUSH_STREAM_ERROR, "推流失败，缺少roomId")
                return
            }
            login(streamRoomId!!,
                streamId ?: "",
                acquire(SonaRoomData::class.java)?.streamInfo?.appInfo ?: null,
                object : ComponentCallback {
                    override fun executeSuccess() {
                        if (shouldPushStream) {
                            pushStreamStep2(streamId, componentCallback)
                        }
                    }

                    override fun executeFailure(code: Int, reason: String?) {
                        componentCallback?.executeFailure(code, reason)
                    }
                }
            )
        } else {
            pushStreamStep2(streamId, componentCallback)
        }
    }

    override fun stopPushStream(componentCallback: ComponentCallback?) {
        shouldPushStream = false
        val result = mStream?.stopPublishStream() ?: false
        if (componentCallback != null) {
            if (result) {
                mStream?.providerStream(SteamType.LOCAL, null)
                componentCallback.executeSuccess()
            } else {
                componentCallback.executeFailure(AudioError.STOP_PUSH_STREAM_ERROR, "停止推流失败")
            }
        }
    }

    override fun pullStream(streamId: String?, componentCallback: ComponentCallback?) {
        val audioStream = mStream?.findAudioStream(streamId)
        if (audioStream == null) {
            // 不存在，则拉流
            val result = mStream?.startPullStream(streamId) ?: false
            if (componentCallback != null) {
                if (result) {
                    componentCallback.executeSuccess()
                } else {
                    componentCallback.executeFailure(AudioError.PULL_STREAM_ERROR, "拉流失败")
                }
            }
        } else {
            componentCallback?.executeSuccess()
        }
    }

    override fun stopPullStream(streamId: String?, componentCallback: ComponentCallback?) {
        val audioStream = mStream?.findAudioStream(streamId)
        if (audioStream != null) {
            // 已经存在，则停止拉流
            val result = mStream?.stopPullStream(streamId) ?: false
            if (componentCallback != null) {
                if (result) {
                    componentCallback.executeSuccess()
                } else {
                    componentCallback.executeFailure(AudioError.STOP_PULL_STREAM_ERROR, "停止拉流失败")
                }
            }
        } else {
            componentCallback?.executeSuccess()
        }
    }

    override fun pullStream(componentCallback: ComponentCallback?) {
        setAutoPullStream(true)
        val result = mStream?.startPullStream() ?: false
        if (result) {
            if (!mHandsfreePattern) {
                // 腾讯默认是扬声器模式，并且是在拉流后才能切换为听筒模式，所以这里做了处理：
                // 在拉流后，判断如果处于听筒模式，且是支持免提功能的产品则设置为听筒模式
                acquire(SonaRoomData::class.java)?.streamInfo?.switchSpeaker?.apply {
                    if ("1" == this) {
                        mTRTCCloud?.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE)
                    }
                }
            }
            componentCallback?.executeSuccess()
        } else {
            componentCallback?.executeFailure(AudioError.PULL_STREAM_ERROR, "拉流失败")
        }
    }

    override fun stopPullStream(componentCallback: ComponentCallback?) {
        setAutoPullStream(false)
        val result = mStream?.stopPullStream() ?: false
        if (result) {
            componentCallback?.executeSuccess()
        } else {
            componentCallback?.executeFailure(AudioError.STOP_PULL_STREAM_ERROR, "停止拉流失败")
        }
    }

    override fun switchListen(realTime: Boolean, componentCallback: ComponentCallback?) {
        if (mStream?.sessionType() == AudioSession.MULTI) {
            componentCallback?.executeSuccess()
            return
        }
        mStream?.stopPullStream()
        mStream?.setAudioSession(AudioSession.MULTI)

        val streamRoomId =
            acquire(SonaRoomData::class.java)?.streamInfo?.streamRoomId?.get(StreamSupplierEnum.TENCENT.supplierName)
        if (TextUtils.isEmpty(streamRoomId)) {
            componentCallback?.executeFailure(AudioError.PUSH_STREAM_ERROR, "缺少roomId")
            return
        }
        val streamId = acquire(SonaRoomData::class.java)?.streamInfo?.streamId

        login(streamRoomId!!,
            streamId ?: "",
            acquire(SonaRoomData::class.java)?.streamInfo?.appInfo ?: null,
            object : ComponentCallback {
                override fun executeSuccess() {
                    componentCallback?.executeSuccess()
                }

                override fun executeFailure(code: Int, reason: String?) {
                    componentCallback?.executeFailure(code, reason)
                }
            }
        )
    }

    override fun init(): Boolean {
        mTRTCCloudListener = TRTCCloudListenerImpl()
        val context = Sona.getAppContext().applicationContext
        TRTCCloud.setLogDirPath(context.filesDir.path)
        mTRTCCloud = TRTCCloud.sharedInstance(context)
        mTRTCCloud?.setListener(mTRTCCloudListener)
        mTRTCAudioFrameListenerImpl = TRTCAudioFrameListenerImpl()
        mTRTCCloud?.setAudioFrameListener(mTRTCAudioFrameListenerImpl)
        mTRTCCloud?.setMixedPlayAudioFrameCallbackFormat(
            TRTCCloudDef.TRTCAudioFrameCallbackFormat().apply {
                channel = 2
                sampleRate = 44100
                samplesPerCall = 441
            })
        mTRTCCloud?.callExperimentalAPI(create3AInfo("forceCallbackMixedPlayAudioFrame"))
        val result = mTRTCCloud != null
        SonaLogger.log(
            content = "初始化SDK",
            code = if (result) AudioReportCode.TENCENT_INIT_SDK_SUCCESS_CODE else AudioReportCode.TENCENT_INIT_SDK_FAIL_CODE
        )
        return result
    }

    @Synchronized
    override fun getAudioPlayer(): IAudioPlayer? {
        mPlayer[IAudioPlayer.Index.NONE]?.let {
            return it
        }

        mTRTCCloud?.let {
            mPlayer[IAudioPlayer.Index.NONE] = TencentPlayer(mTRTCCloud!!, IAudioPlayer.Index.NONE)
        }
        return mPlayer[IAudioPlayer.Index.NONE]
    }

    override fun currentStream(): List<AudioStream>? {
        return mStream?.findAudioStream()
    }

    override fun setAudioDeviceMode(audioDeviceMode: AudioDeviceModeEnum) {

    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun compareAndGet(index: IAudioPlayer.Index): IAudioPlayer? {
        mPlayer[index]?.let {
            return it
        }
        mTRTCCloud?.let {
            mPlayer[index] = TencentPlayer(mTRTCCloud!!, index)
        }
        return mPlayer[index]
    }

    private fun login(
        roomId: String,
        streamId: String?,
        appInfo: AppInfo?,
        componentCallback: ComponentCallback?
    ) {
        val myUid = acquire(UserData::class.java)?.uid
        if (appInfo == null || appInfo.appId == 0 || TextUtils.isEmpty(appInfo.appSign) || TextUtils.isEmpty(
                myUid
            )
        ) {
            componentCallback?.executeFailure(AudioError.LOGIN_ROOM_ERROR, "参数错误")
            return
        }

        val trtcParams = TRTCCloudDef.TRTCParams()
        trtcParams.sdkAppId = appInfo.appId
        trtcParams.userId = myUid
        trtcParams.userSig = appInfo.appSign
        trtcParams.roomId = roomId.toInt()
        trtcParams.businessInfo = createBusinessInfo(streamId)
        trtcParams.role = TRTCCloudDef.TRTCRoleAudience
        val loginStartTime = System.currentTimeMillis()
        mTRTCCloudListener?.enterObserver = object : TRTCCloudListener.Observer {
            override fun onSuccess() {
                reportLoginCostTime(loginStartTime)
                mTRTCCloud?.muteAllRemoteAudio(!isAutoPullStream())

                val audioConfig = acquire(AudioConfig::class.java)
                val soundInterval = audioConfig?.voiceVolumeInterval ?: 0L
                if (audioConfig?.soundCapture == true) {
                    mTRTCCloud?.enableAudioVolumeEvaluation(if (soundInterval == 0L) 500 else soundInterval.toInt())
                }

                componentCallback?.executeSuccess()
                SonaLogger.log(
                    content = "登录房间成功",
                    code = AudioReportCode.TENCENT_LOGIN_SUCCESS_CODE
                )

                setVolumeType()
            }

            override fun onError(errorCode: Int) {
                SonaLogger.log(
                    content = "登录房间失败",
                    code = AudioReportCode.TENCENT_LOGIN_FAIL_CODE
                )
                componentCallback?.executeFailure(AudioError.LOGIN_ROOM_ERROR, "登录失败")
            }
        }

        mTRTCCloud?.muteAllRemoteAudio(!isAutoPullStream())
        mTRTCCloud?.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_LIVE)
    }

    private fun createBusinessInfo(streamId: String?): String {
        val ucObject = JSONObject()
        ucObject["userdefine_streamid_main"] = streamId //6.9以上的版本不需要设置streamId
        ucObject["pure_audio_push_mod"] = 2
        val bussObject = JSONObject()
        bussObject["Str_uc_params"] = ucObject
        return bussObject.toString()

    }

    private fun create3AInfo(api: String): String {
        val paramsObject = JSONObject()
        paramsObject["enable"] = 1

        val callObject = JSONObject()
        callObject["api"] = api
        callObject["params"] = paramsObject

        SonaLogger.print(callObject.toString())
        return callObject.toString()
    }

    private fun pushStreamStep2(streamId: String?, componentCallback: ComponentCallback?) {
        if (mStream?.isPushStreamSuccess() == true) {
            componentCallback?.executeSuccess()
            return
        }
        mStream?.providerStream(SteamType.LOCAL, AudioStream(streamId ?: "", "", ""))
        mTRTCCloud?.startPublishing(streamId, 0)
        mTRTCCloudListener?.switchRoleObserver = object : TRTCCloudListener.Observer {
            override fun onSuccess() {
                mStream?.startPublishStream(streamId!!)
                componentCallback?.executeSuccess()
            }

            override fun onError(errorCode: Int) {
                mStream?.providerStream(SteamType.LOCAL, null)
                componentCallback?.executeFailure(AudioError.PUSH_STREAM_ERROR, "推流失败")
                SonaLogger.log(
                    content = "转换角色失败",
                    code = AudioReportCode.TENCENT_SWITCH_ROLE_ERROR_CODE,
                    sdkCode = errorCode
                )
            }
        }

        mTRTCCloud?.switchRole(TRTCCloudDef.TRTCRoleAnchor)
    }

    private fun setVolumeType() {
        var mediaType = true
        acquire(SonaRoomData::class.java)?.streamInfo?.switchSpeaker?.apply {
            if ("1" == this) {
                mediaType = false
            }
        }
        if (mediaType) {
            mTRTCCloud?.setSystemVolumeType(TRTCCloudDef.TRTCSystemVolumeTypeMedia)
            // 媒体音量下需要开启如下参数
            mTRTCCloud?.callExperimentalAPI(create3AInfo("enableAudioAGC"))
            mTRTCCloud?.callExperimentalAPI(create3AInfo("enableAudioANS"))
        } else {
            mTRTCCloud?.setSystemVolumeType(TRTCCloudDef.TRTCSystemVolumeTypeVOIP)
        }
    }

    inner class TRTCCloudListenerImpl : TRTCCloudListener() {

        override fun onStartPublishing(errCode: Int, errMsg: String?) {
            if (errCode == 0) {
                SonaLogger.log(
                    content = "推流回调成功",
                    code = AudioReportCode.TENCENT_PUSH_STREAM_SUCCESS_CODE
                )
            } else {
                SonaLogger.log(
                    content = "推流回调失败",
                    reason = errMsg ?: "",
                    code = AudioReportCode.TENCENT_PUSH_STREAM_FAIL_CODE,
                    sdkCode = errCode
                )
            }
        }

        override fun onStopPublishing(errCode: Int, errMsg: String?) {
            if (errCode == 0) {
                SonaLogger.log(
                    content = "停止推流回调成功",
                    code = AudioReportCode.TENCENT_STOP_PUSH_STREAM_SUCCESS_CODE
                )
            } else {
                SonaLogger.log(
                    content = "停止推流回调失败",
                    reason = errMsg ?: "",
                    code = AudioReportCode.TENCENT_STOP_PUSH_STREAM_FAIL_CODE,
                    sdkCode = errCode
                )
            }
        }

        /**
         * 是否有音频上行的回调
         * 您可以根据您的项目要求，设置相关的 UI 逻辑，比如显示对端闭麦的图标等
         *
         * @param userId    用户标识
         * @param available true：音频可播放，false：音频被关闭
         */
        override fun onUserAudioAvailable(userId: String?, available: Boolean) {
            if (available) {
                userId?.let {
                    val audioStream = AudioStream(userId, userId, "")
                    mStream?.providerStream(SteamType.AREMOTE, mutableListOf(audioStream))
                    dispatchMessage(ComponentMessage.AUDIO_REV_ADD_STREAM, audioStream)
                }
            } else {
                userId?.let {
                    val audioStream = AudioStream(userId, userId, "")
                    mStream?.providerStream(SteamType.DREMOTE, mutableListOf(audioStream))
                    dispatchMessage(ComponentMessage.AUDIO_REV_REMOVE_STREAM, audioStream)
                }
            }
            SonaLogger.log(
                content = "房间内流变化 $userId",
                code = AudioReportCode.TENCENT_STREAM_CHANGE,
                sdkCode = if (available) 2001 else 2002
            )
        }

        /**
         * 音量大小回调
         * 您可以用来在 UI 上显示当前用户的声音大小，提高用户体验
         *
         * @param userVolumes 所有正在说话的房间成员的音量（取值范围0 - 100）。即 userVolumes 内仅包含音量不为0（正在说话）的用户音量信息。其中本地进房 userId 对应的音量，表示 local 的音量，也就是自己的音量。
         * @param totalVolume 所有远端成员的总音量, 取值范围 [0, 100]
         */
        override fun onUserVoiceVolume(
            userVolumes: ArrayList<TRTCCloudDef.TRTCVolumeInfo>?,
            totalVolume: Int
        ) {
            val audioConfig = acquire(AudioConfig::class.java)
            if (audioConfig == null || !audioConfig.soundCapture) return
            if (userVolumes != null) {
                val soundLevelInfoList = ArrayList<SoundLevelInfoEntity>()
                for (volume in userVolumes) {
                    if (volume.volume > 0) {
                        soundLevelInfoList.add(
                            SoundLevelInfoEntity(
                                volume.userId,
                                volume.volume.toFloat()
                            )
                        )
                    }
                }
                if (soundLevelInfoList.isNotEmpty()) {
                    dispatchMessage(ComponentMessage.AUDIO_REV_SOUND_LEVEL_INFO, soundLevelInfoList)
                }
            }
        }

        /**
         * 连接断开，内部会自动重连
         */
        override fun onConnectionLost() {
            dispatchMessage(ComponentMessage.AUDIO_DISCONNECT, null)
            SonaLogger.log(
                content = "房间断开",
                code = AudioReportCode.TENCENT_DISCONNECT_CODE
            )
        }

        /**
         * 连接重连成功
         */
        override fun onConnectionRecovery() {
            dispatchMessage(ComponentMessage.AUDIO_RECONNECT, null)
            SonaLogger.log(
                content = "房间重连",
                code = AudioReportCode.TENCENT_RECONNECT_CODE
            )
        }

        override fun onFirstAudioFrame(userId: String?) {
            userId ?: return
            val beginTime = pullStreamStartTimeMap[userId]
            beginTime ?: return
            val endTime = System.currentTimeMillis()
            val costTime = endTime - beginTime
            if (costTime <= 0) return
            val ext: HashMap<String?, String?> = HashMap()
            ext[ReportEvent.KEY_BEGIN_TIME] = "$beginTime"
            ext[ReportEvent.KEY_END_TIME] = "$endTime"
            ext[ReportEvent.KET_DURATION] = "$costTime"
            val sonaReportEvent = SonaReportEvent.Builder()
                .setContent("pull stream userId $userId cost time $costTime")
                .setExt(ext)
                .setType(SonaReportEvent.LOG or SonaReportEvent.LOGAN)
                .build()
            SonaReport.report(sonaReportEvent)
        }

        override fun onRemoteUserEnterRoom(userId: String?) {
            if (userId.isNullOrEmpty()) return
            pullStreamStartTimeMap[userId] = System.currentTimeMillis()
            userList.add(userId)
        }

        override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
            if (userId.isNullOrEmpty()) return
            userList.remove(userId)
            if (userList.isEmpty()) {
                SonaConfigManager.getInstance().getConfig()
            }
        }

        /**
         * ERROR 大多是不可恢复的错误，需要通过 UI 提示用户
         * 然后执行退房操作
         *
         * @param errCode   错误码 TXLiteAVError
         * @param errMsg    错误信息
         * @param extraInfo 扩展信息字段，个别错误码可能会带额外的信息帮助定位问题
         */
        override fun onError(errCode: Int, errMsg: String?, extraInfo: Bundle?) {
            dispatchMessage(ComponentMessage.AUDIO_ERROR, errCode)
            SonaLogger.log(
                content = "房间出错",
                reason = "$errMsg",
                sdkCode = errCode,
                code = AudioReportCode.TENCENT_DISCONNECT_ERROR_CODE
            )
        }
    }

    private fun reportLoginCostTime(startLoginTime: Long) {
        val endTime = System.currentTimeMillis()
        val loginCostTime: Long = endTime - startLoginTime
        if (loginCostTime <= 0) return
        val ext: HashMap<String?, String?> = HashMap()
        ext[ReportEvent.KEY_BEGIN_TIME] = startLoginTime.toString()
        ext[ReportEvent.KEY_END_TIME] = endTime.toString()
        ext[ReportEvent.KET_DURATION] = loginCostTime.toString()
        val sonaReportEvent = SonaReportEvent.Builder()
            .setContent("login tencent room cost time $loginCostTime")
            .setExt(ext)
            .setType(SonaReportEvent.LOG or SonaReportEvent.LOGAN)
            .build()
        SonaReport.report(sonaReportEvent)
    }

    inner class TRTCAudioFrameListenerImpl : TRTCAudioFrameListener {
        /**
         * 本地麦克风采集到的音频数据回调
         * 此接口回调出的音频数据包含背景音、音效、混响等前处理效果
         * 请不要在此回调函数中做任何耗时操作，建议直接拷贝到另一线程进行处理，否则会导致各种声音问题
         *
         * @param frame 音频数据
         */
        override fun onCapturedRawAudioFrame(frame: TRTCCloudDef.TRTCAudioFrame?) {

        }

        /**
         * 将各路待播放音频混合之后并在最终提交系统播放之前的数据回调
         *
         * 当您设置完音频数据自定义回调之后，SDK 内部会把各路待播放的音频混合之后的音频数据，在提交系统播放之前，以 PCM 格式的形式通过本接口回调给您。
         *
         * 此接口回调出的音频时间帧长固定为0.02s，格式为 PCM 格式。
         * 由时间帧长转化为字节帧长的公式为【采样率 × 时间帧长 × 声道数 × 采样点位宽】。
         * 以 TRTC 默认的音频录制格式48000采样率、单声道、16采样点位宽为例，字节帧长为【48000 × 0.02s × 1 × 16bit = 15360bit = 1920字节】。
         * 参数
         * frame	PCM 格式的音频数据帧
         * 注意
         * 请不要在此回调函数中做任何耗时操作，由于 SDK 每隔 20ms 就要处理一帧音频数据，如果您的处理时间超过 20ms，就会导致声音异常。
         * 此接口回调出的音频数据是可读写的，也就是说您可以在回调函数中同步修改音频数据，但请保证处理耗时。
         * 此接口回调出的是对各路待播放音频数据的混合，但其中并不包含耳返的音频数据。
         */
        override fun onMixedPlayAudioFrame(frame: TRTCCloudDef.TRTCAudioFrame?) {

        }

        override fun onMixedAllAudioFrame(p0: TRTCCloudDef.TRTCAudioFrame?) {
        }

        override fun onRemoteUserAudioFrame(p0: TRTCCloudDef.TRTCAudioFrame?, p1: String?) {

        }

        override fun onLocalProcessedAudioFrame(frame: TRTCCloudDef.TRTCAudioFrame?) {
            frame ?: return
            dispatchMessage(ComponentMessage.AUDIO_RECEIVE_FRAME, AudioMixBuffer(frame.data))
        }
    }

}