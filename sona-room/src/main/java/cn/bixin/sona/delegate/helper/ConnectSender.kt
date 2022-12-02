package cn.bixin.sona.delegate.helper

/**
 *
 * @Author luokun
 * @Date 2020/4/22
 */
interface ConnectSender {

    fun send(message: String, callback: (code: Int, msg: String) -> Unit)
}