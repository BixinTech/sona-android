package cn.bixin.sona.component.internal.audio.tencent

import cn.bixin.sona.util.SonaLogger
import com.tencent.trtc.TRTCCloudListener

/**
 *
 * @Author luokun
 * @Date 2020-03-04
 */
open class TRTCCloudListener : TRTCCloudListener() {

    /**
     * 进入房间的监听器，在回调回来之后会自动释放，避免泄漏
     */
    var enterObserver: Observer? = null

    /**
     * 转变角色的监听器，在回调回来之后会自动释放，避免泄漏
     */
    var switchRoleObserver: Observer? = null

    /**
     * 加入房间回调
     *
     * @param elapsed 加入房间耗时，单位毫秒
     */
    override fun onEnterRoom(elapsed: Long) {
        SonaLogger.print("onEnterRoom elapsed = $elapsed")
        if (elapsed >= 0) {
            enterObserver?.onSuccess()
        } else {
            enterObserver?.onError(0)
        }
        enterObserver = null
    }

    override fun onSwitchRole(errCode: Int, errMsg: String?) {
        SonaLogger.print("onSwitchRole errCode = $errCode")
        if (errCode == 0) {
            switchRoleObserver?.onSuccess()
        } else {
            switchRoleObserver?.onError(errCode)
        }
        switchRoleObserver = null
    }

    interface Observer {
        fun onSuccess()
        fun onError(errorCode: Int)
    }
}