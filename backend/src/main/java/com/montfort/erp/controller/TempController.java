package com.montfort.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class TempController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/api/public/dump_students")
    public List<Map<String, Object>> dump() {
        return jdbcTemplate.queryForList("DESCRIBE erp_students");
    }
}
