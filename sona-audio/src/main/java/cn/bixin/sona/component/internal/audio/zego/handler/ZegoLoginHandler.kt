package cn.bixin.sona.component.internal.audio.zego.handler

import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.audio.AudioError
import cn.bixin.sona.component.internal.audio.AudioReportCode
import cn.bixin.sona.component.internal.audio.SteamType
import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.util.SonaLogger
import com.zego.zegoliveroom.constants.ZegoConstants
import com.zego.zegoliveroom.entity.ZegoStreamInfo

/**
 * Zego登录
 */
class ZegoLoginHandler(component: ZegoAudio) : BaseZegoHandler(component) {

    fun login(roomId: String, componentCallback: ComponentCallback?) {
        SonaLogger.log(content = "开始登录房间roomId = $roomId")
        val startLoginTime = System.currentTimeMillis()
        getLiveRoom()?.loginRoom(
            roomId,
            ZegoConstants.RoomRole.Anchor
        ) { code: Int, streams: Array<ZegoStreamInfo>? ->
            if (code == 0) {
                getReportHandler().reportLoginCostTime(startLoginTime)
                val sonaRoomData = acquire(SonaRoomData::class.java)
                if ("1" == sonaRoomData?.streamInfo?.switchSpeaker) {
                    getLiveRoom()?.setBuiltInSpeakerOn(false)
                }
                val multiStreams = ZegoStreamTransform.transform(streams)
                SonaLogger.print("login multiStreams size = " + multiStreams.size)
                getStreamHandler()?.providerStream(
                    SteamType.REMOTE,
                    multiStreams
                )
                getLiveRoom()?.enableMic(getComponent().enableMic)
                componentCallback?.executeSuccess()
                SonaLogger.log(
                    content = "登录房间成功 房间内有${multiStreams.size}条流",
                    code = AudioReportCode.ZEGO_LOGIN_SUCCESS_CODE
                )
            } else {
                SonaLogger.log(
                    content = "登录房间失败",
                    code = AudioReportCode.ZEGO_LOGIN_FAIL_CODE,
                    sdkCode = code
                )
                componentCallback?.executeFailure(
                    AudioError.LOGIN_ROOM_ERROR,
                    "登录房间失败"
                )
            }
        }
    }

}