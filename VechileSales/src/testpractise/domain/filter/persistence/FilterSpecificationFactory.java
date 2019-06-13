package com.dizzion.portal.domain.filter.persistence;

import com.dizzion.portal.domain.common.persistence.CriteriaPathFactory;
import com.dizzion.portal.domain.filter.FieldFilter;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FilterSpecificationFactory<T> {
    private final FiltersMapper<T> filtersMapper;
    private final CriteriaPathFactory pathFactory;

    public FilterSpecificationFactory(FiltersMapper<T> filtersMapper,
                                      CriteriaPathFactory pathFactory) {
        this.filtersMapper = filtersMapper;
        this.pathFactory = pathFactory;
    }

    public Specifications<T> specificationFor(Class<T> entityClass, Set<FieldFilter> filters) {
        return Specifications.where(new FilterSpecification<>(filters, entityClass, filtersMapper, pathFactory));
    }
}
