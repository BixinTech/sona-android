package cn.bixin.sona.api;

import io.reactivex.subscribers.DisposableSubscriber;

public class ApiSubscriber<T> extends DisposableSubscriber<T> {

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    @Override
    public void onError(Throwable t) {
        try {
            onFailure(t);
        } catch (Throwable e) {

        }
    }

    @Override
    public void onComplete() {

    }

    protected void onSuccess(T t) {
    }

    protected void onFailure(Throwable e) {
    }
}
