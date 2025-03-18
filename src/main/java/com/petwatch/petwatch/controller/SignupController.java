package com.petwatch.petwatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignupController {
    
    @GetMapping("/signup-choice")
    public String signupChoice() {
        return "signup-choice";
    }
    
    @GetMapping("/signup-owner")
    public String signupOwner() {
        return "signup-owner";
    }
    
    @GetMapping("/signup-sitter")
    public String signupSitter() {
        return "signup-sitter";
    }
} 