package cn.bixin.sona.component.audio;

public class AudioError {
    // 错误码 60001 - 69999
    /**
     * 推流调用失败
     */
    public static final int PUSH_STREAM_ERROR = 60001;
    /**
     * 开关/关闭麦克风失败
     */
    public static final int SWITCH_MIC_ERROR = 60002;
    /**
     * 停止推流失败
     */
    public static final int STOP_PUSH_STREAM_ERROR = 60003;
    /**
     * 静音/取消静音失败
     */
    public static final int SILENT_ERROR = 60004;
    /**
     * 拉单流失败
     */
    public static final int PULL_STREAM_ERROR = 60005;
    /**
     * 停止拉流失败
     */
    public static final int STOP_PULL_STREAM_ERROR = 60006;
    /**
     * 登录房间失败
     */
    public static final int LOGIN_ROOM_ERROR = 60007;
    /**
     * 打开/关闭免提失败
     */
    public static final int SWITCH_HANDSFREE_ERROR = 60008;
    /**
     * 拉混流失败
     */
    public static final int PULL_MIX_STREAM_ERROR = 60009;

}
