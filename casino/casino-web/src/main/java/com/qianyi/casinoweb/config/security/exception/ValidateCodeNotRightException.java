package com.qianyi.casinoweb.config.security.exception;

import org.springframework.security.core.AuthenticationException;

public class ValidateCodeNotRightException extends AuthenticationException {

    public ValidateCodeNotRightException(String msg) {
        super(msg);
    }
}
