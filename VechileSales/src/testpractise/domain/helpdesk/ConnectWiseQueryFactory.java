package com.dizzion.portal.domain.helpdesk;

import com.dizzion.portal.domain.filter.FieldFilter;
import lombok.experimental.UtilityClass;

import java.util.Collection;

import static com.dizzion.portal.domain.filter.FieldFilter.LogicOperator.AND;
import static com.dizzion.portal.domain.filter.FieldFilter.LogicOperator.OR;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService.CID_FIELD;
import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@UtilityClass
public class ConnectWiseQueryFactory {

    public static String queryFromFilters(Collection<FieldFilter> filters) {
        String joinOperator;
        if (filters.stream().allMatch(filter -> filter.getLogicOperator() == AND)) {
            joinOperator = " AND ";
        } else if (filters.stream().allMatch(filter -> filter.getLogicOperator() == OR)) {
            joinOperator = " OR ";
        } else {
            throw new IllegalArgumentException("Using AND and OR operators in the same query is not supported");
        }
        return filters.stream()
                .map(ConnectWiseQueryFactory::buildQueryPredicate)
                .collect(joining(joinOperator));
    }

    public static String queryFromFilters(FieldFilter... filters) {
        return queryFromFilters(asList(filters));
    }

    private static String buildQueryPredicate(FieldFilter filter) {
        String sanitizedValue = filter.getValue().replaceAll("['\"]", "");
        switch (filter.getOperator()) {
            case EQUALS:
                String filterValue = filter.getKey().equals(CID_FIELD)
                        ? wrapInQuotes(sanitizedValue)
                        : wrapInQuotesIfNotPrimitive(sanitizedValue);
                return filter.getKey() + "=" + filterValue;
            case STARTS_WITH:
                return filter.getKey() + " LIKE \"" + sanitizedValue + "%\"";
            case CONTAINS:
                return filter.getKey() + " LIKE \"" + "%" + sanitizedValue + "%\"";
            case IN:
                String listValue = stream(sanitizedValue.split(","))
                        .filter(value -> !value.isEmpty())
                        .map(String::trim)
                        .map(ConnectWiseQueryFactory::wrapInQuotesIfNotPrimitive)
                        .collect(joining(","));
                return filter.getKey() + " IN (" + listValue + ")";
            case GREATER_OR_EQUAL:
                return filter.getKey() + " >= " + sanitizedValue;
            case LESS_OR_EQUAL:
                return filter.getKey() + " <= " + sanitizedValue;
            default:
                throw new IllegalArgumentException("Operator " + filter.getOperator() + " not supported");
        }
    }

    private static String wrapInQuotesIfNotPrimitive(String value) {
        return (isNumerical(value) || isBoolean(value)) ? value : wrapInQuotes(value);
    }

    private static String wrapInQuotes(String value) {
        return "\"" + value + "\"";
    }

    private static boolean isNumerical(String value) {
        try {
            parseDouble(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }
}
