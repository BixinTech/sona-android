package cn.bixin.sona;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Map;

import cn.bixin.sona.delegate.SonaRoomDelegate;
import cn.bixin.sona.notification.SonaNotificationBuilder;
import cn.bixin.sona.plugin.SonaPlugin;

public class SonaRoom implements SonaRoomBasic {

    private SonaRoomDelegate sonaRoomDelegate;

    public SonaRoom() {
        sonaRoomDelegate = new SonaRoomDelegate();
    }

    /**
     * 添加插件
     *
     * @param plugin
     * @param <T>
     * @return
     */
    public final <T extends SonaPlugin> T addPlugin(Class<T> plugin) {
        return sonaRoomDelegate.addPlugin(plugin);
    }

    /**
     * 获取插件
     *
     * @param plugin 插件class
     * @param <T>
     * @return
     */
    @Nullable
    public final <T extends SonaPlugin> T getPlugin(Class<T> plugin) {
        return sonaRoomDelegate.getPlugin(plugin);
    }

    /**
     * 查询插件状态
     *
     * @param plugin 插件
     * @param <T>
     * @return
     */
    public final <T extends SonaPlugin> PluginResult getPluginResult(Class<T> plugin) {
        return sonaRoomDelegate.getPluginResult(plugin, PluginResult.Action.LOAD);
    }

    /**
     * 查询插件状态
     *
     * @param plugin 插件
     * @param action 需要查询的状态
     * @param <T>
     * @return
     */
    public final <T extends SonaPlugin> PluginResult getPluginResult(Class<T> plugin, PluginResult.Action action) {
        return sonaRoomDelegate.getPluginResult(plugin, action);
    }

    @Override
    public void createRoom(String roomTitle, SonaRoomProduct productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback) {
        sonaRoomDelegate.createRoom(roomTitle, productCode, password, ext, sonaRoomCallback);
    }

    @Override
    public void openRoom(String roomId, SonaRoomCallback sonaRoomCallback) {
        sonaRoomDelegate.openRoom(roomId, sonaRoomCallback);
    }

    @Override
    public void closeRoom(String roomId, SonaRoomCallback sonaRoomCallback) {
        sonaRoomDelegate.closeRoom(roomId, sonaRoomCallback);
    }

    @Override
    public void enterRoom(String roomId, SonaRoomProduct productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback) {
        sonaRoomDelegate.enterRoom(roomId, productCode, password, ext, sonaRoomCallback);
    }

    @Override
    public void enterRoom(String roomId, String productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback) {
        sonaRoomDelegate.enterRoom(roomId, productCode, password, ext, sonaRoomCallback);
    }

    @Override
    public void updateRoomPassword(String roomId, String oldPassword, String newPassword, SonaRoomCallback sonaRoomCallback) {
        sonaRoomDelegate.updateRoomPassword(roomId, oldPassword, newPassword, sonaRoomCallback);
    }

    @Override
    public void leaveRoom(SonaRoomCallback sonaRoomCallback) {
        sonaRoomDelegate.leaveRoom(sonaRoomCallback);
    }

    /**
     * 设置房间状态回调
     *
     * @param sonaRoomObserver
     */
    public void observe(SonaRoomObserver sonaRoomObserver) {
        sonaRoomDelegate.observe(sonaRoomObserver);
    }

    /**
     * 设置房间错误事件回调
     *
     * @param sonaRoomErrorObserver
     */
    public void observeError(SonaRoomErrorObserver sonaRoomErrorObserver) {
        sonaRoomDelegate.observeError(sonaRoomErrorObserver);
    }

    /**
     * 开启前台服务：最大限度保证在后台推流不会被系统限制
     *
     * @param context
     * @param notificationBuilder
     */
    public void startForegroundService(Context context, NotificationCompat.Builder notificationBuilder) {
        Intent intent = new Intent(context, ForegroundNotificationService.class);
        if (notificationBuilder instanceof SonaNotificationBuilder) {
            // do nothing
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationBuilder.setChannelId(ForegroundNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_ID);
            }
        }
        intent.putExtra(ForegroundNotificationService.EXTRA_ID, notificationBuilder.build());
        try {
            context.startService(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭前台服务
     *
     * @param context
     */
    public void stopForegroundService(Context context) {
        context.stopService(new Intent(context, ForegroundNotificationService.class));
    }
}
