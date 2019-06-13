package com.dizzion.portal.domain.exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class UniqueConstraintException extends RuntimeException {
    String uniquePropertyName;
}
