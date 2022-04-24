package com.newstar.scorpiodata.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ConvertUtils {
    public static <T> List<T> removeDuplicate(List<T> list) {
        Set<T> newSet = new TreeSet<>(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return String.valueOf(o1.hashCode()).compareTo(String.valueOf(o2.hashCode()));
            }
        });
        newSet.addAll(list);
        return new ArrayList<>(newSet);
    }
}