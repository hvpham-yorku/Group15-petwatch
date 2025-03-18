package com.petwatch.petwatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    
    @GetMapping("/login-choice")
    public String loginChoice() {
        return "login-choice";
    }
    
    @GetMapping("/login-owner")
    public String loginOwner() {
        return "login-owner";
    }
    
    @GetMapping("/login-sitter")
    public String loginSitter() {
        return "login-sitter";
    }
} 