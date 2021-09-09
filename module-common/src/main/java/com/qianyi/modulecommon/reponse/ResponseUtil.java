package com.qianyi.modulecommon.reponse;

public class ResponseUtil {

    public static ResponseEntity error() {
        return new ResponseEntity(ResponseCode.ERROR);
    }

    public static ResponseEntity success() {
        return new ResponseEntity(ResponseCode.SUCCESS);
    }

    public static ResponseEntity success(Object data) {
        return new ResponseEntity(ResponseCode.SUCCESS, data);
    }

    public static ResponseEntity authenticationNopass() {
        return new ResponseEntity(ResponseCode.AUTHENTICATION_NOPASS);
    }

    public static ResponseEntity requestLimit() {
        return new ResponseEntity(ResponseCode.REQUEST_LIMIT);
    }

    public static ResponseEntity parameterNotNull() {
        return new ResponseEntity(ResponseCode.PARAMETER_NOTNULLL);
    }

    public static ResponseEntity custom(String msg) {
        return new ResponseEntity(msg);
    }

    public static ResponseEntity risk() {
        return new ResponseEntity(ResponseCode.RISK);
    }

    public static ResponseEntity googleAuthNoPass() {
        return new ResponseEntity(ResponseCode.GOOGLEAUTH_NOPASS);
    }

    public static ResponseEntity fail() {
        return new ResponseEntity(ResponseCode.FAIL);
    }
}
