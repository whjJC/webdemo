package com.seari.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author seari
 * @date 2019/8/26
 */
@RequestMapping("/project")
@RestController
public class IndexController {

    @RequestMapping("/index")
    public void showIndex(){

    }
}
