package cn.bixin.sona.util;

import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxSchedulers {

    private static final FlowableTransformer<?, ?> subToMain
            = flowable -> flowable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    public static <T> FlowableTransformer<T, T> subToMain() {
        return (FlowableTransformer<T, T>) subToMain;
    }
}
