package com.montfort.erp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    // Forward routes to the single page application
    // If the user visits /admin/list, it forwards internally to dashboard.html
    @GetMapping({"/admin", "/admin/**"})
    public String forwardAdminRoutes() {
        return "forward:/views/admin/dashboard.html";
    }

    // Removed root mapping to avoid static resource conflicts.
    // Use /admin/{module} for UI routes.
}
