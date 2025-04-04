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
            
            // Set pragmas for better concurrency handling
            
            // Enable WAL (Write-Ahead Logging) mode
            // This allows readers to continue reading while writers are writing
            stmt.execute("PRAGMA journal_mode = WAL;");
            
            // Set synchronous mode to NORMAL for better performance while maintaining safety
            stmt.execute("PRAGMA synchronous = NORMAL;");
            
            // Set busy timeout to 5 seconds (5000 ms)
            // When database is locked, SQLite will wait this long before failing
            stmt.execute("PRAGMA busy_timeout = 5000;");
            
            // Allow concurrent readers
            stmt.execute("PRAGMA read_uncommitted = 1;");
            
            System.out.println("SQLite configured for concurrent operation");
        } catch (SQLException e) {
            System.err.println("Error configuring SQLite: " + e.getMessage());
        }
    }
} 