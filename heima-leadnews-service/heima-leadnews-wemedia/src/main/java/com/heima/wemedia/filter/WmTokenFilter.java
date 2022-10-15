package com.heima.wemedia.filter;

import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.ThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter(filterName = "WmTokenFilter", urlPatterns = "/*")
public class WmTokenFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //获取请求头
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String userId = request.getHeader("userId");

        if (StringUtils.isNoneBlank(userId)) {
            //写入ThreadLocal
            WmUser wmUser = new WmUser();
            wmUser.setId(Integer.valueOf(userId));
            ThreadLocalUtils.set(wmUser);
        }

        //放行
        //注意：如果doFilter后续方法执行失败（Controller），则执行SpringMVC的异常处理机制（或自定义异常拦截处理），
        //就不会 filterChain.doFilter(request,response)后续代码了
        try {
            //preHandle
            filterChain.doFilter(request, response);
            //postHandle
        } finally {
            //afterCompletion
            //这句话，不管是 filterChain.doFilter方法执行成功与否，都会执行。删除数据，释放内存
            ThreadLocalUtils.remove();
        }
    }
}
