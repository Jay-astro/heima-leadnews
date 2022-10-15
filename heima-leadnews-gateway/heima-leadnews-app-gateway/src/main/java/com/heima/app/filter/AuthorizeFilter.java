package com.heima.app.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.Payload;
import com.heima.utils.common.RsaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.PublicKey;

@Component
@Order(1)
public class AuthorizeFilter implements GlobalFilter {

    @Value("${leadnews.jwt.publicKeyPath}")
    private  String publicKeyPath;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求和响应
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //判断请求状态是否为登录请求
        String uri = request.getURI().getPath();
        if (uri.contains("/login")){
            return chain.filter(exchange);
        }

        //获取请求头token数据
        String token = request.getHeaders().getFirst("token");
        if (StringUtils.isEmpty(token)){
            //拒绝请求
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        try {
            PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);
            Payload<ApUser> payload = JwtUtils.getInfoFromToken(token, publicKey, ApUser.class);
            //获取登录用户信息
            ApUser user = payload.getInfo();
            //写入请求头
            request.mutate().header("userId",user.getId().toString());
            //放行
            return chain.filter(exchange);

        } catch (Exception e) {
            //拒绝访问
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }
}
