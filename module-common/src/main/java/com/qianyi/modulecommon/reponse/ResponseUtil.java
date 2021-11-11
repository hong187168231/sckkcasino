package com.qianyi.modulecommon.reponse;

import com.qianyi.modulecommon.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtil {

    private static MessageUtil messageUtil;

    @Autowired
    public void setMessageUtil(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }


    public static ResponseEntity error() {
        return new ResponseEntity(ResponseCode.ERROR);
    }

    public static ResponseEntity success() {
        return new ResponseEntity(ResponseCode.SUCCESS);
    }

    public static ResponseEntity success(Object data) {
        return new ResponseEntity(ResponseCode.SUCCESS, data);
    }

    public static ResponseEntity success(String msg, Object data) {
        ResponseCode success = ResponseCode.SUCCESS;
        success.setMsg(messageUtil.get(msg) + " " + data);
        return new ResponseEntity(success);
    }

    public static ResponseEntity authenticationNopass() {
        ResponseCode authenticationNopass = ResponseCode.AUTHENTICATION_NOPASS;
        authenticationNopass.setMsg(messageUtil.get(authenticationNopass.getMsg()));
        return new ResponseEntity(authenticationNopass);
    }

    public static ResponseEntity authorizationNopass() {
        ResponseCode authorizationNopass = ResponseCode.AUTHORIZATION_NOPASS;
        authorizationNopass.setMsg(messageUtil.get(authorizationNopass.getMsg()));
        return new ResponseEntity(authorizationNopass);
    }

    public static ResponseEntity requestLimit() {
        ResponseCode requestLimit = ResponseCode.REQUEST_LIMIT;
        requestLimit.setMsg(requestLimit.getMsg());
        return new ResponseEntity(requestLimit);
    }

    public static ResponseEntity parameterNotNull() {
        ResponseCode parameterNotnulll = ResponseCode.PARAMETER_NOTNULLL;
        parameterNotnulll.setMsg(parameterNotnulll.getMsg());
        return new ResponseEntity(ResponseCode.PARAMETER_NOTNULLL);
    }

    public static ResponseEntity custom(String msg) {
        return new ResponseEntity(messageUtil.get(msg));
    }

    public static ResponseEntity custom(String msg,Object data) {
        String translateMsg = messageUtil.get(msg) + " " + data;
        return new ResponseEntity(translateMsg);
    }

    public static ResponseEntity risk() {
        ResponseCode risk = ResponseCode.RISK;
        risk.setMsg(messageUtil.get(risk.getMsg()));
        return new ResponseEntity(risk);
    }

    public static ResponseEntity googleAuthNoPass() {
        ResponseCode googleauthNopass = ResponseCode.GOOGLEAUTH_NOPASS;
        googleauthNopass.setMsg(messageUtil.get(googleauthNopass.getMsg()));
        return new ResponseEntity(googleauthNopass);
    }

    public static ResponseEntity fail() {
        return new ResponseEntity(ResponseCode.FAIL);
    }

    public static ResponseEntity emptytWithdrawMoney() {
        ResponseCode emptyTwithdrawmoney = ResponseCode.EMPTY_TWITHDRAWMONEY;
        emptyTwithdrawmoney.setMsg(messageUtil.get(emptyTwithdrawmoney.getMsg()));
        return new ResponseEntity(ResponseCode.EMPTY_TWITHDRAWMONEY);

    }
    public static ResponseEntity multiDevice() {
        ResponseCode multidevice = ResponseCode.MULTIDEVICE;
        multidevice.setMsg(messageUtil.get(multidevice.getMsg()));
        return new ResponseEntity(ResponseCode.MULTIDEVICE);
    }
}
