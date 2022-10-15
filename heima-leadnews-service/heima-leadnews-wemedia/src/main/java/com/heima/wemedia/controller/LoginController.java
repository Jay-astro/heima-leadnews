package com.heima.wemedia.controller;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private WmUserService wmUserService;

    @PostMapping("/login/in")
    public ResponseResult loginIn(@RequestBody WmLoginDto wmLoginDto){
        return wmUserService.loginIn(wmLoginDto);
    }
}
