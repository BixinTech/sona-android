package cn.bixin.sona.base.net;

import retrofit2.Retrofit;

public interface IRetrofitFactory {

    Retrofit get(String host);

    void clear();
}
