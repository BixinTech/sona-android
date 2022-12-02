package cn.bixin.sona.plugin.entity

class MessageEntity(var type: MessageType, var msg: String?) {

    enum class MessageType(val value: Int) {
        /**
         * 自定义消息
         */
        CUSTOM(100),

        /**
         * 文本消息
         */
        TXT(101),

        /**
         * 图片消息
         */
        IMAGE(102),

        /**
         * emoji消息
         */
        EMOJI(103),

        /**
         * 音频消息
         */
        AUDIO(104),

        /**
         * 视频消息
         */
        VIDEO(105),

        /**
         * cmd消息
         */
        COMMAND(106);

        companion object Role {
            fun map(value: Int): MessageType {
                val values = values();
                values.forEach {
                    if (it.value == value) {
                        return it
                    }
                }
                return TXT
            }
        }
    }
}