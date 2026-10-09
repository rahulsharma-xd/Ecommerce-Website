package com.ecommerce.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {"/login", "/register", "/admin/**", "/cart", "/products/**", "/checkout", "/orders", "/profile"})
    public String forward() {
        return "forward:/index.html";
    }
}
