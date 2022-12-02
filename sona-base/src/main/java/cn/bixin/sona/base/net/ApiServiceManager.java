package cn.bixin.sona.base.net;


import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;

public class ApiServiceManager {
    private static final String TAG = ApiServiceManager.class.getSimpleName();

    private static volatile ApiServiceManager mApiServiceManager = null;
    private final Map<String, Object> mServiceMap = new ConcurrentHashMap<>();
    private static IRetrofitFactory mRetrofitFactory;

    public static ApiServiceManager getInstance() {
        if (mApiServiceManager == null) {
            synchronized (ApiServiceManager.class) {
                if (mApiServiceManager == null) {
                    mApiServiceManager = new ApiServiceManager();
                }
            }
        }
        return mApiServiceManager;
    }

    private ApiServiceManager() {
        mRetrofitFactory = new RetrofitFactory();
    }

    public <T> T obtainService(Class<T> service) {
        return obtainService(service, null);
    }

    public <T> T obtainService(Class<T> service, String newHost) {
        if (mServiceMap.containsKey(service.getName())) {
            return (T) mServiceMap.get(service.getName());
        }
        Host host = service.getAnnotation(Host.class);

        String hostUrl;
        if (!TextUtils.isEmpty(newHost)) {
            hostUrl = newHost;
        } else {
            hostUrl = host.value();
        }

        putMap(service, mRetrofitFactory.get(hostUrl));

        return (T) mServiceMap.get(service.getName());
    }

    private <T> void putMap(Class<T> service, Retrofit retrofit) {
        if (retrofit != null && !mServiceMap.containsKey(service.getName())) {
            mServiceMap.put(service.getName(), retrofit.create(service));
        }
    }

    public void onDestroy() {
        mApiServiceManager = null;
        if (mRetrofitFactory != null) {
            mRetrofitFactory.clear();
        }
        mServiceMap.clear();
    }
}
