package cn.bixin.sona.component.internal.audio.zego;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.constants.ZegoConstants;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

import cn.bixin.sona.component.ComponentCallback;
import cn.bixin.sona.component.audio.AudioError;
import cn.bixin.sona.component.audio.AudioStream;
import cn.bixin.sona.component.audio.IAudioPlayer;
import cn.bixin.sona.component.internal.audio.AudioComponentWrapper;
import cn.bixin.sona.component.internal.audio.AudioReportCode;
import cn.bixin.sona.component.internal.audio.AudioSession;
import cn.bixin.sona.component.internal.audio.SteamType;
import cn.bixin.sona.component.internal.audio.zego.handler.ZegoInitHandler;
import cn.bixin.sona.component.internal.audio.zego.handler.ZegoLoginHandler;
import cn.bixin.sona.component.internal.audio.zego.handler.ZegoObserverHandler;
import cn.bixin.sona.component.internal.audio.zego.handler.ZegoReportHandler;
import cn.bixin.sona.component.internal.audio.zego.handler.ZegoSoundLevelHandler;
import cn.bixin.sona.component.internal.audio.zego.handler.ZegoStreamRetryHandler;
import cn.bixin.sona.data.AudioDeviceModeEnum;
import cn.bixin.sona.data.StreamModeEnum;
import cn.bixin.sona.data.StreamSupplierEnum;
import cn.bixin.sona.data.entity.RoomInfo;
import cn.bixin.sona.data.entity.SonaRoomData;
import cn.bixin.sona.util.SonaLogger;

public class ZegoAudio extends AudioComponentWrapper {

    private ZegoLiveRoom mLiveRoom;
    private ZegoStream mStream;
    private boolean enableMic = true;
    private HashMap<IAudioPlayer.Index, ZegoPlayer> mPlayer = new HashMap<>();
    private boolean shouldPushStream = false;// 麦下到麦上需要先登录再推流，通过这个状态防止异步问题

    private ZegoInitHandler initHandler = new ZegoInitHandler(this);
    private ZegoObserverHandler observerHandler = new ZegoObserverHandler(this);
    private ZegoLoginHandler loginHandler = new ZegoLoginHandler(this);
    private ZegoReportHandler reportHandler = new ZegoReportHandler(this);
    private ZegoSoundLevelHandler soundLevelHandler = new ZegoSoundLevelHandler(this);
    private ZegoStreamRetryHandler streamRetryHandler = new ZegoStreamRetryHandler(this);

    public ZegoAudio(cn.bixin.sona.component.audio.AudioComponent target) {
        super(target);
    }

    @Override
    public void unAssembling() {
        super.unAssembling();
        setAutoPullStream(false);
        soundLevelHandler.captureSound(false);
        streamRetryHandler.unAssembling();
        reportHandler.unAssembling();
        if (mStream != null) {
            mStream.stopPublishStream();
            mStream.stopPullStream();
        }
        if (mPlayer != null) {
            for (ZegoPlayer player : mPlayer.values()) {
                player.release();
            }
        }

        if (mLiveRoom != null) {
            soundLevelHandler.unAssembling();
            initHandler.unAssembling();
        }
    }

    private void login(String roomId, ComponentCallback componentCallback) {
        loginHandler.login(roomId, componentCallback);
    }

    @Override
    protected void enter(String roomId, RoomInfo.StreamConfig streamConfig, ComponentCallback componentCallback) {
        mStream = new ZegoStream(mLiveRoom);
        if (StreamModeEnum.MIXED.getModeName().equals(streamConfig.getPullMode())) {
            if (!TextUtils.isEmpty(streamConfig.getStreamId())) {
                mStream.setAudioSession(AudioSession.MIX);
                String mixStream = streamConfig.getStreamId();
                mStream.providerStream(SteamType.MIX, new AudioStream(mixStream, "", ""));
                soundLevelHandler.registerSoundLevelListener(AudioSession.MIX);
                if (componentCallback != null) {
                    componentCallback.executeSuccess();
                }
                SonaLogger.log("混流streamId:".concat(mixStream));
            }
        } else {
            soundLevelHandler.registerSoundLevelListener(AudioSession.MULTI);
            login(roomId, componentCallback);
        }
    }

    @Override
    public void pushStream(String streamId, ComponentCallback componentCallback) {
        shouldPushStream = true;
        if (mStream != null && AudioSession.MULTI != mStream.sessionType()) {
            // 如果是混流模式，则停止拉流，并且设置为多路流模式
            mStream.stopPullStream();
            mStream.setAudioSession(AudioSession.MULTI);
            String roomId = acquire(SonaRoomData.class).streamInfo.getStreamRoomId().get(StreamSupplierEnum.ZEGO.name());
            login(roomId, new ComponentCallback() {

                @Override
                public void executeSuccess() {
                    if (isAutoPullStream()) {
                        reportHandler.recordPullAllStreamTime();
                        mStream.startPullStream();
                    }
                    if (shouldPushStream) {
                        pushStreamStep2(streamId, componentCallback);
                    }
                    soundLevelHandler.registerSoundLevelListener(AudioSession.MULTI);
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (componentCallback != null) {
                        componentCallback.executeFailure(code, reason);
                    }
                }
            });

        } else if (mStream != null) {
            pushStreamStep2(streamId, componentCallback);
        }
    }

