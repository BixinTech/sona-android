package cn.bixin.sona.base.pattern;

import androidx.annotation.Nullable;

public interface Setter<T> {
    T update(@Nullable T t);
}
