package com.qianyi.modulecommon.reponse;

import java.io.Serializable;

public class ResponseEntity implements Serializable {

	private static final long serialVersionUID = 6073226655373040149L;

	private int code;
    private String msg;
    private Object data;
    
    public ResponseEntity(String msg) {
        this.code=ResponseCode.CUSTOM.getCode();
        this.msg = msg;
    }

    public ResponseEntity(ResponseCode responseCode) {
        this.code=responseCode.getCode();
        this.msg = responseCode.getMsg();
    }

    public ResponseEntity(ResponseCode responseCode, Object data) {
        this.code=responseCode.getCode();
        this.msg=responseCode.getMsg();
        this.data=data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
