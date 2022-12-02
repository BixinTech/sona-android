package cn.bixin.sona.base.pattern;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Provider implements IProvider {
    private Map<Class, Object> provision = new ConcurrentHashMap<>();
    private Map<Class, Observable> observableMap = new ConcurrentHashMap<>();

    @Override
    public void provide(Object object) {
        provision.put(object.getClass(), object);
    }

    @Override
    @Nullable
    public <T> T acquire(Class<T> clz) {
        try {
            return (T) provision.get(clz);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public <T> void remove(Class<T> clz) {
        provision.remove(clz);
    }

    @Override
    public void clear() {
        provision.clear();
        observableMap.clear();
    }

    @Override
    public <T> Observable<T> observe(Class<T> clz) {
        Observable<T> observable = observableMap.get(clz);
        if (observable == null) {
            observable = new ProviderObservable<>(clz, this);
            observableMap.put(clz, observable);
        }
        return observable;
    }

    public void clearObservable() {
        observableMap.clear();
    }

    private static class ProviderObservable<M> implements Observable<M> {
        private Set<IObserver<M>> observers = Collections.synchronizedSet(new HashSet<>());
        private final Class<M> clz;
        private final WeakReference<IProvider> weakReference;

        ProviderObservable(Class<M> clz, IProvider provider) {
            this.clz = clz;
            this.weakReference = new WeakReference<>(provider);
        }

        @Override
        public void addObserver(IObserver<M> observer) {
            observers.add(observer);
        }

        @Override
        public void removeObserver(IObserver<M> observer) {
            observers.remove(observer);
        }

        @Override
        public void update(Setter<M> setter) {
            IProvider iProvider = weakReference.get();
            if (iProvider == null) {
                return;
            }
            M instance = iProvider.acquire(clz);
            M updated = setter.update(instance);
            if (updated != null) {
                iProvider.provide(updated);
            }
            synchronized (observers) {
                try {
                    for (IObserver<M> observer : observers) {
                        observer.onChanged(updated);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
