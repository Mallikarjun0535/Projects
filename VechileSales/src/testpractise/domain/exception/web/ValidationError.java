package com.dizzion.portal.domain.exception.web;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class ValidationError {

    ErrorCode errorCode;
    Set<String> errors;
}
