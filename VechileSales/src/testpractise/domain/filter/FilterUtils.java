package com.dizzion.portal.domain.filter;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Optional;

@UtilityClass
public class FilterUtils {
    public static Optional<FieldFilter> findFilter(Collection<FieldFilter> filters, String filterKey) {
        return filters.stream().filter(filter -> filter.getKey().equals(filterKey)).findAny();
    }
}
