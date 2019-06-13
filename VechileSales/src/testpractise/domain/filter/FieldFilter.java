package com.dizzion.portal.domain.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;

@Value
@Builder
@AllArgsConstructor
public class FieldFilter {
    private static final String DELIMITER = ":";

    String key;
    LogicOperator logicOperator;
    Operator operator;
    String value;

    public static FieldFilterBuilder baseOn(FieldFilter filter) {
        return builder()
                .key(filter.getKey())
                .logicOperator(filter.getLogicOperator())
                .operator(filter.getOperator())
                .value(filter.getValue());
    }

    public static FieldFilter fromString(String key, String operatorsAndValueString) {
        String[] operatorsAndValue = operatorsAndValueString.split(DELIMITER, 3);

        LogicOperator logicOperator = LogicOperator.valueOf(operatorsAndValue[0].trim().toUpperCase());
        Operator operator = Operator.fromString(operatorsAndValue[1].trim());

        String value = operatorsAndValue.length > 2 ? operatorsAndValue[2] : "";
        return new FieldFilter(key, logicOperator, operator, value);
    }

    public enum Operator {
        EQUALS("equal"),
        STARTS_WITH("startsWith"),
        CONTAINS("contains"),
        IS_NULL("isNull"),
        ARE_MEMBERS("areMembers"),
        IN("in"),
        GREATER("gt"),
        LESS("lt"),
        GREATER_OR_EQUAL("ge"),
        LESS_OR_EQUAL("le");

        private static final Map<String, Operator> operatorByString =
                uniqueIndex(asList(Operator.values()), Enum::toString);

        private String stringVal;

        Operator(String stringVal) {
            this.stringVal = stringVal;
        }

        public static Operator fromString(String stringVal) {
            return Optional.ofNullable(operatorByString.get(stringVal))
                    .orElseThrow(IllegalArgumentException::new);
        }

        @Override
        public String toString() {
            return stringVal;
        }
    }

    public enum LogicOperator {
        AND,
        OR
    }
}
