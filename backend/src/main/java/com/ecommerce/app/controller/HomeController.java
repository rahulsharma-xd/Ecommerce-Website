package com.ecommerce.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @RequestMapping(value = {"/", "/{x:[\\w\\-]+}", "/{x:[\\w\\-]+}/{y:[\\w\\-]+}", "/{x:[\\w\\-]+}/{y:[\\w\\-]+}/{z:[\\w\\-]+}"})
    public String forward() {
        return "forward:/index.html";
    }
}
