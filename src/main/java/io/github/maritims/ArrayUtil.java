package io.github.maritims;

import java.lang.reflect.Array;

public class ArrayUtil {
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyIfNull(T[] array, Class<T> clazz) {
        if(array == null) {
            array = (T[]) Array.newInstance(clazz, 0);
        }
        return array;
    }
}
