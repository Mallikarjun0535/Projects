package com.dizzion.portal.domain.filter.persistence;

import com.dizzion.portal.domain.common.persistence.CriteriaPathFactory;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.filter.FieldFilter.LogicOperator;
import com.dizzion.portal.domain.filter.FieldFilter.Operator;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FilterSpecification<T> implements Specification<T> {
    private final FiltersMapper<T> mapper;
    private final CriteriaPathFactory pathFactory;
    private final Class<T> clazz;
    private final Set<FieldFilter> filters;

    public FilterSpecification(Set<FieldFilter> filters, Class<T> clazz, FiltersMapper<T> mapper, CriteriaPathFactory criteriaPathFactory) {
        this.filters = filters;
        this.mapper = mapper;
        this.clazz = clazz;
        this.pathFactory = criteriaPathFactory;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        query.distinct(true);
        List<Predicate> andPredicates = new ArrayList<>();
        List<Predicate> orPredicates = new ArrayList<>();
        filters.forEach(filter -> {
            Object typedValue = mapper.map(clazz, filter.getKey(), filter.getValue());
            Path fieldPath = pathFactory.criteriaPathFromString(filter.getKey(), root);
            Predicate predicate = buildPredicate(fieldPath, filter.getOperator(), typedValue, cb);
            if (filter.getLogicOperator() == LogicOperator.AND) {
                andPredicates.add(predicate);
            } else if (filter.getLogicOperator() == LogicOperator.OR) {
                orPredicates.add(predicate);
            }
        });
        Predicate[] andPredicatesArray = andPredicates.toArray(new Predicate[andPredicates.size()]);
        Predicate[] orPredicatesArray = orPredicates.toArray(new Predicate[orPredicates.size()]);
        return orPredicatesArray.length == 0 ? cb.and(andPredicatesArray) :
                andPredicatesArray.length == 0 ? cb.or(orPredicatesArray) :
                        cb.or(cb.and(andPredicatesArray), cb.or(orPredicatesArray));
    }

    @SuppressWarnings("unchecked")
    private static Predicate buildPredicate(Path path, Operator operator, Object value, CriteriaBuilder cb) {
        switch (operator) {
            case IS_NULL:
                return cb.isNull(path);
            case EQUALS:
                return cb.equal(path, value);
            case STARTS_WITH:
                return cb.like(path, value + "%");
            case CONTAINS:
                return cb.like(path, "%" + value + "%");
            case ARE_MEMBERS:
                List<Predicate> predicates = new ArrayList<>();
                Arrays.stream((Object[]) value).forEach(val -> predicates.add(cb.isMember(val, path)));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            case IN:
                return path.in((Object[]) value);
            case GREATER:
                throwExceptionIfNotComparable(value);
                return cb.greaterThan(path, (Comparable) value);
            case LESS:
                throwExceptionIfNotComparable(value);
                return cb.lessThan(path, (Comparable) value);
            case GREATER_OR_EQUAL:
                throwExceptionIfNotComparable(value);
                return cb.greaterThanOrEqualTo(path, (Comparable) value);
            case LESS_OR_EQUAL:
                throwExceptionIfNotComparable(value);
                return cb.lessThanOrEqualTo(path, (Comparable) value);
            default:
                throw new IllegalArgumentException(operator + " not supported");
        }
    }

    private static void throwExceptionIfNotComparable(Object value) {
        if (!(value instanceof Comparable)) {
            throw new IllegalArgumentException("Value must be comparable");
        }
    }
}
