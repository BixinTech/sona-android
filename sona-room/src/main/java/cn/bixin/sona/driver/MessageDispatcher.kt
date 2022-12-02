package cn.bixin.sona.driver

import cn.bixin.sona.component.ComponentMessage

/**
 * 消息分发器
 *
 * @Author luokun
 * @Date 2020/3/25
 */
interface MessageDispatcher {

    /**
     * 分发消息
     *
     * @param msgType 消息类型
     * @param message 消息体
     */
    fun dispatchMessage(msgType: ComponentMessage, message: Any?)
}