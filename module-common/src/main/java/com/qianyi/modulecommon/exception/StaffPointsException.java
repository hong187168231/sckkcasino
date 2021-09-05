package com.qianyi.modulecommon.exception;

import com.qianyi.modulecommon.reponse.ResponseCode;

import lombok.Data;

@Data
public class StaffPointsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private int code;
	private String message;

	public StaffPointsException() {
	}

	public StaffPointsException(Exception e) {
		super(e);
	}

	public StaffPointsException(String message) {
		super(message);
		this.message = message;
	}
	
	public StaffPointsException(int code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	public StaffPointsException(ResponseCode codeEnum) {
		super(codeEnum.getMsg());
		this.code = codeEnum.getCode();
		this.message = codeEnum.getMsg();
	}
}