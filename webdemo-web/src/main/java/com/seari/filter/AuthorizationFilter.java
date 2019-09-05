package com.seari.filter;

import cn.hutool.core.util.StrUtil;
import com.seari.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口请求身份认证
 *
 * @author seari
 * @date 2019/9/4
 */
@Order(1) //多个filter的时候，数值越小，优先级越高
@WebFilter(filterName = "AuthorizationFilter", urlPatterns = "/*") //url过滤配置，并非包配置
public class AuthorizationFilter extends OncePerRequestFilter {

    /**
     * javax.servlet.Filter原生接口
     * spring的OncePerRequestFilter实现了Filter接口，保证一次请求只通过一次filter
     * request.getParameter("key")从body中获取数值
     * request.getHeader("key")从header中获取值
     */

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //如果是登录接口，放行
        if (request.getRequestURL().indexOf("/login") >= 0) {
            filterChain.doFilter(request, response);
        } else {
            Map<String, Object> result = new HashMap<String, Object>();
            //token放在请求头部，key="Authorization"
            String token = request.getHeader("Authorization");
            if (StrUtil.isBlank(token)) {
                result.put("status", 0);
                result.put("message", "token不存在，请登录");
            } else {
                token = token.replace("Bearer ", "");
                String userId = request.getHeader("userId");
                if (jwtUtils.validateToken(token, userId)) {
                    filterChain.doFilter(request, response);
                } else {
                    result.put("status", 0);
                    result.put("message", "token已失效，请重新登录");
                }
            }
        }


    }
}


