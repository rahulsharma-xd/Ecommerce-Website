package com.buyora.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendForwardController {
    @RequestMapping(value = {
        "/",
        "/login",
        "/register",
        "/dashboard",
        "/cart",
        "/checkout",
        "/orders",
        "/admin"
    })
    public String forwardRoot() {
        return "forward:/index.html";
    }

    @RequestMapping(value = {
        "/dashboard/**",
        "/products/**",
        "/orders/**",
        "/admin/**"
    })
    public String forwardNested() {
        return "forward:/index.html";
    }
}
