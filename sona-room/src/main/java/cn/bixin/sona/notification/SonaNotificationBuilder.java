package cn.bixin.sona.notification;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import cn.bixin.sona.ForegroundNotificationService;


/**
 * Sona 通知builder
 *
 * @Author luokun
 * @Date 2020/11/18
 */
public class SonaNotificationBuilder extends NotificationCompat.Builder {

    private int flag = 0;
    private int notificationId = 0;
    private NotificationCompat.Builder builder = null;

    public SonaNotificationBuilder(@NonNull Context context, @NonNull String channelId) {
        super(context, channelId);
    }

    public SonaNotificationBuilder(@NonNull Context context) {
        super(context, ForegroundNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_ID);
    }

    public SonaNotificationBuilder(@NonNull Context context, NotificationCompat.Builder builder) {
        super(context, ForegroundNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_ID);
        this.builder = builder;
    }

    /**
     * 设置flag: 同Notification 的flag
     *
     * @param flag
     * @return
     */
    public SonaNotificationBuilder setFlag(int flag) {
        this.flag = flag;
        return this;
    }

    /**
     * 设置通知id
     *
     * @param notificationId
     * @return
     */
    public SonaNotificationBuilder setNotificationId(int notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    @Override
    public Notification build() {
        Notification notification;
        if (builder != null) {
            notification = builder.build();
        } else {
            notification = super.build();
        }
        notification.flags |= flag;
        SonaNotification sonaNotification = new SonaNotification(notification, notificationId);
        return sonaNotification;
    }
}
