package com.montfort.erp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/login", "/admin"})
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/apply")
    public String applyPage() {
        return "forward:/apply.html";
    }

    @GetMapping("/apply/status")
    public String applicationStatus() {
        return "forward:/status.html";
    }

    @GetMapping("/apply/print")
    public String printApplication() {
        return "forward:/print_application.html";
    }

    @GetMapping("/admin/application/view.html")
    public String viewAdminApplication() {
        return "forward:/admin_view_application.html";
    }
}
