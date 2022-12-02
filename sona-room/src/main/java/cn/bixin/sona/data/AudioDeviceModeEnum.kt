package cn.bixin.sona.data

enum class AudioDeviceModeEnum {

    /**
     * 通话模式, 开启系统前处理模式(包括回声消除、噪声抑制、音量增益).
     * 该模式下麦后释放麦克风，切回媒体音量
     */
    COMMUNICATION3,

    /**
     * 普通模式, 始终关闭系统前处理.
     */
    GENERAL

}