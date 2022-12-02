package cn.bixin.sona.plugin.config;

/**
 *
 * 音频配置
 *
 * @author luokun
 */
public class AudioConfig extends PluginConfig {

    /**
     * 获取自身音量的时间间隔，单位是毫秒
     */
    public long voiceVolumeInterval = 500;

    /**
     * 是否打开声音监听
     */
    public boolean soundCapture = false;

    public AudioConfig() {}

    public AudioConfig(int voiceVolumeInterval) {
        this.voiceVolumeInterval = voiceVolumeInterval;
    }

    public AudioConfig(boolean soundCapture, int voiceVolumeInterval) {
        this.voiceVolumeInterval = voiceVolumeInterval;
        this.soundCapture = soundCapture;
    }

    public AudioConfig(boolean soundCapture) {
        this.soundCapture = soundCapture;
    }

    public boolean noiseSuppressionEnable = false;

    public boolean isNoiseSuppressionEnable() {
        return noiseSuppressionEnable;
    }

    public void setNoiseSuppressionEnable(boolean noiseSuppressionEnable) {
        this.noiseSuppressionEnable = noiseSuppressionEnable;
    }

    public static class MediaSideInfo {
        public int type;
        public String data;
        public String streamId;
    }
}