    @Override
    public void stopPushStream(ComponentCallback sonaCoreCallback) {
        shouldPushStream = false;
        streamRetryHandler.removePushStreamMsg();
        soundLevelHandler.captureSound(false);
        if (mStream != null) {
            boolean result = mStream.stopPublishStream();
            if (result) {
                mStream.providerStream(SteamType.LOCAL, null);
                sonaCoreCallback.executeSuccess();
            } else {
                sonaCoreCallback.executeFailure(AudioError.STOP_PUSH_STREAM_ERROR, "停止推流失败");
            }
        } else {
            sonaCoreCallback.executeSuccess();
        }

    }

    @Override
    public void pullStream(String streamId, ComponentCallback sonaCoreCallback) {
        if (mStream != null) {
            if (mStream.findAudioStream(streamId) == null) {
                // 如果没有这条流，则拉这条流
                reportHandler.recordPullStreamTime(streamId);
                boolean result = mStream.startPullStream(streamId);
                if (sonaCoreCallback != null) {
                    if (result) {
                        sonaCoreCallback.executeSuccess();
                    } else {
                        sonaCoreCallback.executeFailure(AudioError.PULL_STREAM_ERROR, "拉流失败");
                    }
                }
            } else {
                if (sonaCoreCallback != null) {
                    sonaCoreCallback.executeSuccess();
                }
            }
        }
    }

    @Override
    public void stopPullStream(String streamId, ComponentCallback sonaCoreCallback) {
        if (mStream != null) {
            streamRetryHandler.removeStream(streamId);
            if (mStream.findAudioStream(streamId) != null) {
                // 如果有这条流，则停止拉这条流
                boolean result = mStream.stopPullStream(streamId);
                if (sonaCoreCallback != null) {
                    if (result) {
                        sonaCoreCallback.executeSuccess();
                    } else {
                        sonaCoreCallback.executeFailure(AudioError.STOP_PULL_STREAM_ERROR, "停止拉流失败");
                    }
                }
            } else {
                if (sonaCoreCallback != null) {
                    sonaCoreCallback.executeSuccess();
                }
            }
        }
    }

    @Override
    public void pullStream(ComponentCallback sonaCoreCallback) {
        // 备注：只要开启拉流，则后续变为自动拉流
        SonaLogger.print("execute pull stream");
        setAutoPullStream(true);

        if (mStream != null) {
            reportHandler.recordPullAllStreamTime();
            boolean result = mStream.startPullStream();
            if (result) {
                sonaCoreCallback.executeSuccess();
            } else {
                sonaCoreCallback.executeFailure(AudioError.PULL_STREAM_ERROR, "拉流失败");
            }
        }
    }

    @Override
    public void stopPullStream(ComponentCallback sonaCoreCallback) {
        // 备注：只要关闭拉流，则后续变为自动不拉流
        setAutoPullStream(false);

        if (mStream != null) {
            streamRetryHandler.stopPullStream();
            boolean result = mStream.stopPullStream();
            if (result) {
                sonaCoreCallback.executeSuccess();
            } else {
                sonaCoreCallback.executeFailure(AudioError.STOP_PULL_STREAM_ERROR, "停止拉流失败");
            }
        }
    }

    @Override
    public void switchMic(boolean on, ComponentCallback componentCallback) {
        enableMic = on;
        boolean result = mLiveRoom.enableMic(on);
        if (result) {
            componentCallback.executeSuccess();
            SonaLogger.log(on ? "打开麦克风成功" : "关闭麦克风成功", AudioReportCode.ZEGO_MIC_SUCCESS_CODE);
        } else {
            componentCallback.executeFailure(AudioError.SWITCH_MIC_ERROR, on ? "打开麦克风失败" : "关闭麦克风失败");
            SonaLogger.log(on ? "打开麦克风失败" : "关闭麦克风失败", AudioReportCode.ZEGO_MIC_FAIL_CODE);
        }
    }

    @Override
    public void switchHandsfree(boolean on, ComponentCallback componentCallback) {
        boolean result = mLiveRoom.setBuiltInSpeakerOn(on);
        if (result) {
            componentCallback.executeSuccess();
            SonaLogger.log(on ? "打开免提成功" : "关闭免提成功", AudioReportCode.ZEGO_HANDSFREE_SUCCESS_CODE);
        } else {
            componentCallback.executeFailure(AudioError.SWITCH_HANDSFREE_ERROR, on ? "打开免提失败" : "关闭免提失败");
            SonaLogger.log(on ? "打开免提失败" : "关闭免提失败", AudioReportCode.ZEGO_HANDSFREE_ERROR_CODE);
        }
    }

