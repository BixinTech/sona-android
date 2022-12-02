package cn.bixin.sona.component.internal.audio;

/**
 * 音频错误码
 *
 * @Author luokun
 * @Date 2020/6/8
 */
public interface AudioReportCode {

    /****************** 即构 **********************/
    /**
     * 即构流增减
     */
    int ZEGO_STREAM_CHANGE = 11000;
    /**
     * 即构SDK初始化成功
     */
    int ZEGO_INIT_SDK_SUCCESS_CODE = 11001;
    /**
     * 即构SDK初始化失败
     */
    int ZEGO_INIT_SDK_FAIL_CODE = -11001;
    /**
     * 即构SDK反初始化成功
     */
    int ZEGO_UN_INIT_SDK_SUCCESS_CODE = 11002;
    /**
     * 即构SDK反初始化失败
     */
    int ZEGO_UN_INIT_SDK_FAIL_CODE = -11002;
    /**
     * 即构推流成功
     */
    int ZEGO_PUSH_STREAM_SUCCESS_CODE = 11003;
    /**
     * 即构推流失败
     */
    int ZEGO_PUSH_STREAM_FAIL_CODE = -11003;
    /**
     * 即构推流回调成功
     */
    int ZEGO_PUSH_STREAM_BACK_SUCCESS_CODE = 11004;
    /**
     * 即构推流回调失败
     */
    int ZEGO_PUSH_STREAM_BACK_FAIL_CODE = -11004;
    /**
     * 即构停止推流成功
     */
    int ZEGO_STOP_PUSH_STREAM_SUCCESS_CODE = 11005;
    /**
     * 即构停止推流失败
     */
    int ZEGO_STOP_PUSH_STREAM_FAIL_CODE = -11005;
    /**
     * 即构拉流成功
     */
    int ZEGO_PULL_STREAM_SUCCESS_CODE = 11006;
    /**
     * 即构拉流失败
     */
    int ZEGO_PULL_STREAM_FAIL_CODE = -11006;
    /**
     * 即构拉流回调成功
     */
    int ZEGO_PULL_STREAM_BACK_SUCCESS_CODE = 11007;
    /**
     * 即构拉流回调失败
     */
    int ZEGO_PULL_STREAM_BACK_FAIL_CODE = -11007;
    /**
     * 即构停止拉流成功
     */
    int ZEGO_STOP_PULL_STREAM_SUCCESS_CODE = 11008;
    /**
     * 即构停止拉流失败
     */
    int ZEGO_STOP_PULL_STREAM_FAIL_CODE = -11008;
    /**
     * 即构登录房间成功
     */
    int ZEGO_LOGIN_CODE = 11009;
    /**
     * 即构登录房间成功
     */
    int ZEGO_LOGIN_SUCCESS_CODE = 11010;
    /**
     * 即构登录房间失败
     */
    int ZEGO_LOGIN_FAIL_CODE = -11010;
    /**
     * 即构退出登录房间成功
     */
    int ZEGO_LOGIN_OUT_SUCCESS_CODE = 11011;
    /**
     * 即构退出登录房间失败
     */
    int ZEGO_LOGIN_OUT_FAIL_CODE = -11011;
    /**
     * 即构操作麦克风成功
     */
    int ZEGO_MIC_SUCCESS_CODE = 11012;
    /**
     * 即构操作麦克风失败
     */
    int ZEGO_MIC_FAIL_CODE = -11012;
    /**
     * 即构设置用户成功
     */
    int ZEGO_SET_USER_SUCCESS_CODE = 11013;
    /**
     * 即构设置用户失败
     */
    int ZEGO_SET_USER_FAIL_CODE = -11013;
    /**
     * 即构操作音量成功
     */
    int ZEGO_SET_VOLUME_SUCCESS_CODE = 11014;
    /**
     * 即构操作音量失败
     */
    int ZEGO_SET_VOLUME_FAIL_CODE = -11014;
    /**
     * 即构断开连接
     */
    int ZEGO_DISCONNECT_CODE = -11015;
    /**
     * 即构重新连接
     */
    int ZEGO_RECONNECT_CODE = 11015;
    /**
     * 即构彻底断开连接
     */
    int ZEGO_DISCONNECT_ERROR_CODE = -11016;

    /**
     * 即构打开/关闭免提成功
     */
    int ZEGO_HANDSFREE_SUCCESS_CODE = 11017;
    /**
     * 即构打开/关闭免提失败
     */
    int ZEGO_HANDSFREE_ERROR_CODE = -11017;
    /**
     * 即构转换角色失败
     */
    int ZEGO_SWITCH_ROLE_ERROR_CODE = -11018;
    /**
     * 即构播放音乐失败
     */
    int ZEGO_MUSIC_PLAY_ERROR_CODE = -11019;

    /**
     * 即构拉混流成功
     */
    int ZEGO_PULL_MIX_STREAM_SUCCESS_CODE = 11027;
    /**
     * 即构拉混流失败
     */
    int ZEGO_PULL_MIX_STREAM_FAIL_CODE = -11027;

    /**
     * 即构停止拉混流成功
     */
    int ZEGO_STOP_PULL_MIX_STREAM_SUCCESS_CODE = 11028;
    /**
     * 即构停止拉混流失败
     */
    int ZEGO_STOP_PULL_MIX_STREAM_FAIL_CODE = -11028;

