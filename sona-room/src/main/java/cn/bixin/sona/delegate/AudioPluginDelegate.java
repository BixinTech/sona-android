package cn.bixin.sona.delegate;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import cn.bixin.sona.api.ApiSubscriber;
import cn.bixin.sona.api.SonaApi;
import cn.bixin.sona.component.ComponentCallback;
import cn.bixin.sona.component.ComponentMessage;
import cn.bixin.sona.component.audio.AudioError;
import cn.bixin.sona.component.audio.AudioMixBuffer;
import cn.bixin.sona.component.audio.AudioStream;
import cn.bixin.sona.component.connection.ConnectionMessage;
import cn.bixin.sona.component.connection.MessageItemEnum;
import cn.bixin.sona.data.AudioDeviceModeEnum;
import cn.bixin.sona.data.StreamModeEnum;
import cn.bixin.sona.data.StreamSupplierEnum;
import cn.bixin.sona.data.entity.AppInfo;
import cn.bixin.sona.data.entity.SonaRoomData;
import cn.bixin.sona.data.entity.UserData;
import cn.bixin.sona.delegate.internal.AudioDataTracker;
import cn.bixin.sona.delegate.internal.PluginError;
import cn.bixin.sona.delegate.observer.AudioReconnectObserver;
import cn.bixin.sona.driver.ComponentType;
import cn.bixin.sona.driver.RoomDriver;
import cn.bixin.sona.plugin.AudioPlugin;
import cn.bixin.sona.plugin.PluginCallback;
import cn.bixin.sona.plugin.SonaPlugin;
import cn.bixin.sona.plugin.config.AudioConfig;
import cn.bixin.sona.plugin.entity.PluginEnum;
import cn.bixin.sona.plugin.entity.SoundLevelInfoEntity;
import cn.bixin.sona.plugin.entity.SpeakEntity;
import cn.bixin.sona.plugin.observer.AudioPluginObserver;
import cn.bixin.sona.report.ReportCode;
import cn.bixin.sona.report.SonaReport;
import cn.bixin.sona.report.SonaReportEvent;
import cn.bixin.sona.util.SonaConfigManager;
import cn.bixin.sona.util.SonaLogger;

/**
 * 音频代理
 *
 * @author luokun
 */
public class AudioPluginDelegate extends SonaPluginDelegate implements AudioPlugin, AudioPluginObserver {

    private RoomDriver roomDriver;
    private AudioPluginObserver observer;

    /**
     * 风控检测到停止推流
     **/
    private volatile boolean receiveStopStreamDueRisk = false;

    public AudioPluginDelegate(RoomDriver roomDriver) {
        this.roomDriver = roomDriver;
        this.roomDriver.provide(new AudioDataTracker());
    }

