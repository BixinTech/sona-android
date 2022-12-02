package cn.bixin.sona.component.internal.audio.zego;

import androidx.annotation.Nullable;

import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoLivePlayerCallback2;
import com.zego.zegoliveroom.callback.IZegoLivePublisherCallback;
import com.zego.zegoliveroom.callback.IZegoLogHookCallback;
import com.zego.zegoliveroom.callback.IZegoRoomCallback;
import com.zego.zegoliveroom.entity.ZegoPlayStats;
import com.zego.zegoliveroom.entity.ZegoPlayStreamQuality;
import com.zego.zegoliveroom.entity.ZegoPublishStreamQuality;
import com.zego.zegoliveroom.entity.ZegoRoomInfo;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;

import java.util.HashMap;

public interface ZegoSampleCallback {

    abstract class ContextObserver implements ZegoLiveRoom.SDKContextEx {

        @Nullable
        @Override
        public String getSoFullPath() {
            return null;
        }

        @Nullable
        @Override
        public String getLogPath() {
            return null;
        }

        @Nullable
        @Override
        public String getSubLogFolder() {
            return null;
        }

        @Override
        public IZegoLogHookCallback getLogHookCallback() {
            return null;
        }
    }

    class PullStreamObserver implements IZegoLivePlayerCallback2 {

        @Override
        public void onPlayStateUpdate(int stateCode, String streamID) {

        }

        @Override
        public void onPlayQualityUpdate(String streamID, ZegoPlayStreamQuality sq) {

        }

        @Override
        public void onInviteJoinLiveRequest(int seq, String fromUserID, String fromUserName, String roomID) {

        }

        @Override
        public void onRecvEndJoinLiveCommand(String fromUserID, String fromUserName, String roomID) {

        }

        @Override
        public void onVideoSizeChangedTo(String streamID, int width, int height) {

        }

        @Override
        public void onRemoteCameraStatusUpdate(String streamID, int status, int reason) {

        }

        @Override
        public void onRemoteMicStatusUpdate(String streamID, int status, int reason) {

        }

        @Override
        public void onRemoteSpeakerStatusUpdate(String streamID, int status, int reason) {

        }

        @Override
        public void onRecvRemoteAudioFirstFrame(String streamID) {

        }

        @Override
        public void onRecvRemoteVideoFirstFrame(String streamID) {

        }

        @Override
        public void onRenderRemoteVideoFirstFrame(String streamID) {

        }

        @Override
        public void onVideoDecoderError(int codecID, int errorCode, String streamID) {

        }

        @Override
        public void onPlayStatsUpdate(ZegoPlayStats stats) {

        }
    }

    class PushStreamObserver implements IZegoLivePublisherCallback {

        @Override
        public void onPublishStateUpdate(int stateCode, String streamID, HashMap<String, Object> streamInfo) {

        }

        @Override
        public void onJoinLiveRequest(int seq, String fromUserID, String fromUserName, String roomID) {

        }

        @Override
        public void onPublishQualityUpdate(String streamID, ZegoPublishStreamQuality sq) {

        }

        @Override
        public void onCaptureVideoSizeChangedTo(int width, int height) {

        }

        @Override
        public void onCaptureVideoFirstFrame() {

        }

        @Override
        public void onCaptureAudioFirstFrame() {

        }
    }

    class RoomObserver implements IZegoRoomCallback {


        @Override
        public void onKickOut(int reason, String roomID, String customReason) {

        }

        @Override
        public void onDisconnect(int errorCode, String roomID) {

        }

        @Override
        public void onReconnect(int errorCode, String roomID) {

        }

        @Override
        public void onTempBroken(int errorCode, String roomID) {

        }

        @Override
        public void onRoomInfoUpdated(ZegoRoomInfo info, String roomID) {

        }

        @Override
        public void onStreamUpdated(int type, ZegoStreamInfo[] listStream, String roomID) {

        }

        @Override
        public void onStreamExtraInfoUpdated(ZegoStreamInfo[] listStream, String roomID) {

        }

        @Override
        public void onRecvCustomCommand(String userID, String userName, String content, String roomID) {

        }

        @Override
        public void onNetworkQuality(String userID, int txQuality, int rxQuality) {

        }

        @Override
        public void onTokenWillExpired(String roomID, int remainTimeInSecond) {

        }
    }
}
