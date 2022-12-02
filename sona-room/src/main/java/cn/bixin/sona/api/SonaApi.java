package cn.bixin.sona.api;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.bixin.sona.base.Sona;
import cn.bixin.sona.base.net.ApiServiceManager;
import cn.bixin.sona.base.net.RequestParam;
import cn.bixin.sona.base.net.ResponseFunc;
import cn.bixin.sona.data.entity.AppInfo;
import cn.bixin.sona.data.entity.RoomInfo;
import cn.bixin.sona.plugin.entity.OnlineUserData;
import cn.bixin.sona.util.RxSchedulers;
import io.reactivex.Flowable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class SonaApi {

    /**
     * 创建房间
     *
     * @param roomTitle   房间名称
     * @param productCode 产品名称
     * @param password    房间密码
     * @param ext         扩展信息
     * @return
     */
    public static Flowable<RoomInfo> createRoom(String roomTitle, String productCode, String password, Map ext) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .createRoom(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomTitle", roomTitle)
                        .putParam("productCode", productCode)
                        .putParam("password", password)
                        .putParam("extMap", ext)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 开启房间
     *
     * @param roomId
     * @return
     */
    public static Flowable<RoomInfo> openRoom(String roomId) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .openRoom(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 关闭房间
     *
     * @param roomId
     * @return
     */
    public static Flowable<Boolean> closeRoom(String roomId) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .closeRoom(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 进入房间
     *
     * @param bizRoomId   业务房间id
     * @param productCode 业务产品
     * @param password    房间密码
     * @param ext         其他字段
     * @return
     */
    public static Flowable<RoomInfo> enterRoom(String bizRoomId, String productCode, String password, Map ext) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .enterRoom(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", bizRoomId)
                        .putParam("password", password)
                        .putParam("extMap", ext)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 离开房间
     *
     * @param roomId
     * @return
     */
    public static Flowable<Boolean> leaveRoom(String roomId) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .leaveRoom(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 更改房间密码
     *
     * @param roomId
     * @param oldPassword 老密码
     * @param newPassword 新密码
     * @return
     */
    public static Flowable<Boolean> updateRoomPassword(String roomId, String oldPassword, String newPassword) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .updateRoomPassword(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("oldPassword", oldPassword)
                        .putParam("newPassword", newPassword)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 发送消息
     *
     * @param messageType
     * @param message
     * @param attach
     * @param mediaInfo
     * @param roomId
     * @return
     */
    public static Flowable<Boolean> sendMessage(int messageType, String message, String attach, String mediaInfo, String roomId) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .sendMessage((RequestParam.paramBuilder()
                        .putParam("message", message)
                        .putParam("messageType", messageType)
                        .putParam("attach", attach)
                        .putParam("mediaInfo", mediaInfo)
                        .putParam("roomId", roomId)
                        .build()
                        .getRequestBody()))
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 发送消息
     *
     * @param params
     * @return
     */
    public static Flowable<Boolean> sendMessage(String params) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .sendMessage(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params))
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 设置管理员
     *
     * @param roomId
     * @param targetUid 被设置的uid
     * @return
     */
    public static Flowable<Boolean> setAdmin(String roomId, String targetUid) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .setAdmin(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUid", targetUid)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 取消管理员
     *
     * @param roomId
     * @param targetUid
     * @return
     */
    public static Flowable<Boolean> cancelAdmin(String roomId, String targetUid) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .cancelAdmin(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUid", targetUid)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 拉黑
     *
     * @param roomId
     * @param targetUid
     * @param reason
     * @return
     */
    public static Flowable<Boolean> black(String roomId, String targetUid, String reason) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .black(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUid", targetUid)
                        .putParam("reason", reason)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 取消拉黑
     *
     * @param roomId
     * @param targetUid
     * @param reason
     * @return
     */
    public static Flowable<Boolean> cancelBlack(String roomId, String targetUid, String reason) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .cancelBlack(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUid", targetUid)
                        .putParam("reason", reason)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 禁言
     *
     * @param roomId
     * @param targetUid
     * @param minute    禁言时间，单位为分钟
     * @return
     */
    public static Flowable<Boolean> mute(String roomId, String targetUid, int minute) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .mute(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUid", targetUid)
                        .putParam("minute", minute)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 取消禁言
     *
     * @param roomId
     * @param targetUid
     * @return
     */
    public static Flowable<Boolean> cancelMute(String roomId, String targetUid) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .cancelMute(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUid", targetUid)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 静音
     *
     * @param roomId
     * @param targetUids
     * @return
     */
    public static Flowable<Boolean> silent(String roomId, List<String> targetUids) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .silent(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUids", targetUids)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 取消静音
     *
     * @param roomId
     * @param targetUids
     * @return
     */
    public static Flowable<Boolean> cancelSilent(String roomId, List<String> targetUids) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .cancelSilent(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUids", targetUids)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 踢人
     *
     * @param roomId
     * @param targetUid
     * @return
     */
    public static Flowable<Boolean> kick(String roomId, String targetUid) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .kick(RequestParam.paramBuilder()
                        .putParam("uid", Sona.getUid())
                        .putParam("roomId", roomId)
                        .putParam("targetUid", targetUid)
                        .build()
                        .getRequestBody())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 获取在线人员
     *
     * @param roomId
     * @return
     */
    public static Flowable<OnlineUserData> getOnlineUser(String roomId, String anchor, int limit) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .getOnlineUser(Sona.getUid(), roomId, anchor, limit)
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 获取在线人员
     *
     * @param roomId
     * @return
     */
    public static Flowable<Integer> getOnlineNumber(String roomId) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .getOnlineNumber(Sona.getUid(), roomId)
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 获取userSig、登录腾讯房间需要
     *
     * @param roomId
     * @return
     */
    public static Flowable<AppInfo> getUserSig(String roomId) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .getUserSig(roomId, Sona.getUid())
                .map(new ResponseFunc<>())
                .compose(RxSchedulers.subToMain());
    }

    /**
     * 获取房间音频配置信息
     *
     * @param roomId
     * @return
     */
    public static Flowable<String> syncConfig(String roomId) {
        return ApiServiceManager.getInstance().obtainService(SonaApiService.class)
                .syncConfig(roomId, Sona.getUid())
                .map(new ResponseFunc<>())
                .map(sonaConfigInfo -> {
                    RoomInfo.StreamConfig streamConfig = sonaConfigInfo.getStreamConfig();
                    if (streamConfig != null) {
                        streamConfig.setFromSyncConfig(true);
                        return JSON.toJSONString(streamConfig);
                    }
                    return "";
                }).compose(RxSchedulers.subToMain());
    }
}
