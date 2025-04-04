package com.petwatch.petwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import jakarta.annotation.PostConstruct;

@Configuration
public class SQLiteConfig {
    
    private static final String URL = "jdbc:sqlite:petwatch.db";
    
    @PostConstruct
    public void configureSQLite() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            
            // fix database lock issue 
            stmt.execute("PRAGMA journal_mode = WAL;");
            
            stmt.execute("PRAGMA synchronous = NORMAL;");
            
            stmt.execute("PRAGMA busy_timeout = 5000;");
            
            stmt.execute("PRAGMA read_uncommitted = 1;");
            
            System.out.println("SQLite configured for concurrent operation");
        } catch (SQLException e) {
            System.err.println("Error configuring SQLite: " + e.getMessage());
        }
    }
} 