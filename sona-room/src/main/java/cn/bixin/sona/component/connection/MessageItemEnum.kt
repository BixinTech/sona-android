package cn.bixin.sona.component.connection

enum class MessageItemEnum(val value: Int) {

    UNKNOWN(-1),

    /** 自定义消息 **/
    CUSTOM(100),

    /** 文本消息 **/
    TXT(101),

    /** 图片消息 **/
    IMAGE(102),

    /** emoji表情消息 **/
    EMOJI(103),

    /** 音频消息 **/
    AUDIO(104),

    /** 视频消息 **/
    VIDEO(105),

    /**
     * 消息ack
     */
    ACK(106),

    /** 创建房间 **/
    CREATE_ROOM(10000),

    /** 关闭房间 **/
    CLOSE_ROOM(10001),

    /** 进入房间 **/
    ENTER_ROOM(10002),

    /** 离开房间 **/
    LEAVE_ROOM(10003),

    /** 设置、取消管理员 **/
    ADMIN_SET_CANCEL(10004),

    /** 拉黑、取消拉黑 **/
    BLACK_SET_CANCEL(10005),

    /** 禁言、取消禁言 **/
    MUTE_SET_CANCEL(10006),

    /** 踢出房间 **/
    KICK(10007),

    /** 音频热切 **/
    AUDIO_HOT_SWITCH(10008),

    /** 静音流 **/
    STREAM_SILENT_SET_CANCEL(10009);

    companion object Message {
        fun messageType(value: Int): MessageItemEnum {
            val values = values()
            values.forEach {
                if (it.value == value) {
                    return it
                }
            }
            return UNKNOWN
        }
    }

}