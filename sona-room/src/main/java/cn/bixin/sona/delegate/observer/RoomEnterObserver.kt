package cn.bixin.sona.delegate.observer

import cn.bixin.sona.SonaRoomCallback

/**
 *
 * @Author luokun
 * @Date 2020-01-02
 */
class RoomEnterObserver(private var callback: SonaRoomCallback?) : SonaRoomCallback {

    override fun onSuccess(roomId: String) {
        callback?.onSuccess(roomId)
    }

    override fun onFailed(code: Int, reason: String?) {
        callback?.onFailed(code, reason)
    }
}