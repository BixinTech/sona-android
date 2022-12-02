package cn.bixin.sona;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import cn.bixin.sona.notification.SonaNotification;
import cn.bixin.sona.util.SonaLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 前台服务
 *
 * @Author luokun
 * @Date 2020/11/16
 */
public class ForegroundNotificationService extends Service {

    public static final String EXTRA_ID = "ForegroundNotificationService";

    // Android 7.0 之后，显示通知时指定通知频道，由于管理通知类型
    public static final String FOREGROUND_NOTIFICATION_CHANNEL_ID = "foreground_channel_id";
    public static final String FOREGROUND_NOTIFICATION_CHANNEL_NAME = "前台服务通知";

    // PendingIntent 的 requestCode
    private static final int FOREGROUND_NOTIFICATION_PENDING_INTENT_REQUEST_CODE = 0x10;
    // 前台通知ID
    private static final int FOREGROUND_NOTIFICATION_ID = 0xc000;

    private NotificationManager mNotificationManager;

    private List<Integer> outNotificationIdList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取 NotificationManager 对象，用户显示通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建前台服务通知频道
            NotificationChannel channel = new NotificationChannel(ForegroundNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_ID,
                    ForegroundNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 显示通知，并且设置当前服务为前台服务
        SonaLogger.print("ForegroundNotificationService#onCreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SonaLogger.print("ForegroundNotificationService#onStartCommand");
        if (intent != null) {
            Parcelable parcelableExtra = intent.getParcelableExtra(EXTRA_ID);
            if (parcelableExtra != null && parcelableExtra instanceof Notification) {
                showNotification((Notification) parcelableExtra);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 当服务被销毁的同时，也取消对应的通知
        mNotificationManager.cancel(FOREGROUND_NOTIFICATION_ID);
        for (Integer id : outNotificationIdList) {
            mNotificationManager.cancel(id);
        }
        outNotificationIdList.clear();
    }

    private void showNotification(Notification notification) {
        // 显示通知（前台服务需要依赖于通知）
        SonaLogger.print("ForegroundNotificationService#showNotification");
        if (notification instanceof SonaNotification) {
            SonaNotification sonaNotification = (SonaNotification)notification;
            Notification originNotification = sonaNotification.getOrigin();
            if (sonaNotification.notificationIdValid()) {
                int id = sonaNotification.getNotificationId();
                outNotificationIdList.add(id);
                mNotificationManager.notify(id, originNotification);
                // 将当前服务设置为前台服务
                startForeground(id, originNotification);
            } else {
                mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, originNotification);
                // 将当前服务设置为前台服务
                startForeground(FOREGROUND_NOTIFICATION_ID, originNotification);
            }
        } else {
            mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
            // 将当前服务设置为前台服务
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        }

    }
}
