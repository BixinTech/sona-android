package cn.bixin.sona.base.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory implements IRetrofitFactory {
    private final static Map<String, Retrofit> retrofits = new ConcurrentHashMap<>();

    @Override
    public Retrofit get(String host) {
        if (retrofits.containsKey(host)) {
            return retrofits.get(host);
        } else {
            Retrofit retrofit = create(host);
            retrofits.put(host, retrofit);
        }
        return retrofits.get(host);
    }

    @Override
    public void clear() {
        retrofits.clear();
    }

    private Retrofit create(String host) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(host)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        return builder.build();
    }

    private OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15000, TimeUnit.MILLISECONDS)
                .readTimeout(15000, TimeUnit.MILLISECONDS)
                .writeTimeout(15000, TimeUnit.MILLISECONDS)
                .addInterceptor(logInterceptor);
        return builder.build();
    }

}
