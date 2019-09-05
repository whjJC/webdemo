package com.seari.controller;

import com.seari.request.LoginRequest;
import com.seari.response.LoginResponse;
import com.seari.response.MessagePack;
import com.seari.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * 用户登录
 *
 * @author seari
 * @date 2019/9/5
 */
@RestController
public class LoginController {

    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    @ResponseBody
    public MessagePack login(@RequestBody LoginRequest request) { //request不需要判空，对象的值需要判空
        MessagePack result = new MessagePack();
        try {

            Map<String, Object> claims = new HashMap<String, Object>(1);
            claims.put("userId", request.getUserName());
            LoginResponse response = new LoginResponse();
            response.setToken(jwtUtils.createJWT(claims));
            response.setUserId(request.getUserName());
            result.setData(response);
            result.setMsg("登录成功.");

        } catch (Exception e) {
            result.setCode(-1);
            result.setMsg("登录异常.");
            logger.debug("登录异常...");
        }

        return result;
    }

}
