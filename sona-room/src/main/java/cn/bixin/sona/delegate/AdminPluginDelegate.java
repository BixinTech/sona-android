package cn.bixin.sona.delegate;

import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import cn.bixin.sona.api.ApiSubscriber;
import cn.bixin.sona.api.SonaApi;
import cn.bixin.sona.base.net.ApiException;
import cn.bixin.sona.component.ComponentMessage;
import cn.bixin.sona.component.connection.ConnectionMessage;
import cn.bixin.sona.component.connection.MessageGroupEnum;
import cn.bixin.sona.component.connection.MessageItemEnum;
import cn.bixin.sona.data.entity.SonaRoomData;
import cn.bixin.sona.delegate.internal.PluginError;
import cn.bixin.sona.driver.RoomDriver;
import cn.bixin.sona.plugin.AdminPlugin;
import cn.bixin.sona.plugin.PluginCallback;
import cn.bixin.sona.plugin.SonaPlugin;
import cn.bixin.sona.plugin.config.AdminConfig;
import cn.bixin.sona.plugin.entity.MuteEntity;
import cn.bixin.sona.plugin.entity.OnlineUserData;
import cn.bixin.sona.plugin.entity.PluginEntity;
import cn.bixin.sona.plugin.entity.PluginEnum;
import cn.bixin.sona.plugin.internal.OnlineUserCallback;
import cn.bixin.sona.plugin.internal.OnlineUserNumberCallback;
import cn.bixin.sona.plugin.observer.AdminPluginObserver;
import cn.bixin.sona.util.NumberParse;

public class AdminPluginDelegate extends SonaPluginDelegate implements AdminPlugin, AdminPluginObserver {

    private RoomDriver roomDriver;

    private AdminPluginObserver observer;

    public AdminPluginDelegate(RoomDriver roomDriver) {
        this.roomDriver = roomDriver;
    }

