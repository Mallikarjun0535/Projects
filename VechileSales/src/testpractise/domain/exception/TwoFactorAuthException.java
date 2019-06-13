package com.dizzion.portal.domain.exception;

public class TwoFactorAuthException extends BusinessException {
    public TwoFactorAuthException() {
        super("twoFactorAuthFailed");
    }
}
