package cn.bixin.sona.component.internal.audio

/**
 *
 * @Author luokun
 * @Date 2020-03-03
 */
interface IStreamAcquire {

    /**
     * 获取流
     *
     * @param type
     * @return
     */
    fun acquireStream(type: SteamType): Any?

}