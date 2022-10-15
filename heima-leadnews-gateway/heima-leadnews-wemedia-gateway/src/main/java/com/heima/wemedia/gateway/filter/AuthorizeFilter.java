package com.heima.wemedia.gateway.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.Payload;
import com.heima.utils.common.RsaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.PublicKey;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    @Value("${leadnews.jwt.publicKeyPath}")
    private String publicKeyPath;//公钥路径

    /**
     * 过滤编辑器
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //放行登录请求
        String uri = request.getURI().getPath();
        if (uri.contains("/login")) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("token");
        if (StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //验证合法token
        try {
            PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);
            Payload<WmUser> payload = JwtUtils.getInfoFromToken(token, publicKey, WmUser.class);
            WmUser user = payload.getInfo();

            request.mutate().header("userId", user.getId().toString());
            return chain.filter(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
