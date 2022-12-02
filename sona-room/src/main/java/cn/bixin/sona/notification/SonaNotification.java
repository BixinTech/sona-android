package cn.bixin.sona.notification;

import android.app.Notification;
import android.os.Parcel;

import androidx.annotation.NonNull;

/**
 * Sona 通知 Notification
 *
 * @Author luokun
 * @Date 2020/11/18
 */
public class SonaNotification extends Notification {

    private Notification origin;
    private int notificationId;

    public SonaNotification(@NonNull Notification origin, int notificationId) {
        this.origin = origin;
        this.notificationId = notificationId;
    }

    public Notification getOrigin() {
        return origin;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public final boolean notificationIdValid() {
        return notificationId != 0;
    }

    protected SonaNotification(Parcel in) {
        origin = in.readParcelable(Notification.class.getClassLoader());
        notificationId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(origin, flags);
        parcel.writeInt(notificationId);
    }

    public static final Creator<SonaNotification> CREATOR = new Creator<SonaNotification>() {

        @Override
        public SonaNotification createFromParcel(Parcel source) {
            return new SonaNotification(source);
        }

        @Override
        public SonaNotification[] newArray(int size) {
            return new SonaNotification[size];
        }
    };
}
