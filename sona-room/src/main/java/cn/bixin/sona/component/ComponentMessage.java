package cn.bixin.sona.component;

public enum ComponentMessage {

    /**
     * 连接初始化
     **/
    CONNECT_INIT_SUCCESS,
    CONNECT_INIT_FAIL,

    /**
     * 音频初始化
     **/
    AUDIO_INIT_SUCCESS,
    AUDIO_INIT_FAIL,

    /**
     * 组件初始化
     **/
    COMPONENT_INIT_SUCCESS,
    COMPONENT_INIT_FAIL,

    /**
     * 长连断开
     **/
    CONNECT_DISCONNECT,

    /**
     * 长连重新连接
     **/
    CONNECT_RECONNECT,

    /**
     * 连接错误
     **/
    CONNECT_ERROR,

    /**
     * 收到消息
     **/
    CONNECT_REV_MESSAGE,

    /**
     * 音频断开
     **/
    AUDIO_DISCONNECT,

    /**
     * 音频重新连接
     **/
    AUDIO_RECONNECT,

    /**
     * 音频出现错误
     **/
    AUDIO_ERROR,

    /**
     * 收到流消息-增加流
     **/
    AUDIO_REV_ADD_STREAM,

    /**
     * 收到流消息-删除流
     **/
    AUDIO_REV_REMOVE_STREAM,

    /**
     * 麦上所有说话声音音量回传消息
     **/
    AUDIO_REV_SOUND_LEVEL_INFO,

    /**
     * 音频切换开始
     **/
    AUDIO_CHANGE_START,

    /**
     * 音频切换结束
     **/
    AUDIO_CHANGE_END,

    /**
     * IM切换开始
     **/
    IM_CHANGE_START,

    /**
     * IM音频切换结束
     **/
    IM_CHANGE_END,

    /**
     * 收到音频数据
     **/
    AUDIO_RECEIVE_FRAME,

    /**
     * 错误消息
     **/
    ERROR_MSG,

    /**
     * 房间踢人消息
     **/
    USER_KICK,

}
