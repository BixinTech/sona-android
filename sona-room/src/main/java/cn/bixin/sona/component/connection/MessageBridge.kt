package cn.bixin.sona.component.connection

/**
 *
 * @Author luokun
 * @Date 2020/4/16
 */
interface MessageBridge<in T> {

    fun filterMessage(message: T?) : Boolean
}