package cn.bixin.sona.delegate.observer

import cn.bixin.sona.plugin.PluginCallback

/**
 * 音频重连
 *
 * @Author luokun
 * @Date 2020/8/21
 */
class AudioReconnectObserver(private var callback: PluginCallback?) : PluginCallback {

    override fun onSuccess() {
        callback?.onSuccess()
    }

    override fun onFailure(code: Int, reason: String?) {
        callback?.onFailure(code, reason)
    }
}