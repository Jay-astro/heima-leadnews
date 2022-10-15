package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.BCrypt;
import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.RsaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {
    @Value("${leadnews.jwt.privateKeyPath}")
    private String privateKeyPath;
    @Value("${leadnews.jwt.expire}")
    private Integer expire;

    @Override
    public ResponseResult login(LoginDto dto) {
        if (StringUtils.isNotEmpty(dto.getPhone()) && StringUtils.isNotEmpty(dto.getPassword())){
            QueryWrapper<ApUser> queryWrapper = new QueryWrapper();
            queryWrapper.eq("phone",dto.getPhone());
            ApUser loginUser = getOne(queryWrapper);
            if (loginUser == null){
                throw new LeadNewsException(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
            }
            if (!BCrypt.checkpw(dto.getPassword(),loginUser.getPassword())){
                throw new LeadNewsException(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            try {
                PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);
                loginUser.setPassword("null");
                String token = JwtUtils.generateTokenExpireInSeconds(loginUser, privateKey, expire);
                Map<String,Object> msg = new HashMap<>();
                msg.put("token",token);
                msg.put("user",loginUser);
                return ResponseResult.okResult(msg);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("秘钥文件读取失败！");
            }
        }else {
            //游客
            try {
                ApUser visitUser = new ApUser();
                PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);
                visitUser.setId(0);
                String token = JwtUtils.generateTokenExpireInSeconds(visitUser, privateKey, expire);
                Map<String,Object> msg = new HashMap<>();
                msg.put("token",token);
                return ResponseResult.okResult(msg);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("秘钥文件读取失败！");
            }
        }
    }
}
