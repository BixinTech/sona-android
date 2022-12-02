package cn.bixin.sona.component.internal.audio.zego;

import android.app.Application;

import androidx.annotation.NonNull;

import cn.bixin.sona.base.Sona;

/**
 * @Author luokun
 * @Date 2020-01-02
 */
public class ZegoContextObserver extends ZegoSampleCallback.ContextObserver {
    @NonNull
    @Override
    public Application getAppContext() {
        return Sona.getAppContext();
    }

    @Override
    public long getLogFileSize() {
        return 10 * 1024 * 1024;
    }
}