    @Override
    public void startSpeak(PluginCallback pluginCallback) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            SonaConfigManager.getInstance().startTask();
            AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
            if (audioDataTracker != null && audioDataTracker.isPublishing()) {
                if (pluginCallback != null) {
                    pluginCallback.onSuccess();
                }
                return;
            }
            String stream = generateStream();
            if (!TextUtils.isEmpty(stream)) {
                SonaLogger.print("generateStream: " + stream);
                roomDriver.getAudio().pushStream(stream, new ComponentCallback() {
                    @Override
                    public void executeSuccess() {
                        AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                        if (audioDataTracker != null) {
                            audioDataTracker.setPublishing(true);
                        }
                        if (pluginCallback != null) {
                            pluginCallback.onSuccess();
                        }
                    }

                    @Override
                    public void executeFailure(int code, String reason) {
                        AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                        if (audioDataTracker != null) {
                            audioDataTracker.setPublishing(false);
                        }
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(code, "说话失败");
                        }
                    }
                });
            } else {
                if (pluginCallback != null) {
                    pluginCallback.onFailure(PluginError.SERVER_GENERATE_STREAM_ERROR, "生成流失败");
                }
            }
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void stopSpeak(PluginCallback pluginCallback) {
        SonaConfigManager.getInstance().stopTask();
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.getAudio().stopPushStream(new ComponentCallback() {
                @Override
                public void executeSuccess() {
                    AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                    if (audioDataTracker != null) {
                        audioDataTracker.setPublishing(false);
                    }
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (pluginCallback != null) {
                        pluginCallback.onFailure(code, "停止说话失败");
                    }
                }
            });
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void startListen(String streamId, PluginCallback pluginCallback) {
        if (!TextUtils.isEmpty(streamId)) {
            if (roomDriver.componentCertified(ComponentType.AUDIO)) {
                roomDriver.getAudio().pullStream(streamId, new ComponentCallback() {
                    @Override
                    public void executeSuccess() {
                        if (pluginCallback != null) {
                            pluginCallback.onSuccess();
                        }
                    }

                    @Override
                    public void executeFailure(int code, String reason) {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(code, reason);
                        }
                    }
                });
            } else {
                if (pluginCallback != null) {
                    pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
                }
            }
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.CALL_PARAM_ERROR, "调用参数错误");
            }
        }
    }

    @Override
    public void stopListen(String streamId, PluginCallback pluginCallback) {
        if (!TextUtils.isEmpty(streamId)) {
            if (roomDriver.componentCertified(ComponentType.AUDIO)) {
                roomDriver.getAudio().stopPullStream(streamId, new ComponentCallback() {
                    @Override
                    public void executeSuccess() {
                        if (pluginCallback != null) {
                            pluginCallback.onSuccess();
                        }
                    }

                    @Override
                    public void executeFailure(int code, String reason) {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(code, reason);
                        }
                    }
                });
            } else {
                if (pluginCallback != null) {
                    pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
                }
            }
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.CALL_PARAM_ERROR, "调用参数错误");
            }
        }
    }

    @Override
    public void startListen(PluginCallback pluginCallback) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.getAudio().pullStream(new ComponentCallback() {
                @Override
                public void executeSuccess() {
                    AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                    if (audioDataTracker != null) {
                        audioDataTracker.setPullStream(true);
                    }
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                    if (audioDataTracker != null) {
                        audioDataTracker.setPullStream(false);
                    }
                    if (pluginCallback != null) {
                        pluginCallback.onFailure(code, reason);
                    }
                }
            });
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void stopListen(PluginCallback pluginCallback) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.getAudio().stopPullStream((new ComponentCallback() {
                @Override
                public void executeSuccess() {
                    AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                    if (audioDataTracker != null) {
                        audioDataTracker.setPullStream(false);
                    }
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (pluginCallback != null) {
                        pluginCallback.onFailure(code, reason);
                    }
                }
            }));
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void silent(String streamId, boolean on, PluginCallback pluginCallback) {
        if (!TextUtils.isEmpty(streamId) && roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.getAudio().silent(streamId, on, new ComponentCallback() {
                @Override
                public void executeSuccess() {
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (pluginCallback != null) {
                        pluginCallback.onFailure(code, reason);
                    }
                }
            });
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void switchMic(boolean on, PluginCallback pluginCallback) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.getAudio().switchMic(on, new ComponentCallback() {
                @Override
                public void executeSuccess() {
                    AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                    if (audioDataTracker != null) {
                        audioDataTracker.setMicOn(on);
                    }
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (pluginCallback != null) {
                        pluginCallback.onFailure(code, reason);
                    }
                }
            });
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void switchHandsfree(boolean on, PluginCallback pluginCallback) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.getAudio().switchHandsfree(on, new ComponentCallback() {
                @Override
                public void executeSuccess() {
                    AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                    if (audioDataTracker != null) {
                        audioDataTracker.setHandsfree(on);
                    }
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (pluginCallback != null) {
                        pluginCallback.onFailure(code, reason);
                    }
                }
            });
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void switchListen(boolean realTime, PluginCallback pluginCallback) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
            if (audioDataTracker.isPublishing()) {
                if (pluginCallback != null) {
                    pluginCallback.onSuccess();
                }
                return;
            }
            roomDriver.getAudio().switchListen(realTime, new ComponentCallback() {
                @Override
                public void executeSuccess() {
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (pluginCallback != null) {
                        pluginCallback.onFailure(code, reason);
                    }
                }
            });
        } else {
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void setAudioDeviceMode(AudioDeviceModeEnum audioDeviceMode) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.getAudio().setAudioDeviceMode(audioDeviceMode);
        }
    }

    @Override
    public List<AudioStream> currentStream() {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            return roomDriver.getAudio().currentStream();
        }
        return null;
    }

    @Override
    public StreamSupplierEnum getStreamSupplier() {
        SonaRoomData sonaRoomData = roomDriver.acquire(SonaRoomData.class);
        if (sonaRoomData == null || sonaRoomData.streamInfo == null || TextUtils.isEmpty(sonaRoomData.streamInfo.getSupplier())) {
            return StreamSupplierEnum.ZEGO;
        }
        return StreamSupplierEnum.valueOf(sonaRoomData.streamInfo.getSupplier());
    }

    @Override
    public void startReconnect(PluginCallback pluginCallback) {
        AudioReconnectObserver audioReconnectObserver = new AudioReconnectObserver(pluginCallback);
        roomDriver.provide(audioReconnectObserver);
        // 发送音频切换的消息，通知其他plugin
        roomDriver.dispatchMessage(ComponentMessage.AUDIO_CHANGE_START, null);
        // 断开音频
        roomDriver.unAssembling(ComponentType.AUDIO);
        // 打开音频
        roomDriver.assembling(ComponentType.AUDIO);
    }

    @Override
    public void observe(AudioPluginObserver observer) {
        this.observer = observer;
    }

    @Override
    public SonaPlugin config(AudioConfig config) {
        roomDriver.provide(config);
        return this;
    }

    @Override
    public void handleMessage(ComponentMessage msgType, Object message) {
        // 处理自身想要的消息
        switch (msgType) {
            case AUDIO_DISCONNECT:
                onDisconnect();
                break;
            case AUDIO_RECONNECT:
                onReconnect();
                break;
            case AUDIO_ERROR:
                onAudioError((int) message);
                break;
            case AUDIO_INIT_FAIL: {
                AudioReconnectObserver audioReconnectObserver = roomDriver.acquire(AudioReconnectObserver.class);
                if (audioReconnectObserver != null) {
                    audioReconnectObserver.onFailure(PluginError.ROOM_COMPONENT_AUDIO_RECONNECT_ERROR, (String) message);
                    roomDriver.remove(AudioReconnectObserver.class);
                }
            }
            break;
            case AUDIO_INIT_SUCCESS: {
                restoreOriginalState();
                AudioReconnectObserver audioReconnectObserver = roomDriver.acquire(AudioReconnectObserver.class);
                if (audioReconnectObserver != null) {
                    audioReconnectObserver.onSuccess();
                    roomDriver.remove(AudioReconnectObserver.class);
                }
            }
            break;
            case AUDIO_REV_ADD_STREAM: {
                AudioStream audioStream = (AudioStream) message;
                SpeakEntity speakEntity = new SpeakEntity(audioStream.getUserId(), audioStream.getUserName(), "", audioStream.getStreamId());
                onSpeakerSpeaking(1, speakEntity);
            }
            break;
            case AUDIO_REV_REMOVE_STREAM: {
                AudioStream audioStream = (AudioStream) message;
                SpeakEntity speakEntity = new SpeakEntity(audioStream.getUserId(), audioStream.getUserName(), "", audioStream.getStreamId());
                onSpeakerSpeaking(0, speakEntity);
            }
            break;
            case AUDIO_REV_SOUND_LEVEL_INFO: {
                onSoundLevelInfo((List<SoundLevelInfoEntity>) message);
            }
            break;
            case ERROR_MSG: {
                if (AudioError.PUSH_STREAM_ERROR == (int) message) {
                    // 推流失败，缓存需要置为false，以至于可以重新推流
                    AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
                    if (audioDataTracker != null) {
                        audioDataTracker.setPublishing(false);
                    }
                }
            }
            break;
            case CONNECT_REV_MESSAGE:
                ConnectionMessage connectionMessage = (ConnectionMessage) message;
                if (connectionMessage.getItem() == MessageItemEnum.STREAM_SILENT_SET_CANCEL) {
                    // 静音消息
                    if (TextUtils.isEmpty(connectionMessage.getMessage())) {
                        return;
                    }
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(connectionMessage.getMessage());
                        String roomId = jsonObject.getString("roomId");
                        int mute = jsonObject.getIntValue("isMute");
                        JSONArray streams = jsonObject.getJSONArray("streamList");
                        if (streams == null) {
                            return;
                        }
                        int streamSize = streams.size();
                        for (int i = 0; i < streamSize; i++) {
                            JSONObject item = streams.getJSONObject(i);
                            String streamId = item.getString("streamId");
                            String accId = item.getString("accId");
                            String uid = item.getString("uid");
                            silentStream(streamId, accId, uid, roomId, mute);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (connectionMessage.getItem() == MessageItemEnum.AUDIO_HOT_SWITCH) {
                    // 热切
                    if (TextUtils.isEmpty(connectionMessage.getMessage())) {
                        return;
                    }
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(connectionMessage.getMessage());
                        String supplier = jsonObject.getString("supplier");
                        String pullMode = jsonObject.getString("pullMode");
                        String streamId = jsonObject.getString("streamId");
                        String streamUrl = jsonObject.getString("streamUrl");
                        int bitrate = jsonObject.getIntValue("bitrate");
                        int playerType = jsonObject.getIntValue("playerType");
                        if (StreamSupplierEnum.valueOf(supplier) != null && StreamModeEnum.valueOf(pullMode) != null) {
                            roomDriver.runUiThread(() -> {
                                handleHotSwitch(supplier, pullMode, streamId, streamUrl, bitrate, playerType);
                            });
                        }
                        boolean fromSyncConfig = jsonObject.getBooleanValue("fromSyncConfig");
                        if (!fromSyncConfig) {
                            SonaReportEvent sonaReportEvent = new SonaReportEvent.Builder()
                                    .setCode(ReportCode.ROOM_AUDIO_SWITCH_CODE)
                                    .setContent(connectionMessage.getMessage())
                                    .setType(SonaReportEvent.LOG | SonaReportEvent.LOGAN)
                                    .build();
                            SonaReport.INSTANCE.report(sonaReportEvent);
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            case AUDIO_RECEIVE_FRAME:
                if (message instanceof AudioMixBuffer) {
                    onAudioFrameDetected((AudioMixBuffer) message);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSpeakerSilent(int silent, SpeakEntity entity) {
        if (observer != null) {
            observer.onSpeakerSilent(silent, entity);
        }
    }

    @Override
    public void onSpeakerSpeaking(int speak, SpeakEntity entity) {
        if (observer != null) {
            observer.onSpeakerSpeaking(speak, entity);
        }
    }

    @Override
    public void onAudioFrameDetected(AudioMixBuffer audioMixBuffer) {
        if (observer != null) {
            observer.onAudioFrameDetected(audioMixBuffer);
        }
    }

    @Override
    public void onDisconnect() {
        if (observer != null) {
            observer.onDisconnect();
        }
    }

    @Override
    public void onReconnect() {
        if (observer != null) {
            observer.onReconnect();
        }
    }

    @Override
    public void onAudioError(int code) {
        if (observer != null) {
            observer.onAudioError(code);
        }
    }

    @Override
    public void onSoundLevelInfo(List<SoundLevelInfoEntity> soundLevelInfoList) {
        roomDriver.runUiThread(() -> {
            if (observer != null) {
                observer.onSoundLevelInfo(soundLevelInfoList);
            }
        });
    }

    @Override
    public final PluginEnum pluginType() {
        return PluginEnum.AUDIO;
    }

    @Override
    public void remove() {
        super.remove();
        roomDriver.remove(AudioConfig.class);
        observer = null;
    }

    /**
     * 拉流/关流
     *
     * @param streamId
     * @param accId
     * @param setStream 0 关流 1 拉流
     */
    private void listen(String streamId, String accId, int setStream) {
        String audioId = streamId;
        if (roomDriver.acquire(SonaRoomData.class) != null && roomDriver.acquire(SonaRoomData.class).streamInfo != null) {
            String supplier = roomDriver.acquire(SonaRoomData.class).streamInfo.getSupplier();
            if (StreamSupplierEnum.TENCENT.getSupplierName().equals(supplier)) {
                audioId = accId;
            }
        }
        final String realAudioId = audioId;
        if (!TextUtils.isEmpty(realAudioId)) {
            roomDriver.runUiThread(() -> {
                if (setStream == 1) {
                    startListen(realAudioId, null);
                } else if (setStream == 0) {
                    stopListen(realAudioId, null);
                }
            });
        }
    }

    /**
     * 静音一条流
     *
     * @param streamId
     * @param accId
     * @param uid
     * @param roomId
     * @param mute
     */
    private void silentStream(String streamId, String accId, String uid, String roomId, int mute) {
        String audioId = streamId;
        if (roomDriver.acquire(SonaRoomData.class) != null && roomDriver.acquire(SonaRoomData.class).streamInfo != null) {
            String supplier = roomDriver.acquire(SonaRoomData.class).streamInfo.getSupplier();
            if (StreamSupplierEnum.TENCENT.getSupplierName().equals(supplier)) {
                audioId = accId;
            }
        }
        final String realAudioId = audioId;
        if (!TextUtils.isEmpty(realAudioId)) {
            roomDriver.runUiThread(() -> {
                silent(realAudioId, mute == 1, new PluginCallback() {
                    @Override
                    public void onSuccess() {
                        // 发出消息，消息归属待确定
                        SpeakEntity speakEntity = new SpeakEntity(uid, roomId, realAudioId);
                        onSpeakerSilent(mute, speakEntity);
                    }

                    @Override
                    public void onFailure(int code, String reason) {
                        SonaLogger.print("静音失败 code:" + code + ", reason:" + reason);
                    }
                });
            });
        }
    }

    /**
     * 静音多条流
     *
     * @param streams
     * @param accIds
     * @param silent
     */
    private void silentStream(JSONArray streams, JSONArray accIds, boolean silent) {
        if (roomDriver.acquire(SonaRoomData.class) != null && roomDriver.acquire(SonaRoomData.class).streamInfo != null) {
            String supplier = roomDriver.acquire(SonaRoomData.class).streamInfo.getSupplier();
            if (StreamSupplierEnum.TENCENT.getSupplierName().equals(supplier)) {
                silentStream(accIds, silent);
            } else {
                silentStream(streams, silent);
            }
        }
    }

    private void silentStream(JSONArray streams, boolean silent) {
        if (streams == null) {
            return;
        }
        int streamSize = streams.size();
        for (int i = 0; i < streamSize; i++) {
            String streamId = streams.getString(i);
            if (!TextUtils.isEmpty(streamId)) {
                roomDriver.runUiThread(() -> {
                    silent(streamId, silent, new PluginCallback() {
                        @Override
                        public void onSuccess() {
                            // 发出消息，消息归属待确定
                            SpeakEntity speakEntity = new SpeakEntity("", "", streamId);
                            onSpeakerSilent(silent ? 1 : 0, speakEntity);
                        }

                        @Override
                        public void onFailure(int code, String reason) {
                            SonaLogger.print("静音失败 code:" + code + ", reason:" + reason);
                        }
                    });
                });
            }
        }
    }

    /**
     * 处理热切
     *
     * @param supplier   产商
     * @param pullMode   拉流模式
     * @param streamId   即构混流id
     * @param streamUrl  腾讯混流地址
     * @param bitrate    码率
     * @param playerType 播放类型
     */
    private void handleHotSwitch(String supplier, String pullMode, String streamId, String
            streamUrl, int bitrate, int playerType) {
        if (!roomDriver.componentCertified(ComponentType.AUDIO) ||
                roomDriver.acquire(SonaRoomData.class) == null) {
            return;
        }

        SonaRoomData sonaRoomData = roomDriver.acquire(SonaRoomData.class);
        String lastSupplier = "";
        String lastPullMode = "";
        if (sonaRoomData != null && sonaRoomData.streamInfo != null) {
            lastSupplier = sonaRoomData.streamInfo.getSupplier();
            lastPullMode = sonaRoomData.streamInfo.getPullMode();
        }
        if (sonaRoomData == null || sonaRoomData.roomId == null || sonaRoomData.streamInfo == null
                || (supplier.equals(lastSupplier) && pullMode.equals(lastPullMode))) {
            // 过滤与当前一样的音频模式
            return;
        }

        sonaRoomData.streamInfo.setPullMode(pullMode);
        sonaRoomData.streamInfo.setSupplier(supplier);

        String finalLastPullMode = lastPullMode;
        String finalLastSupplier = lastSupplier;
        register(SonaApi.getUserSig(sonaRoomData.roomId)
                .subscribeWith(new ApiSubscriber<AppInfo>() {
                    @Override
                    protected void onSuccess(AppInfo appInfo) {
                        // 替换当前的流的数据
                        SonaRoomData sonaRoomData = roomDriver.acquire(SonaRoomData.class);
                        if (sonaRoomData != null && sonaRoomData.streamInfo != null) {
                            sonaRoomData.streamInfo.setPullMode(pullMode);
                            sonaRoomData.streamInfo.setSupplier(supplier);
                            sonaRoomData.streamInfo.setStreamId(streamId);
                            sonaRoomData.streamInfo.setStreamUrl(streamUrl);
                            sonaRoomData.streamInfo.setAppInfo(appInfo);
                            sonaRoomData.streamInfo.setBitrate(bitrate);
                            sonaRoomData.streamInfo.setPlayerType(playerType);
                            // 发送音频切换的消息，通知其他plugin
                            roomDriver.dispatchMessage(ComponentMessage.AUDIO_CHANGE_START, null);
                            // 断开音频
                            roomDriver.unAssembling(ComponentType.AUDIO);
                            roomDriver.fillReport("supplier", supplier);
                            // 打开音频
                            roomDriver.assembling(ComponentType.AUDIO);
                        }
                    }

                    @Override
                    protected void onFailure(Throwable e) {
                        SonaRoomData sonaRoomData = roomDriver.acquire(SonaRoomData.class);
                        if (sonaRoomData != null && sonaRoomData.streamInfo != null) {
                            sonaRoomData.streamInfo.setPullMode(finalLastPullMode);
                            sonaRoomData.streamInfo.setSupplier(finalLastSupplier);
                        }
                    }
                })
        );
    }

    /**
     * 还原原有状态
     */
    private void restoreOriginalState() {
        // 备份当前音频状态
        AudioDataTracker audioDataTracker = roomDriver.acquire(AudioDataTracker.class);
        roomDriver.provide(new AudioDataTracker());
        if (audioDataTracker != null) {
            if (audioDataTracker.isPullStream()) {
                // 拉流
                startListen(null);
            }

            if (audioDataTracker.isPublishing()) {
                // 推流
                startSpeak(null);
            }

            if (audioDataTracker.isHandsfree()) {
                // 打开免提
                switchHandsfree(true, null);
            }

            if (!audioDataTracker.isMicOn()) {
                // 关麦
                switchMic(false, null);
            }
        }
        roomDriver.dispatchMessage(ComponentMessage.AUDIO_CHANGE_END, audioDataTracker);
    }

    private final String CONNECT = "_";

    private String generateStream() {
        SonaRoomData sonaRoomData = roomDriver.acquire(SonaRoomData.class);
        if (sonaRoomData == null || sonaRoomData.streamInfo == null || TextUtils.isEmpty(sonaRoomData.streamInfo.getSupplier())) {
            return null;
        }

        UserData userData = roomDriver.acquire(UserData.class);
        if (userData == null || TextUtils.isEmpty(userData.getUid())) {
            return null;
        }
        try {
            StringBuilder streamBuilder = new StringBuilder();
            streamBuilder.append(sonaRoomData.productCodeAlias)
                    .append(CONNECT)
                    .append(sonaRoomData.streamInfo.getSupplier().charAt(0))
                    .append(CONNECT)
                    .append(SonaRoomData.sdkVersion)
                    .append(CONNECT)
                    .append(1)
                    .append(CONNECT)
                    .append(sonaRoomData.roomId)
                    .append(CONNECT)
                    .append(userData.getUid())
                    .append(CONNECT);
            String stream = streamBuilder.toString();
            int leftLen = 64 - stream.length();
            if (leftLen > 0) {
                // 补全64个字符
                String random = String.valueOf(System.currentTimeMillis());
                int diff = random.length() - leftLen;
                while (diff < 0) {
                    random = random + System.currentTimeMillis();
                    diff = random.length() - leftLen;
                }

                random = random.substring(0, leftLen);

                stream = stream + random;
            } else if (leftLen < 0) {
                stream = stream.substring(0, 64);
            }
            return stream;
        } catch (Throwable e) {
            SonaLogger.log("generateStream" + e.getMessage(), ReportCode.ROOM_GENERATE_STREAM_CODE);
        }

        return null;
    }

}
