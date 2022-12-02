package cn.bixin.sona.delegate;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.bixin.sona.PluginResult;
import cn.bixin.sona.RoomEntity;
import cn.bixin.sona.RoomEvent;
import cn.bixin.sona.SonaRoomBasic;
import cn.bixin.sona.SonaRoomCallback;
import cn.bixin.sona.SonaRoomErrorObserver;
import cn.bixin.sona.SonaRoomObserver;
import cn.bixin.sona.SonaRoomProduct;
import cn.bixin.sona.api.ApiRegister;
import cn.bixin.sona.api.ApiSubscriber;
import cn.bixin.sona.api.SonaApi;
import cn.bixin.sona.base.net.ApiException;
import cn.bixin.sona.component.ComponentMessage;
import cn.bixin.sona.component.connection.ConnectionMessage;
import cn.bixin.sona.component.connection.MessageItemEnum;
import cn.bixin.sona.data.entity.RoomInfo;
import cn.bixin.sona.data.entity.SonaRoomData;
import cn.bixin.sona.data.entity.UserData;
import cn.bixin.sona.delegate.internal.PluginError;
import cn.bixin.sona.delegate.observer.RoomEnterObserver;
import cn.bixin.sona.driver.ComponentType;
import cn.bixin.sona.driver.RoomDriver;
import cn.bixin.sona.plugin.SonaPlugin;
import cn.bixin.sona.plugin.anotation.SonaPluginAnnotation;
import cn.bixin.sona.plugin.entity.PluginEnum;
import cn.bixin.sona.report.ReportCode;
import cn.bixin.sona.util.NumberParse;
import cn.bixin.sona.util.SonaLogger;

public class SonaRoomDelegate extends ApiRegister implements SonaRoomBasic {

    private final Map<String, Object> mPluginMap;
    private List<SonaPluginDelegate> mPlugins;
    private RoomDriver mRoomDriver;
    private SonaRoomObserver mRoomObserver;
    private SonaRoomErrorObserver mRoomErrorObserver;

    public SonaRoomDelegate() {
        mPlugins = new ArrayList<>();
        mRoomDriver = new RoomDriver(this);
        mPluginMap = new ConcurrentHashMap<>();
    }

    public final <T extends SonaPlugin> T addPlugin(Class<T> plugin) {
        if (mPluginMap.get(plugin.getName()) == null) {
            if (effectPlugin(plugin)) {
                create(plugin);
            } else {
                throw new IllegalArgumentException("暂不支持此插件");
            }
        }
        return (T) mPluginMap.get(plugin.getName());
    }

    public final <T extends SonaPlugin> T getPlugin(Class<T> plugin) {
        if (mPluginMap.get(plugin.getName()) != null) {
            return (T) mPluginMap.get(plugin.getName());
        }
        return null;
    }

    public final <T extends SonaPlugin> PluginResult getPluginResult(Class<T> plugin, PluginResult.Action action) {
        SonaPluginAnnotation annotation = plugin.getAnnotation(SonaPluginAnnotation.class);
        PluginEnum value = annotation.value();
        PluginResult pluginResult = new PluginResult(action, 1);
        if (action == PluginResult.Action.LOAD) {
            switch (value) {
                case AUDIO:
                case APLAYER:
                case CONNECT:
                    pluginResult.setCode(mRoomDriver.componentCertified(ComponentType.IM) ? 0 : 1);
                    break;
                case ADMIN:
                    pluginResult.setCode(mRoomDriver.getServerCertified() ? 0 : 1);
                    break;
            }
        } else if (action == PluginResult.Action.LEGAL) {
            if (pluginLoaded(value)) {
                switch (value) {
                    case AUDIO:
                    case APLAYER:
                    case CONNECT:
                        pluginResult.setCode(mRoomDriver.componentSupport(ComponentType.IM) ? 0 : 1);
                        break;
                    case ADMIN:
                        pluginResult.setCode(0);
                        break;
                }
            }
        }
        return pluginResult;
    }

