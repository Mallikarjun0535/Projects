package com.dizzion.portal.domain.common;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@UtilityClass
public class PageUtils {
    public static <T> Page<T> emptyPage() {
        return new PageImpl<>(emptyList());
    }

    public static <T> Page<T> pageOf(T[] items) {
        return new PageImpl<>(asList(items));
    }

    public static <T> Page<T> pageOf(T item) {
        return new PageImpl<>(singletonList(item));
    }
}
