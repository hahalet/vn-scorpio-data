package com.newstar.scorpiodata.utils;

public interface Callback<T, K> {
    void reject(K err);
    void resolve(T res);
}