    @Override
    public void silent(String streamId, boolean on, ComponentCallback sonaCoreCallback) {
        if (mStream != null) {
            boolean result = mStream.silentStream(streamId, on);
            if (sonaCoreCallback != null) {
                if (result) {
                    sonaCoreCallback.executeSuccess();
                } else {
                    sonaCoreCallback.executeFailure(AudioError.SILENT_ERROR, on ? "静音失败" : "取消静音失败");
                }
            }
        }
    }

    @Override
    public void switchListen(boolean b, @Nullable ComponentCallback componentCallback) {
        if (mStream != null) {
            if (mStream.sessionType() == AudioSession.MULTI) {
                if (componentCallback != null) {
                    componentCallback.executeSuccess();
                }
                return;
            }
            mStream.stopPullStream();
            mStream.setAudioSession(AudioSession.MULTI);
            String roomId = acquire(SonaRoomData.class).streamInfo.getStreamRoomId().get(StreamSupplierEnum.ZEGO.name());
            login(roomId, new ComponentCallback() {

                @Override
                public void executeSuccess() {
                    if (isAutoPullStream()) {
                        mStream.startPullStream();
                    }
                    soundLevelHandler.registerSoundLevelListener(AudioSession.MULTI);
                    if (componentCallback != null) {
                        componentCallback.executeSuccess();
                    }
                }

                @Override
                public void executeFailure(int code, String reason) {
                    if (componentCallback != null) {
                        componentCallback.executeFailure(code, reason);
                    }
                }
            });
        }
    }

    @Override
    public boolean init() {
        initHandler.initUserConfig();
        initHandler.initAdvancedConfig();
        mLiveRoom = new ZegoLiveRoom();
        if (!initHandler.initSdk()) {
            return false;
        }
        initHandler.initBasicConfig();
        observerHandler.addObservers();
        return true;
    }

    @Override
    synchronized public IAudioPlayer getAudioPlayer() {
        if (mPlayer.get(IAudioPlayer.Index.NONE) == null) {
            mPlayer.put(IAudioPlayer.Index.NONE, new ZegoPlayer(IAudioPlayer.Index.NONE));
        }
        return mPlayer.get(IAudioPlayer.Index.NONE);
    }

    @Nullable
    @Override
    public List<AudioStream> currentStream() {
        if (mStream != null) {
            return mStream.findAudioStream();
        }
        return null;
    }

    @Override
    protected void resume() {

    }

    @Override
    protected void pause() {

    }

    private void pushStreamStep2(String streamId, ComponentCallback componentCallback) {
        if (mStream != null) {
            if (mStream.isPushStreamSuccess()) {
                if (componentCallback != null) {
                    componentCallback.executeSuccess();
                }
                return;
            }
            mStream.providerStream(SteamType.LOCAL, new AudioStream(streamId, "", ""));
            boolean result = mStream.startPublishStream(streamId);

            if (componentCallback != null) {
                if (result) {
                    componentCallback.executeSuccess();
                } else {
                    mStream.providerStream(SteamType.LOCAL, null);
                    componentCallback.executeFailure(AudioError.PUSH_STREAM_ERROR, "推流失败");
                }
            }
        }
    }

    @Override
    public IAudioPlayer compareAndGet(IAudioPlayer.Index index) {
        if (mPlayer.get(index) == null) {
            mPlayer.put(index, new ZegoPlayer(index));
        }
        return mPlayer.get(index);
    }

    @Override
    public void setAudioDeviceMode(@NonNull AudioDeviceModeEnum audioDeviceMode) {
        SonaLogger.log("setAudioDeviceMode " + audioDeviceMode.name());
        if (audioDeviceMode == AudioDeviceModeEnum.COMMUNICATION3) {
            ZegoLiveRoom.setAudioDeviceMode(ZegoConstants.AudioDeviceMode.Communication3);
        } else {
            ZegoLiveRoom.setAudioDeviceMode(ZegoConstants.AudioDeviceMode.General);
        }
    }

    public boolean getEnableMic() {
        return enableMic;
    }

    public ZegoStream getStreamHandler() {
        return mStream;
    }

    public ZegoLiveRoom getLiveRoom() {
        return mLiveRoom;
    }

    public ZegoReportHandler getReportHandler() {
        return reportHandler;
    }

    public ZegoStreamRetryHandler getStreamRetryHandler() {
        return streamRetryHandler;
    }

    public ZegoSoundLevelHandler getSoundLevelHandler() {
        return soundLevelHandler;
    }
}
