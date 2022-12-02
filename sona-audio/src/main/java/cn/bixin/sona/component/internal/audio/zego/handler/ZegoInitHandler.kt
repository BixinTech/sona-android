package cn.bixin.sona.component.internal.audio.zego.handler

import android.os.Build
import android.text.TextUtils
import cn.bixin.sona.component.internal.audio.AudioReportCode
import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.component.internal.audio.zego.ZegoContextObserver
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.data.entity.UserData
import cn.bixin.sona.util.SonaLogger
import com.zego.zegoavkit2.audiodevice.ZegoAudioDevice
import com.zego.zegoliveroom.ZegoLiveRoom
import com.zego.zegoliveroom.constants.ZegoAvConfig
import com.zego.zegoliveroom.constants.ZegoConstants

/**
 * Zego初始化
 */
class ZegoInitHandler(component: ZegoAudio) : BaseZegoHandler(component) {

    /**
     * 初始化用户配置
     */
    fun initUserConfig() {
        val userData = acquire(UserData::class.java)
        var uid = userData?.uid ?: ""
        var userName = ""
        if (TextUtils.isEmpty(uid)) {
            val ms = System.currentTimeMillis()
            uid = ms.toString()
        }
        if (TextUtils.isEmpty(userName)) {
            userName = "Android_" + Build.MODEL.replace(",".toRegex(), ".") + "_" + uid
        }

        // 设置用户信息
        val result = ZegoLiveRoom.setUser(uid, userName)
        SonaLogger.log(
            content = "设置用户信息uid = $uid , userName = $userName",
            code = if (result) AudioReportCode.ZEGO_SET_USER_SUCCESS_CODE else AudioReportCode.ZEGO_SET_USER_FAIL_CODE
        )
    }

    /**
     * 自定义高级功能
     */
    fun initAdvancedConfig() {
        val sonaRoomData = acquire(SonaRoomData::class.java) // 设置业务类型：0:直播、2:视频通话
        ZegoLiveRoom.setBusinessType(0)
        if ("1" == sonaRoomData?.streamInfo?.switchSpeaker) { // 设置音频设备模式: Auto:自动模式、Communication:通话模式、General:始终关闭回声消除
            ZegoLiveRoom.setAudioDeviceMode(ZegoConstants.AudioDeviceMode.Communication2)
        } else {
            ZegoLiveRoom.setAudioDeviceMode(ZegoConstants.AudioDeviceMode.General)
        }
        ZegoLiveRoom.setTestEnv(false)
        ZegoLiveRoom.setSDKContext(ZegoContextObserver())
        ZegoLiveRoom.setPlayQualityMonitorCycle(10000) // 设置拉流质量监控周期
    }

    /**
     * 初始化基本功能配置
     */
    fun initBasicConfig() {
        getLiveRoom()?.let {
            val sonaRoomData = acquire(SonaRoomData::class.java)
            val zegoAvConfig = ZegoAvConfig(ZegoAvConfig.Level.High) // 初始化设置级别为"High"
            ZegoAudioDevice.enableCaptureStereo(1)
            it.setLatencyMode(ZegoConstants.LatencyMode.Normal)
            it.setAudioChannelCount(2)
            val bitrate = sonaRoomData?.streamInfo?.bitrate ?: 0
            // 设置推流配置
            if (bitrate < 64000 || bitrate > 128000) {
                it.setAudioBitrate(64000)
            } else {
                it.setAudioBitrate(bitrate)
            }
            it.setAVConfig(zegoAvConfig) // 默认摄像头是打开状态，这里设置关闭
            it.enableCamera(false) // 回声消除开关
            it.enableAEC(true) // 音频采集自动增益开关
            it.enableAGC(true) // 音频采集噪声抑制开关
            it.enableNoiseSuppress(true)

            // 硬件编码
            ZegoLiveRoom.requireHardwareEncoder(false) // 硬件解码
            ZegoLiveRoom.requireHardwareDecoder(false) // 码率控制
            it.enableRateControl(false)
        }
    }

    /**
     * sdk初始化
     *
     * @return 初始化结果
     */
    fun initSdk(): Boolean {
        getLiveRoom()?.unInitSDK()
        ZegoLiveRoom.setConfig("av_retry_time=5")

        val sonaRoomData = acquire(SonaRoomData::class.java)
        if ("1" == sonaRoomData?.streamInfo?.switchSpeaker) {
            getLiveRoom()?.enableAECWhenHeadsetDetected(true)
        }

        if (sonaRoomData?.streamInfo == null) {
            SonaLogger.log(
                content = "初始化SDK",
                reason = "streamInfo = null",
                code = AudioReportCode.ZEGO_INIT_SDK_FAIL_CODE
            )
            return false
        }

        val appInfo = sonaRoomData.streamInfo.appInfo
        if (appInfo != null && appInfo.appId != 0 && !TextUtils.isEmpty(appInfo.appSign)) {
            try {
                val signSegment = appInfo.appSign?.split(",")?.toTypedArray()
                if (signSegment != null && signSegment.isNotEmpty()) {
                    val signByte = ByteArray(signSegment.size)
                    for (i in signSegment.indices) {
                        signByte[i] = signSegment[i].substring(2).toInt(16).toByte()
                    }
                    val isInit = getLiveRoom()?.initSDK(appInfo.appId.toLong(), signByte) ?: false
                    SonaLogger.log(
                        content = "初始化SDK",
                        code = if (isInit) AudioReportCode.ZEGO_INIT_SDK_SUCCESS_CODE else AudioReportCode.ZEGO_INIT_SDK_FAIL_CODE
                    )
                    return isInit
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                SonaLogger.log(
                    content = "初始化SDK",
                    reason = "${e.message}",
                    code = AudioReportCode.ZEGO_INIT_SDK_FAIL_CODE
                )
            }
        }
        return false
    }

    override fun unAssembling() {
        getLiveRoom()?.let {
            it.setAudioPrepCallback(null, null)
            it.enableAECWhenHeadsetDetected(false)
            it.setZegoLivePublisherCallback(null)
            it.setZegoLivePlayerCallback(null)
            it.setZegoRoomCallback(null)
            it.setZegoLiveEventCallback(null)
            it.enableAECWhenHeadsetDetected(false)
            val sonaRoomData = acquire(SonaRoomData::class.java)
            if ("1" == sonaRoomData?.streamInfo?.switchSpeaker) {
                it.setBuiltInSpeakerOn(false)
            }
            val logoutResult = it.logoutRoom()
            val unInitSdkResult = it.unInitSDK()

            SonaLogger.log(
                content = "退出房间",
                code = if (logoutResult) AudioReportCode.ZEGO_LOGIN_OUT_SUCCESS_CODE else AudioReportCode.ZEGO_LOGIN_OUT_FAIL_CODE
            )

            SonaLogger.log(
                content = "反初始化SDK",
                code = if (unInitSdkResult) AudioReportCode.ZEGO_UN_INIT_SDK_SUCCESS_CODE else AudioReportCode.ZEGO_UN_INIT_SDK_FAIL_CODE
            )
        } ?: let {
            SonaLogger.log(content = "ZegoInitHandler unAssembling liveRoom is null")
        }
    }
}