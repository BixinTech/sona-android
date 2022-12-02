package cn.bixin.sona.base;

import android.app.Application;

public class Sona {
    private static Application sApplication;

    private static boolean sShowLog = false;

    private static String sUid = "";

    public static void init(Application app) {
        sApplication = app;
    }


    public static Application getAppContext() {
        return sApplication;
    }


    public static void openLog() {
        sShowLog = true;
    }

    public static void setTestEnv() {

    }

    public static boolean showLog() {
        return sShowLog;
    }

    public static void initUserInfo(String uid) {
        sUid = uid;
    }

    public static String getUid() {
        return sUid;
    }

}
