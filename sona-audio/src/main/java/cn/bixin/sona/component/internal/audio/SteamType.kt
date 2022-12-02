package cn.bixin.sona.component.internal.audio

/**
 *
 * @Author luokun
 * @Date 2020-03-04
 */
enum class SteamType {
    REMOTE, // 远端的流
    MIX, // 混流
    LOCAL, // 推的流
    AREMOTE, // 增加远端的流
    DREMOTE, // 减少远端的流
}