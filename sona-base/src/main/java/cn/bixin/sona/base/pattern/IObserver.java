package cn.bixin.sona.base.pattern;

public interface IObserver<T> {
    void onChanged(T value);
}
