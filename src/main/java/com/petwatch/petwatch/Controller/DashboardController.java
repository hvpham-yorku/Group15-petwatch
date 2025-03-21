package com.petwatch.petwatch.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    
    @GetMapping("/dashboard-owner")
    public String dashboardOwner() {
        return "dashboard-owner";
    }
    
    @GetMapping("/dashboard-sitter")
    public String dashboardSitter() {
        return "dashboard-sitter";
    }
} 