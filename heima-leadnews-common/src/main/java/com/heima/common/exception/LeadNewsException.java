package com.heima.common.exception;


import com.heima.common.dtos.AppHttpCodeEnum;
import lombok.Getter;

@Getter
public class LeadNewsException extends RuntimeException{
    private Integer status;

    public LeadNewsException(Integer status,String message){
        super(message);
        this.status = status;
    }

    public LeadNewsException(AppHttpCodeEnum appHttpCodeEnum){
        super(appHttpCodeEnum.getErrorMessage());
        this.status = appHttpCodeEnum.getCode();
    }
}