    /**
     * 判断plugin是否有效: 注册则有效
     *
     * @param plugin
     * @param <T>
     * @return
     */
    private <T> boolean effectPlugin(Class<T> plugin) {
        SonaPluginAnnotation annotation = plugin.getAnnotation(SonaPluginAnnotation.class);
        if (annotation != null) {
            return true;
        }
        return false;
    }

    /**
     * 创建代理plugin
     *
     * @param plugin
     * @param <T>
     */
    private <T> void create(Class<T> plugin) {
        if (!mPluginMap.containsKey(plugin.getName())) {
            SonaLogger.print("createPlugin:>" + plugin.getName());
            SonaPluginAnnotation annotation = plugin.getAnnotation(SonaPluginAnnotation.class);
            PluginEnum value = annotation.value();
            mPlugins.add(createDelegate(value));
            mPluginMap.put(plugin.getName(), createPlugin(plugin, value));
        }
    }

    /**
     * 创建代理对象
     *
     * @param value
     * @return
     */
    private SonaPluginDelegate createDelegate(PluginEnum value) {
        SonaPluginDelegate delegate = null;
        switch (value) {
            case ADMIN:
                delegate = new AdminPluginDelegate(mRoomDriver);
                break;
            case CONNECT:
                delegate = new ConnectPluginDelegate(mRoomDriver);
                break;
            case AUDIO:
                delegate = new AudioPluginDelegate(mRoomDriver);
                break;
            case APLAYER:
                delegate = new AudioPlayerPluginDelegate(mRoomDriver);
                break;
            default:
                break;
        }
        return delegate;
    }

    /**
     * 动态代理，返回代理plugin
     *
     * @param plugin
     * @param <T>
     * @return
     */
    private <T> T createPlugin(Class<T> plugin, PluginEnum value) {
        return (T) Proxy.newProxyInstance(plugin.getClassLoader(), new Class<?>[]{plugin},
                (proxy, method, args) -> {
                    if (proxy instanceof SonaPlugin) {
                        // 真正调用处
                        SonaPluginDelegate sonaPluginDelegate = findPluginDelegate(value);
                        if (sonaPluginDelegate != null) {
                            return method.invoke(sonaPluginDelegate, args);
                        }
                    }
                    return null;
                });
    }

    /**
     * 查找plugin对应的代理plugin
     *
     * @param pluginEnum
     * @return
     */
    private SonaPluginDelegate findPluginDelegate(PluginEnum pluginEnum) {
        for (SonaPluginDelegate pluginDelegate : mPlugins) {
            if (pluginDelegate.pluginType() == pluginEnum) {
                return pluginDelegate;
            }
        }
        return null;
    }

