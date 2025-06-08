package com.group2.VinfastAuto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class LoginPageController {

    @GetMapping
    public String loginRedirect() {
        return "forward:/private_resources/dashboard.html"; // Chuyen huong noi bo qua file tinh
    }
}
