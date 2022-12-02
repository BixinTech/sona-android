package cn.bixin.sona.plugin;

import java.util.List;

import cn.bixin.sona.component.audio.AudioStream;
import cn.bixin.sona.data.AudioDeviceModeEnum;
import cn.bixin.sona.data.StreamSupplierEnum;
import cn.bixin.sona.plugin.anotation.SonaPluginAnnotation;
import cn.bixin.sona.plugin.config.AudioConfig;
import cn.bixin.sona.plugin.entity.PluginEnum;
import cn.bixin.sona.plugin.observer.AudioPluginObserver;

/**
 * 音频插件
 * 提供操作音频相关能力
 *
 * @author luokun
 */
@SonaPluginAnnotation(PluginEnum.AUDIO)
public interface AudioPlugin extends SonaPlugin<AudioConfig, AudioPluginObserver> {

    /**
     * 开始说话
     *
     * @param callback
     */
    void startSpeak(PluginCallback callback);

    /**
     * 停止说话
     *
     * @param callback
     */
    void stopSpeak(PluginCallback callback);

    /**
     * 开始听一条流的声音
     *
     * @param streamId
     * @param callback
     */
    void startListen(String streamId, PluginCallback callback);

    /**
     * 停止听一条流的声音
     *
     * @param streamId
     * @param callback
     */
    void stopListen(String streamId, PluginCallback callback);

    /**
     * 开始听所有流的声音
     *
     * @param callback
     */
    void startListen(PluginCallback callback);

    /**
     * 停止听所有流的声音
     *
     * @param callback
     */
    void stopListen(PluginCallback callback);

    /**
     * 静音
     *
     * @param streamId
     * @param on       true 为静音，false为取消静音
     * @param callback
     */
    void silent(String streamId, boolean on, PluginCallback callback);

    /**
     * 开关麦克风
     *
     * @param on       true: 开，false:关
     * @param callback
     */
    void switchMic(boolean on, PluginCallback callback);

    /**
     * 开关免提
     *
     * @param on       true: 开，false:关
     * @param callback
     */
    void switchHandsfree(boolean on, PluginCallback callback);

    /**
     * 当前房间流信息
     *
     * @return
     */
    List<AudioStream> currentStream();

    /**
     * 获取当前流的提供方
     *
     * @return
     */
    StreamSupplierEnum getStreamSupplier();

    /**
     * 开始重连
     *
     * @param pluginCallback
     */
    void startReconnect(PluginCallback pluginCallback);

    /**
     * 切换拉流模式
     *
     * @param realTime true：实时， false：CDN
     * @param callback
     */
    void switchListen(boolean realTime, PluginCallback callback);

    /**
     * 设置音频设备模式
     * <p>
     * 注意：
     * 1. 在推流预览前后调用均有效.
     * 2. 调用该接口会触发设备的启动切换，建议不要频繁调用，避免不必要的开销与硬件问题
     * 3. 调用本接口可能导致音量模式在 通话/媒体 间切换，若媒体音量和通话音量不一致，可能导致音量变化.
     */
    void setAudioDeviceMode(AudioDeviceModeEnum audioDeviceMode);
}
