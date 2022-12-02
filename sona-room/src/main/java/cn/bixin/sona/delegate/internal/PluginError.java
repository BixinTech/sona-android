package cn.bixin.sona.delegate.internal;

public class PluginError {

    // 错误码 50000 - 59999
    /**
     * 房间参数错误
     */
    public static final int ROOM_PARAM_ERROR = 50000;
    /**
     * 房间状态不对
     */
    public static final int ROOM_STATUS_ERROR = 50001;
    /**
     * 设置管理员失败
     */
    public static final int SERVER_SET_ADMIN_ERROR = 50002;
    /**
     * 取消设置管理员失败
     */
    public static final int SERVER_CANCEL_ADMIN_ERROR = 50003;
    /**
     * 拉黑失败
     */
    public static final int SERVER_SET_BLOCK_ERROR = 50004;
    /**
     * 取消拉黑失败
     */
    public static final int SERVER_CANCEL_BLOCK_ERROR = 50005;
    /**
     * 禁言失败
     */
    public static final int SERVER_SET_MUTE_ERROR = 50006;
    /**
     * 取消禁言失败
     */
    public static final int SERVER_CANCEL_MUTE_ERROR = 50007;
    /**
     * 禁音失败
     */
    public static final int SERVER_SET_SILENT_ERROR = 50008;
    /**
     * 取消禁音失败
     */
    public static final int SERVER_CANCEL_SILENT_ERROR = 50009;
    /**
     * 踢人失败
     */
    public static final int SERVER_KICK_ERROR = 50010;
    /**
     * 生产流失败
     */
    public static final int SERVER_GENERATE_STREAM_ERROR = 50011;
    /**
     * 发送消息失败
     */
    public static final int SERVER_SEND_MSG_ERROR = 50012;
    /**
     * 进入房间失败
     */
    public static final int SERVER_ENTER_ROOM_ERROR = 50013;
    /**
     * 创建房间失败
     */
    public static final int SERVER_CREATE_ROOM_ERROR = 50014;
    /**
     * 更改密码失败
     */
    public static final int SERVER_UPDATE_PASSWORD_ERROR = 50015;
    /**
     * 组件初始化失败
     */
    public static final int ROOM_COMPONENT_INIT_ERROR = 50016;
    /**
     * 音频组件重连失败
     */
    public static final int ROOM_COMPONENT_AUDIO_RECONNECT_ERROR = 50017;
    /**
     * 长连组件重连失败
     */
    public static final int ROOM_COMPONENT_CONNECT_RECONNECT_ERROR = 50018;
    /**
     * 离开房间失败
     */
    public static final int ROOM_LEAVE_ROOM_ERROR = 50019;
    /**
     * 关闭房间失败
     */
    public static final int ROOM_CLOSE_ROOM_ERROR = 50020;
    /**
     * 在线人员列表错误
     */
    public static final int SERVER_ONLINE_LIST_ERROR = 50021;
    /**
     * 在线人员数量错误
     */
    public static final int SERVER_ONLINE_COUNT_ERROR = 50022;
    /**
     * 设置某些人能听到某些人说话错误
     */
    public static final int SERVER_SPECIFIC_ERROR = 50023;
    /**
     * 设置某些人不能听到某些人说话错误
     */
    public static final int SERVER_SPECIFIC_NOT_ERROR = 50024;
    /**
     * 调用参数错误
     */
    public static final int CALL_PARAM_ERROR = 50025;

}
