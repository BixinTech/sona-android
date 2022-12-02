package cn.bixin.sona.delegate;

import org.jetbrains.annotations.NotNull;

import cn.bixin.sona.component.ComponentCallback;
import cn.bixin.sona.component.ComponentMessage;
import cn.bixin.sona.component.connection.ConnectionMessage;
import cn.bixin.sona.component.connection.MessageGroupEnum;
import cn.bixin.sona.component.connection.MessageItemEnum;
import cn.bixin.sona.data.entity.SonaRoomData;
import cn.bixin.sona.delegate.helper.ConnectPresenter;
import cn.bixin.sona.delegate.helper.ConnectSender;
import cn.bixin.sona.delegate.internal.PluginError;
import cn.bixin.sona.delegate.observer.ConnectReconnectObserver;
import cn.bixin.sona.driver.ComponentType;
import cn.bixin.sona.driver.RoomDriver;
import cn.bixin.sona.plugin.ConnectPlugin;
import cn.bixin.sona.plugin.PluginCallback;
import cn.bixin.sona.plugin.SonaPlugin;
import cn.bixin.sona.plugin.config.ConnectConfig;
import cn.bixin.sona.plugin.entity.MessageEntity;
import cn.bixin.sona.plugin.entity.PluginEnum;
import cn.bixin.sona.plugin.internal.ConnectMessage;
import cn.bixin.sona.plugin.observer.ConnectPluginObserver;
import cn.bixin.sona.report.ReportCode;
import cn.bixin.sona.util.SonaConstant;
import cn.bixin.sona.util.SonaLogger;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ConnectPluginDelegate extends SonaPluginDelegate implements ConnectPlugin, ConnectPluginObserver, ConnectSender {

    private RoomDriver roomDriver;
    private ConnectPluginObserver observer;
    private ConnectPresenter presenter;

    public ConnectPluginDelegate(RoomDriver roomDriver) {
        this.roomDriver = roomDriver;
        this.presenter = new ConnectPresenter(roomDriver, this);
    }

    @Override
    public void sendMessage(ConnectMessage connectMessage, PluginCallback pluginCallback) {
        sendMessage(connectMessage, SonaConstant.UNNEEDED_TO_SAVE_MESSAGE, pluginCallback);
    }

    @Override
    public void sendMessage(ConnectMessage connectMessage, int needToSave, PluginCallback pluginCallback) {
        if (roomDriver.getConnection() != null && roomDriver.acquire(SonaRoomData.class) != null) {
            if (!roomDriver.componentCertified(ComponentType.IM)) {
                SonaLogger.log("sendMessage 状态不对<1>", ReportCode.ROOM_STATUS_FAIL_CODE);
            }
            presenter.sendMessage(connectMessage, needToSave, roomDriver.acquire(SonaRoomData.class).roomId, pluginCallback);
        } else {
            if (roomDriver.getConnection() == null) {
                SonaLogger.log("sendMessage 状态不对<2>", ReportCode.ROOM_STATUS_FAIL_CODE);
            }
            if (pluginCallback != null) {
                pluginCallback.onFailure(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
            }
        }
    }

    @Override
    public void startReconnect(PluginCallback pluginCallback) {
        ConnectReconnectObserver connectReconnectObserver = new ConnectReconnectObserver(pluginCallback);
        roomDriver.provide(connectReconnectObserver);
        // 发送音频切换的消息，通知其他plugin
        roomDriver.dispatchMessage(ComponentMessage.IM_CHANGE_START, null);
        // 断开音频
        roomDriver.unAssembling(ComponentType.IM);
        // 打开音频
        roomDriver.assembling(ComponentType.IM);
    }

    @Override
    public void observe(ConnectPluginObserver observer) {
        this.observer = observer;
    }

    @Override
    public SonaPlugin config(ConnectConfig config) {
        roomDriver.provide(config);
        return this;
    }

    @Override
    public void onReceiveMessage(MessageEntity messageEntity) {
        if (roomDriver.componentCertified(ComponentType.IM)) {
            if (observer != null) {
                observer.onReceiveMessage(messageEntity);
            }
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
    public void onConnectError(int code) {
        if (observer != null) {
            observer.onConnectError(code);
        }
    }

    @Override
    public void handleMessage(ComponentMessage msgType, Object message) {
        switch (msgType) {
            case CONNECT_REV_MESSAGE:
                if (message instanceof ConnectionMessage) {
                    ConnectionMessage connectionMessage = (ConnectionMessage) message;
                    MessageGroupEnum groupEnum = connectionMessage.getGroup();
                    MessageItemEnum item = connectionMessage.getItem();
                    String msg = connectionMessage.getMessage();
                    switch (groupEnum) {
                        case BASIC:
                            // 基本消息（文本、图片、emoji、音频、视频）
                            onReceiveMessage(new MessageEntity(MessageEntity.MessageType.Role.map(item.getValue()), msg));
                            break;
                        case CUSTOM:
                            // custom消息
                            onReceiveMessage(new MessageEntity(MessageEntity.MessageType.CUSTOM, msg));
                            break;
                        case COMMAND:
                            // cmd消息
                            onReceiveMessage(new MessageEntity(MessageEntity.MessageType.COMMAND, msg));
                            break;
                        default:
                            break;
                    }
                }
                break;
            case CONNECT_DISCONNECT:
                onDisconnect();
                break;
            case CONNECT_RECONNECT:
                onReconnect();
                break;
            case CONNECT_ERROR:
                onConnectError((Integer) message);
                break;
            case CONNECT_INIT_SUCCESS: {
                roomDriver.dispatchMessage(ComponentMessage.IM_CHANGE_END, null);
                ConnectReconnectObserver connectReconnectObserver = roomDriver.acquire(ConnectReconnectObserver.class);
                if (connectReconnectObserver != null) {
                    connectReconnectObserver.onSuccess();
                    roomDriver.remove(ConnectReconnectObserver.class);
                }
            }
            break;
            case CONNECT_INIT_FAIL: {
                ConnectReconnectObserver connectReconnectObserver = roomDriver.acquire(ConnectReconnectObserver.class);
                if (connectReconnectObserver != null) {
                    connectReconnectObserver.onFailure(PluginError.ROOM_COMPONENT_CONNECT_RECONNECT_ERROR, (String) message);
                    roomDriver.remove(ConnectReconnectObserver.class);
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    public PluginEnum pluginType() {
        return PluginEnum.CONNECT;
    }

    @Override
    public void send(@NotNull String message, @NotNull Function2<? super Integer, ? super String, Unit> callback) {
        if (roomDriver.getConnection() != null) {
            roomDriver.getConnection().sendMessage(message, new ComponentCallback() {

                @Override
                public void executeSuccess() {
                    callback.invoke(0, "");
                }

                @Override
                public void executeFailure(int code, String reason) {
                    callback.invoke(code, reason);
                }
            });
        } else {
            callback.invoke(PluginError.ROOM_STATUS_ERROR, "房间状态不对");
        }
    }

    @Override
    public void remove() {
        super.remove();
        roomDriver.remove(ConnectConfig.class);
        observer = null;
    }
}
