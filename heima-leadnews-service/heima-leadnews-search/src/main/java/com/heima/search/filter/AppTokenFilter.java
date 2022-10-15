package com.heima.search.filter;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.ThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 获取登录用户信息过滤器
 */
@Component
@WebFilter(filterName = "AppTokenFilter",urlPatterns = "/*")
public class AppTokenFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //获取userId
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String userId = request.getHeader("userId");

        if (StringUtils.isNoneBlank(userId) && !userId.equals("0")){
            //游客身份不存储ThreadLocal数据
            //把userId存入ThreadLocal对象
            ApUser apUser = new ApUser();
            apUser.setId(Integer.parseInt(userId));
            ThreadLocalUtils.set(apUser);
        }

        //放行
        try {
            filterChain.doFilter(request,response);
        } finally {
            ThreadLocalUtils.remove();
        }
    }
}
