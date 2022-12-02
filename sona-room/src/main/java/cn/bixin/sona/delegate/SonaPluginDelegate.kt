package cn.bixin.sona.delegate

import cn.bixin.sona.api.ApiRegister
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.plugin.entity.PluginEnum

abstract class SonaPluginDelegate : ApiRegister() {
    /**
     * 分发消息
     *
     * @param msgType
     * @param message
     */
    abstract fun handleMessage(msgType: ComponentMessage?, message: Any?)

    /**
     * plugin类型
     *
     * @return
     */
    abstract fun pluginType(): PluginEnum?

    /**
     * 释放
     */
    open fun remove() {
        clear()
    }
}