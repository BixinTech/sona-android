package cn.bixin.sona.plugin.observer;

import java.util.List;

import cn.bixin.sona.component.audio.AudioMixBuffer;
import cn.bixin.sona.plugin.entity.SoundLevelInfoEntity;
import cn.bixin.sona.plugin.entity.SpeakEntity;

public interface AudioPluginObserver extends PluginObserver {

    /**
     * 有人被静音、取消静音
     *
     * @param silent 1 静音， 0 取消静音
     * @param entity
     */
    void onSpeakerSilent(int silent, SpeakEntity entity);

    /**
     * 有人说话
     *
     * @param speak  1 说话，0 停止说话
     * @param entity
     */
    void onSpeakerSpeaking(int speak, SpeakEntity entity);

    /**
     * 获取到音频每一帧回调
     *
     * @param audioMixBuffer 音频数据
     */
    void onAudioFrameDetected(AudioMixBuffer audioMixBuffer);

    /**
     * 断开连接，只是暂时断开，内部会进行重试
     */
    void onDisconnect();

    /**
     * 重新连接，内部重试连接成功
     */
    void onReconnect();

    /**
     * 音频出错且不可逆，建议退出房间，或者重新连接音频组件
     *
     * @param code 错误码
     */
    void onAudioError(int code);

    /**
     * 拉流声浪数据回调
     *
     * @param soundLevelInfoList
     */
    void onSoundLevelInfo(List<SoundLevelInfoEntity> soundLevelInfoList);

}
