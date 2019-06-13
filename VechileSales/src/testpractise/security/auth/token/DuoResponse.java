package com.dizzion.portal.security.auth.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class DuoResponse {
    Response response;

    @Value
    public static class Response {
        Result result;
    }

    public enum Result {
        DENY, ALLOW;

        @JsonCreator
        public static Result from(String result) {
            return result == null
                    ? null
                    : Result.valueOf(result.toUpperCase());
        }
    }
}
