package com.montfort.erp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class DumpTable implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            List<Map<String, Object>> columns = jdbcTemplate.queryForList("DESCRIBE erp_students");
            for(Map<String, Object> col : columns) {
                System.out.println("COL: " + col.get("Field"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
