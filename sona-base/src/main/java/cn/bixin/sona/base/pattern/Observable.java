package cn.bixin.sona.base.pattern;

public interface Observable<T> {

    void addObserver(IObserver<T> observer);

    void removeObserver(IObserver<T> observer);

    void update(Setter<T> setter);
}
