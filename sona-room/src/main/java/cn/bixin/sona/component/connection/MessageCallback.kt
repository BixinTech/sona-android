package cn.bixin.sona.component.connection

/**
 *
 * @Author luokun
 * @Date 2020/4/17
 */
interface MessageCallback {

    /**
     * 接收到消息
     * @param response
     */
    fun onResponse(response: ConnectionMessage)

    /**
     * 标记消息是否需要ack
     */
    fun setMessageAck(isAck: Boolean)
}