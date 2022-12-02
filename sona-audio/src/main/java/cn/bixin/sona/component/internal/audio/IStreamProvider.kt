package cn.bixin.sona.component.internal.audio

/**
 *
 * @Author luokun
 * @Date 2020-03-04
 */
interface IStreamProvider {

    /**
     * 提供流
     *
     * @param type
     * @param stream
     */
    fun providerStream(type: SteamType, stream: Any?)
}