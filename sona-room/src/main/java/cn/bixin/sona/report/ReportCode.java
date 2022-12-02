package cn.bixin.sona.report;

/**
 *
 * @Author luokun
 * @Date 2020-01-03
 */

public interface ReportCode {

    /**
     * 长连开始进入聊天室
     */
    int MERCURY_ENTER_CHATROOM_START_CODE = 22001;

    /**
     * 长连进入聊天室成功
     */
    int MERCURY_ENTER_CHATROOM_SUCCESS_CODE = 22002;
    /**
     * 长连进入聊天室失败
     */
    int MERCURY_ENTER_CHATROOM_FAIL_CODE = -22002;

    /**
     * 长连断开
     */
    int MERCURY_DISCONNECT_CODE = -22004;
    /**
     * 长连重连
     */
    int MERCURY_RECONNECT_CODE = 22004;

    /**
     * 长连用户被踢
     */
    int MERCURY_USER_KICK_CODE = 22005;

    /**
     * 长连login
     */
    int MERCURY_LOGIN = 22006;
    /**
     * 长连开始重连
     */
    int MERCURY_RECONNECT_START_CODE = 22007;
    /**
     * 长连重连失败
     */
    int MERCURY_RECONNECT_FAIL_CODE = -22007;

    /**收到重复消息**/
    int RECEIVE_DUPLICATE_MESSAGE_CODE = -40001;

    /****************** 房间 **********************/
    /**
     * 进入房间成功
     */
    int ENTER_ROOM_SUCCESS_CODE = 31001;
    /**
     * 进入房间失败
     */
    int ENTER_ROOM_FAIL_CODE = -31001;

    /** 房间状态不对 **/
    int ROOM_STATUS_FAIL_CODE = -32001;

    /** 创建流失败 **/
    int ROOM_GENERATE_STREAM_CODE = -32002;

    /** 音频热切 **/
    int ROOM_AUDIO_SWITCH_CODE = 32003;

    /** 音频监控 **/
    int ROOM_AUDIO_MONITOR_CODE = 32004;

    /** im重连 **/
    int ROOM_IM_RECONNECT_CODE = 32006;

    /**进入房间异常**/
    int ENTER_ROOM_EXCEPTION_CODE = -31003;
}