    /**
     * 即构互踢
     */
    int ZEGO_KICK_USER = -11029;

    /** 特有的事件 **/
    /**
     * 直播音频断开事件
     */
    int ZEGO_PLAY_BREAK_EVENT = 11100;

    /**
     * 直播音频断开事件
     */
    int ZEGO_PLAY_BREAK_END_EVENT = 11101;

    /****************** 腾讯 **********************/
    /**
     * 即构流增减
     */
    int TENCENT_STREAM_CHANGE = 12000;
    /**
     * 腾讯SDK初始化成功
     */
    int TENCENT_INIT_SDK_SUCCESS_CODE = 12001;
    /**
     * 腾讯SDK初始化失败
     */
    int TENCENT_INIT_SDK_FAIL_CODE = -12001;
    /**
     * 腾讯SDK反初始化成功
     */
    int TENCENT_UN_INIT_SDK_SUCCESS_CODE = 12002;
    /**
     * 腾讯SDK反初始化失败
     */
    int TENCENT_UN_INIT_SDK_FAIL_CODE = -12002;
    /**
     * 腾讯推流成功
     */
    int TENCENT_PUSH_STREAM_SUCCESS_CODE = 12003;
    /**
     * 腾讯推流失败
     */
    int TENCENT_PUSH_STREAM_FAIL_CODE = -12003;
    /**
     * 腾讯推流回调成功
     */
    int TENCENT_PUSH_STREAM_BACK_SUCCESS_CODE = 12004;
    /**
     * 腾讯推流回调失败
     */
    int TENCENT_PUSH_STREAM_BACK_FAIL_CODE = -12004;
    /**
     * 腾讯停止推流成功
     */
    int TENCENT_STOP_PUSH_STREAM_SUCCESS_CODE = 12005;
    /**
     * 腾讯停止推流失败
     */
    int TENCENT_STOP_PUSH_STREAM_FAIL_CODE = -12005;

    /**
     * 腾讯拉流成功
     */
    int TENCENT_PULL_STREAM_SUCCESS_CODE = 12006;
    /**
     * 腾讯拉流失败
     */
    int TENCENT_PULL_STREAM_FAIL_CODE = -12006;

    /**
     * 腾讯停止拉流成功
     */
    int TENCENT_STOP_PULL_STREAM_SUCCESS_CODE = 12008;
    /**
     * 腾讯停止拉流失败
     */
    int TENCENT_STOP_PULL_STREAM_FAIL_CODE = -12008;
    /**
     * 腾讯登录房间成功
     */
    int TENCENT_LOGIN_SUCCESS_CODE = 12010;
    /**
     * 腾讯登录房间失败
     */
    int TENCENT_LOGIN_FAIL_CODE = -12010;
    /**
     * 腾讯退出登录房间成功
     */
    int TENCENT_LOGIN_OUT_SUCCESS_CODE = 12011;
    /**
     * 腾讯退出登录房间失败
     */
    int TENCENT_LOGIN_OUT_FAIL_CODE = -12011;
    /**
     * 腾讯操作麦克风成功
     */
    int TENCENT_MIC_SUCCESS_CODE = 12012;
    /**
     * 腾讯操作麦克风失败
     */
    int TENCENT_MIC_FAIL_CODE = -12012;
    /**
     * 腾讯操作音量成功
     */
    int TENCENT_SET_VOLUME_SUCCESS_CODE = 12014;
    /**
     * 腾讯操作音量失败
     */
    int TENCENT_SET_VOLUME_FAIL_CODE = -12014;
    /**
     * 腾讯断开连接
     */
    int TENCENT_DISCONNECT_CODE = -12015;
    /**
     * 腾讯重新连接
     */
    int TENCENT_RECONNECT_CODE = 12015;
    /**
     * 腾讯连接出错
     */
    int TENCENT_DISCONNECT_ERROR_CODE = -12016;
    /**
     * 腾讯打开/关闭免提成功
     */
    int TENCENT_HANDSFREE_SUCCESS_CODE = 12017;
    /**
     * 腾讯打开/关闭免提失败
     */
    int TENCENT_HANDSFREE_ERROR_CODE = -12017;
    /**
     * 腾讯转换角色失败
     */
    int TENCENT_SWITCH_ROLE_ERROR_CODE = -12018;
    /**
     * 腾讯播放音乐失败
     */
    int TENCENT_MUSIC_PLAY_ERROR_CODE = -12019;
    /**
     * 腾讯拉混流成功
     */
    int TENCENT_PULL_MIX_STREAM_SUCCESS_CODE = 12027;
    /**
     * 腾讯拉混流失败
     */
    int TENCENT_PULL_MIX_STREAM_FAIL_CODE = -12027;
    /**
     * 腾讯停止拉混流成功
     */
    int TENCENT_STOP_PULL_MIX_STREAM_SUCCESS_CODE = 12028;
    /**
     * 腾讯停止拉混流失败
     */
    int TENCENT_STOP_PULL_MIX_STREAM_FAIL_CODE = -12028;

    /** 特有事件 **/
    /**
     * 腾讯混流播放事件
     */
    int TENCENT_MIX_PLAY_EVENT_CODE = 12100;

    /****************其他问题********************/
    /** 基础问题 **/
    /**
     * 音频配置问题
     */
    int AUDIO_SETTING_ERROR_CODE = -18001;
}