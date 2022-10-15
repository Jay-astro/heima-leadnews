package com.heima.common.exception;

import com.heima.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     *  处理业务异常（自定义异常）
     * @param e
     * @return
     */
    @ExceptionHandler(value = LeadNewsException.class)
    public ResponseResult handleLeadNewsException(LeadNewsException e){
        return ResponseResult.errorResult(e.getStatus(),e.getMessage());
    }

    /**
     * 处理系统异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseResult handleException(Exception e){
        return ResponseResult.errorResult(500,"系统异常："+e.getMessage());
    }
}