    /**
     * 分发消息（带参数）
     *
     * @param msgType
     * @param message
     */
    public void dispatchMessage(ComponentMessage msgType, Object message) {
        for (SonaPluginDelegate pluginDelegate : mPlugins) {
            pluginDelegate.handleMessage(msgType, message);
        }

        // 自身消费
        if (msgType == ComponentMessage.COMPONENT_INIT_SUCCESS) {
            // 组件初始化成功
            RoomEnterObserver roomEnterObserver = mRoomDriver.acquire(RoomEnterObserver.class);
            SonaLogger.log(roomEnterObserver != null ? "进入房间成功<1>" : "进入房间成功<2>", ReportCode.ENTER_ROOM_SUCCESS_CODE);
            if (roomEnterObserver != null) {
                roomEnterObserver.onSuccess(mRoomDriver.acquire(SonaRoomData.class).roomId);
                mRoomDriver.remove(RoomEnterObserver.class);
            }
        } else if (msgType == ComponentMessage.COMPONENT_INIT_FAIL) {
            // 组件初始化失败
            RoomEnterObserver roomEnterObserver = mRoomDriver.acquire(RoomEnterObserver.class);
            if (roomEnterObserver != null) {
                roomEnterObserver.onFailed(PluginError.ROOM_COMPONENT_INIT_ERROR, (String) message);
                mRoomDriver.remove(RoomEnterObserver.class);
            }
        } else if (msgType == ComponentMessage.CONNECT_REV_MESSAGE) {
            // 某些消息，需要内部处理消费
            ConnectionMessage connectionMessage = (ConnectionMessage) message;
            MessageItemEnum messageItemEnum = connectionMessage.getItem();
            if (messageItemEnum == MessageItemEnum.ENTER_ROOM || messageItemEnum == MessageItemEnum.LEAVE_ROOM) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(connectionMessage.getMessage());
                    String uid = jsonObject.getString("uid");
                    String roomId = jsonObject.getString("roomId");
                    RoomEvent roomEvent;
                    if (messageItemEnum == MessageItemEnum.ENTER_ROOM) {
                        roomEvent = RoomEvent.USER_ENTER;
                    } else {
                        roomEvent = RoomEvent.USER_LEAVE;
                    }
                    RoomEntity roomEntity = new RoomEntity(roomId, uid);
                    mRoomDriver.runUiThread(() -> {
                        if (mRoomObserver != null) {
                            mRoomObserver.onRoomReceiveEvent(roomEvent, roomEntity);
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else if (messageItemEnum == MessageItemEnum.CLOSE_ROOM) {
                // 关闭消息，需要关闭房间
                mRoomDriver.runUiThread(() -> {
                    SonaRoomData sonaRoomData = mRoomDriver.acquire(SonaRoomData.class);
                    if (sonaRoomData != null) {
                        String roomId = sonaRoomData.roomId;
                        leaveRoom(null);
                        if (mRoomObserver != null) {
                            mRoomObserver.onRoomReceiveEvent(RoomEvent.ROOM_CLOSE, new RoomEntity(roomId, null));
                        }
                    }
                });
            }  else if (messageItemEnum == MessageItemEnum.BLACK_SET_CANCEL) {
                if (!TextUtils.isEmpty(connectionMessage.getMessage())) {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(connectionMessage.getMessage());
                        String uid = jsonObject.getString("uid");
                        int isBlock = jsonObject.getIntValue("isBlock");
                        if (isBlock == 1) {
                            UserData userData = mRoomDriver.acquire(UserData.class);
                            if (userData != null && TextUtils.equals(uid, userData.getUid())) {
                                // 如果是自己被拉黑，需要关闭房间
                                mRoomDriver.runUiThread(() -> closeDriver());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (msgType == ComponentMessage.ERROR_MSG) {
            mRoomDriver.runUiThread(() -> {
                if (mRoomErrorObserver != null) {
                    mRoomErrorObserver.onError((Integer) message);
                }
            });
        }
    }

    @Override
    public void createRoom(String roomTitle, SonaRoomProduct productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback) {
        register(SonaApi.createRoom(roomTitle, productCode.getProduct(), password, ext).subscribeWith(new ApiSubscriber<RoomInfo>() {
            @Override
            protected void onSuccess(RoomInfo roomInfo) {
                RoomEnterObserver enterObserver = new RoomEnterObserver(sonaRoomCallback);
                mRoomDriver.provide(enterObserver);
                enter(roomInfo);
            }

            @Override
            protected void onFailure(Throwable e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    if (sonaRoomCallback != null) {
                        sonaRoomCallback.onFailed(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_CREATE_ROOM_ERROR), apiException.getMessage());
                    }
                } else {
                    if (sonaRoomCallback != null) {
                        sonaRoomCallback.onFailed(PluginError.SERVER_CREATE_ROOM_ERROR, "服务器错误");
                    }
                }
            }
        }));
    }

    @Override
    public void openRoom(String roomId, SonaRoomCallback sonaRoomCallback) {
        if (!TextUtils.isEmpty(roomId)) {
            if (mRoomDriver.roomCertified()) {
                SonaLogger.print("已经在房间内");
                if (sonaRoomCallback != null) {
                    sonaRoomCallback.onSuccess(mRoomDriver.acquire(SonaRoomData.class).roomId);
                }
                return;
            }

            preClear();
            register(SonaApi.openRoom(roomId).subscribeWith(new ApiSubscriber<RoomInfo>() {
                @Override
                protected void onSuccess(RoomInfo roomInfo) {
                    RoomEnterObserver enterObserver = new RoomEnterObserver(sonaRoomCallback);
                    mRoomDriver.provide(enterObserver);
                    enter(roomInfo);
                }

                @Override
                protected void onFailure(Throwable e) {
                    // 如果是因为操作不对的错误，则暂时不离开房间，提供失败回调告知业务方
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(NumberParse.parseInt(apiException.getCode(), PluginError.ROOM_CLOSE_ROOM_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(PluginError.ROOM_CLOSE_ROOM_ERROR, "服务器错误");
                        }
                    }
                }
            }));
        } else {
            if (sonaRoomCallback != null) {
                sonaRoomCallback.onFailed(PluginError.ROOM_PARAM_ERROR, "参数错误");
            }
        }
    }

    @Override
    public void closeRoom(String roomId, SonaRoomCallback sonaRoomCallback) {
        if (!TextUtils.isEmpty(roomId)) {
            register(SonaApi.closeRoom(roomId).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean aBoolean) {
                    if (sonaRoomCallback != null) {
                        sonaRoomCallback.onSuccess(roomId);
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(NumberParse.parseInt(apiException.getCode(), PluginError.ROOM_CLOSE_ROOM_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(PluginError.ROOM_CLOSE_ROOM_ERROR, "服务器错误");
                        }
                    }
                }
            }));
        } else {
            if (sonaRoomCallback != null) {
                sonaRoomCallback.onFailed(PluginError.ROOM_PARAM_ERROR, "参数错误");
            }
        }
    }

    @Override
    public void enterRoom(String roomId, SonaRoomProduct productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback) {
        enterRoom(roomId, productCode.getProduct(), password, ext, sonaRoomCallback);
    }

    @Override
    public void enterRoom(String roomId, String productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback) {
        if (mRoomDriver.roomCertified()) {
            SonaLogger.print("已经在房间内");
            if (sonaRoomCallback != null) {
                sonaRoomCallback.onSuccess(mRoomDriver.acquire(SonaRoomData.class).roomId);
            }
            return;
        }

        preClear();

        register(SonaApi.enterRoom(roomId, productCode, password, ext).subscribeWith(new ApiSubscriber<RoomInfo>() {
            @Override
            protected void onSuccess(RoomInfo roomInfo) {
                RoomEnterObserver enterObserver = new RoomEnterObserver(sonaRoomCallback);
                mRoomDriver.provide(enterObserver);
                enter(roomInfo);
            }

            @Override
            protected void onFailure(Throwable e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    if (sonaRoomCallback != null) {
                        sonaRoomCallback.onFailed(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_ENTER_ROOM_ERROR), apiException.getMessage());
                    }
                } else {
                    String errorMessage = e == null ? "服务器错误" : e.getMessage() + "";
                    SonaLogger.log(errorMessage, ReportCode.ENTER_ROOM_EXCEPTION_CODE);
                    if (sonaRoomCallback != null) {
                        sonaRoomCallback.onFailed(PluginError.SERVER_ENTER_ROOM_ERROR, errorMessage);
                    }
                }
            }
        }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void leaveRoom(SonaRoomCallback sonaRoomCallback) {
        SonaRoomData sonaRoomData = mRoomDriver.acquire(SonaRoomData.class);
        if (sonaRoomData != null && !TextUtils.isEmpty(sonaRoomData.roomId)) {
            close();
            SonaApi.leaveRoom(sonaRoomData.roomId).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean aBoolean) {
                    if (sonaRoomCallback != null) {
                        sonaRoomCallback.onSuccess(sonaRoomData.roomId);
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(NumberParse.parseInt(apiException.getCode(), PluginError.ROOM_LEAVE_ROOM_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(PluginError.ROOM_LEAVE_ROOM_ERROR, "服务器错误");
                        }
                    }
                }
            });
        } else {
            SonaLogger.log("Sona leaveRoom SonaRoomData invalid");
            clear();
            if (sonaRoomCallback != null) {
                sonaRoomCallback.onFailed(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void updateRoomPassword(String roomId, String oldPassword, String newPassword, SonaRoomCallback sonaRoomCallback) {
        if (!TextUtils.isEmpty(roomId)) {
            register(SonaApi.updateRoomPassword(roomId, oldPassword, newPassword).subscribeWith(new ApiSubscriber<Boolean>() {
                @Override
                protected void onSuccess(Boolean success) {
                    if (sonaRoomCallback != null) {
                        if (success) {
                            sonaRoomCallback.onSuccess(roomId);
                        } else {
                            sonaRoomCallback.onFailed(PluginError.SERVER_UPDATE_PASSWORD_ERROR, "设置密码失败");
                        }
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(NumberParse.parseInt(apiException.getCode(), PluginError.SERVER_UPDATE_PASSWORD_ERROR), apiException.getMessage());
                        }
                    } else {
                        if (sonaRoomCallback != null) {
                            sonaRoomCallback.onFailed(PluginError.SERVER_UPDATE_PASSWORD_ERROR, "服务器错误");
                        }
                    }
                }
            }));
        } else {
            if (sonaRoomCallback != null) {
                sonaRoomCallback.onFailed(PluginError.ROOM_PARAM_ERROR, "参数错误");
            }
        }
    }

    /**
     * 数据装换
     *
     * @param roomInfo
     * @return
     */
    private SonaRoomData map(RoomInfo roomInfo) {
        SonaRoomData sonaRoomData = new SonaRoomData();
        sonaRoomData.roomId = roomInfo.getRoomId();
        sonaRoomData.guestUid = roomInfo.getGuestUid();
        sonaRoomData.addr = roomInfo.getAddr();
        sonaRoomData.nickname = roomInfo.getNickname();

        if (roomInfo.getProductConfig() != null) {
            sonaRoomData.productCode = roomInfo.getProductConfig().getProductCode();
            sonaRoomData.productCodeAlias = roomInfo.getProductConfig().getProductCodeAlias();
            sonaRoomData.imInfo = roomInfo.getProductConfig().getImConfig();
            sonaRoomData.streamInfo = roomInfo.getProductConfig().getStreamConfig();
        }
        if (roomInfo.getExtra() != null) {
            sonaRoomData.extra = roomInfo.getExtra();
        }
        return sonaRoomData;
    }

    /**
     * 进入房间
     *
     * @param roomInfo
     */
    private void enter(RoomInfo roomInfo) {
        SonaRoomData sonaRoomData = map(roomInfo);
        mRoomDriver.provide(sonaRoomData);
        mRoomDriver.setServerCertified(true);
        mRoomDriver.assembling();
    }

    /**
     * 关闭
     */
    private void close() {
        closeDriver();
        closeData();
    }

    /**
     * 关闭driver，关闭各个组件
     */
    private void closeDriver() {
        mRoomDriver.unAssembling();
    }

    /**
     * 释放数据
     */
    private void closeData() {
        for (SonaPluginDelegate sonaPluginDelegate : mPlugins) {
            sonaPluginDelegate.remove();
        }
        mRoomDriver.remove(SonaRoomData.class);
        mRoomDriver.clear();
        mRoomDriver.setServerCertified(false);
        clear();
    }

    private void preClear() {
        SonaRoomData sonaRoomData = mRoomDriver.acquire(SonaRoomData.class);
        if (sonaRoomData != null && !TextUtils.isEmpty(sonaRoomData.roomId)) {
            mRoomDriver.unAssembling();
            mRoomDriver.remove(SonaRoomData.class);
            mRoomDriver.setServerCertified(false);
            clear();
        }
    }

    /**
     * 插件是否已加载
     *
     * @param pluginEnum
     * @return
     */
    public boolean pluginLoaded(PluginEnum pluginEnum) {
        for (SonaPluginDelegate pluginDelegate : mPlugins) {
            if (pluginDelegate.pluginType() == pluginEnum) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置房间回调
     *
     * @param sonaRoomObserver
     */
    public void observe(SonaRoomObserver sonaRoomObserver) {
        this.mRoomObserver = sonaRoomObserver;
    }

    /**
     * 设置房间错误事件回调
     *
     * @param sonaRoomErrorObserver
     */
    public void observeError(SonaRoomErrorObserver sonaRoomErrorObserver) {
        this.mRoomErrorObserver = sonaRoomErrorObserver;
    }
}