    @Override
    public void handleMessage(ComponentMessage msgType, Object message) {
        switch (msgType) {
            case CONNECT_REV_MESSAGE:
                ConnectionMessage connectionMessage = (ConnectionMessage) message;
                MessageGroupEnum groupEnum = connectionMessage.getGroup();
                switch (groupEnum) {
                    case ADMIN:
                        // 管理消息
                        if (!TextUtils.isEmpty(connectionMessage.getMessage())) {
                            PluginEntity adminEntity;
                            try {
                                JSONObject jsonObject = JSONObject.parseObject(connectionMessage.getMessage());
                                String uid = jsonObject.getString("uid");
                                String roomId = jsonObject.getString("roomId");
                                MessageItemEnum itemEnum = connectionMessage.getItem();
                                switch (itemEnum) {
                                    case ADMIN_SET_CANCEL:
                                        int isAdmin = jsonObject.getIntValue("isAdmin");
                                        adminEntity = new PluginEntity(uid, roomId);
                                        onAdministratorChange(isAdmin, adminEntity);
                                        break;
                                    case MUTE_SET_CANCEL:
                                        int isMute = jsonObject.getIntValue("isMute");
                                        int duration = jsonObject.getIntValue("duration");
                                        if (isMute != 1) {
                                            duration = 0;
                                        }
                                        adminEntity = new MuteEntity(uid, roomId, duration);
                                        onUserMuteChange(isMute, (MuteEntity) adminEntity);
                                        break;
                                    case BLACK_SET_CANCEL:
                                        int isBlock = jsonObject.getIntValue("isBlock");
                                        adminEntity = new PluginEntity(uid, roomId);
                                        onUserBlockChange(isBlock, adminEntity);
                                        break;
                                    case KICK:
                                         adminEntity = new PluginEntity(uid, roomId);
                                         onUserKick(adminEntity);
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
                break;
            case USER_KICK:
                if (message instanceof PluginEntity) {
                    onUserKick((PluginEntity) message);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setAdmin(String uid, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.setAdmin(roomDriver.acquire(SonaRoomData.class).roomId, uid).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_SET_ADMIN_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_SET_ADMIN_ERROR, "服务器错误");
                        }
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
    public void cancelAdmin(String uid, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.cancelAdmin(roomDriver.acquire(SonaRoomData.class).roomId, uid).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_CANCEL_ADMIN_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_CANCEL_ADMIN_ERROR, "服务器错误");
                        }
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
    public void black(String uid, String reason, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.black(roomDriver.acquire(SonaRoomData.class).roomId, uid, reason).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_SET_BLOCK_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_SET_BLOCK_ERROR, "服务器错误");
                        }
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
    public void cancelBlack(String uid, String reason, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.cancelBlack(roomDriver.acquire(SonaRoomData.class).roomId, uid, reason).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_CANCEL_BLOCK_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_CANCEL_BLOCK_ERROR, "服务器错误");
                        }
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
    public void mute(String uid, int minute, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.mute(roomDriver.acquire(SonaRoomData.class).roomId, uid, minute).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_SET_MUTE_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_SET_MUTE_ERROR, "服务器错误");
                        }
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
    public void cancelMute(String uid, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.cancelMute(roomDriver.acquire(SonaRoomData.class).roomId, uid).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_CANCEL_MUTE_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_CANCEL_MUTE_ERROR, "服务器错误");
                        }
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
    public void silent(List<String> uids, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.silent(roomDriver.acquire(SonaRoomData.class).roomId, uids).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_SET_SILENT_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_SET_SILENT_ERROR, "服务器错误");
                        }
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
    public void cancelSilent(List<String> uids, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.cancelSilent(roomDriver.acquire(SonaRoomData.class).roomId, uids).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_CANCEL_SILENT_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_CANCEL_SILENT_ERROR, "服务器错误");
                        }
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
    public void kick(String uid, PluginCallback pluginCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.kick(roomDriver.acquire(SonaRoomData.class).roomId, uid).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean b) {
                    super.onSuccess(b);
                    if (pluginCallback != null) {
                        pluginCallback.onSuccess();
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    super.onFailure(e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_KICK_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (pluginCallback != null) {
                            pluginCallback.onFailure(PluginError.SERVER_KICK_ERROR, "服务器错误");
                        }
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
    public void queryOnlineUsers(String anchor, int limit, OnlineUserCallback onlineUserCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.getOnlineUser(roomDriver.acquire(SonaRoomData.class).roomId, anchor, limit).subscribeWith(new ApiSubscriber<OnlineUserData>() {
                @Override
                protected void onSuccess(OnlineUserData onlineUserData) {
                    if (onlineUserCallback != null) {
                        onlineUserCallback.onSuccess(onlineUserData);
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (onlineUserCallback != null) {
                            onlineUserCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_ONLINE_LIST_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (onlineUserCallback != null) {
                            onlineUserCallback.onFailure(PluginError.SERVER_ONLINE_LIST_ERROR, "服务器错误");
                        }
                    }
                }
            }));
        } else {
            if (onlineUserCallback != null) {
                onlineUserCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void queryOnlineUserNumber(OnlineUserNumberCallback onlineUserNumberCallback) {
        if (roomDriver.getServerCertified()) {
            register(SonaApi.getOnlineNumber(roomDriver.acquire(SonaRoomData.class).roomId).subscribeWith(new ApiSubscriber<Integer>() {
                @Override
                protected void onSuccess(Integer integer) {
                    if (onlineUserNumberCallback != null) {
                        onlineUserNumberCallback.onSuccess(integer);
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (onlineUserNumberCallback != null) {
                            onlineUserNumberCallback.onFailure(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_ONLINE_COUNT_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (onlineUserNumberCallback != null) {
                            onlineUserNumberCallback.onFailure(PluginError.SERVER_ONLINE_COUNT_ERROR, "服务器错误");
                        }
                    }
                }
            }));
        } else {
            if (onlineUserNumberCallback != null) {
                onlineUserNumberCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void observe(AdminPluginObserver observer) {
        this.observer = observer;
    }

    @Override
    public SonaPlugin config(AdminConfig config) {
        roomDriver.provide(config);
        return this;
    }

    @Override
    public void onAdministratorChange(int admin, PluginEntity entity) {
        if (roomDriver.getServerCertified()) {
            roomDriver.runUiThread(() -> {
                if (observer != null) {
                    observer.onAdministratorChange(admin, entity);
                }
            });
        }
    }

    @Override
    public void onUserBlockChange(int block, PluginEntity entity) {
        if (roomDriver.getServerCertified()) {
            roomDriver.runUiThread(() -> {
                if (observer != null) {
                    observer.onUserBlockChange(block, entity);
                }
            });
        }
    }

    @Override
    public void onUserMuteChange(int mute, MuteEntity entity) {
        if (roomDriver.getServerCertified()) {
            roomDriver.runUiThread(() -> {
                if (observer != null) {
                    observer.onUserMuteChange(mute, entity);
                }
            });
        }
    }

    @Override
    public void onUserKick(PluginEntity entity) {
        if (roomDriver.getServerCertified()) {
            roomDriver.runUiThread(() -> {
                if (observer != null) {
                    observer.onUserKick(entity);
                }
            });
        }
    }

    @Override
    public PluginEnum pluginType() {
        return PluginEnum.ADMIN;
    }

    @Override
    public void remove() {
        super.remove();
        roomDriver.remove(AdminConfig.class);
        observer = null;
    }
}
