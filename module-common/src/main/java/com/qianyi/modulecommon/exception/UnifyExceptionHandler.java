package com.qianyi.modulecommon.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;

/**
 * @author jordan
 * 	自定义异常处理器 用以业务处理
 */
@RestControllerAdvice
@Component
public class UnifyExceptionHandler {
	private Logger logger = LoggerFactory.getLogger(UnifyExceptionHandler.class);

	/**
	 * 默认异常
	 * @param e
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity handlerCommonException(Exception e) {
		ResponseEntity responseDto = new ResponseEntity();
		responseDto.setCode(ResponseCode.ERROR.getCode());
		responseDto.setMsg(ResponseCode.ERROR.getMsg());
		logger.error("UnifyExceptionHandler.handlerCommonException exception:{}", e);
		return responseDto;
	}

	// 报自定义异常 StaffPointException时，对其进行拦截并处理的方法
	@ExceptionHandler(StaffPointsException.class)
	public ResponseEntity handlerCustomizeException(StaffPointsException e) {
		ResponseEntity responseDto = new ResponseEntity();
		responseDto.setCode(e.getCode());
		responseDto.setMsg(e.getMessage());
		logger.error("UnifyExceptionHandler.handlerCustomizeException StaffPointsException:{}", e);
		return responseDto;
	}
	
	/**
	 * 400 BAD_REQUEST
	 * @param e
	 * @return
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity handlerMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		logger.error("缺少请求参数", e);
		return ResponseUtil.error(400, e.getMessage());
	}
	
}