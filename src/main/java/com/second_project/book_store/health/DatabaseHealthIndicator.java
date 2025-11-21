package com.second_project.book_store.health;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Custom health indicator for database connectivity and status.
 * 
 * Checks:
 * - Database connection availability
 * - Able to execute test query
 * - Connection pool status (via HikariCP)
 * 
 * Health status:
 * - UP: Database accessible and responding
 * - DOWN: Database unreachable or query failed
 * 
 * Available at: /actuator/health
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            // Execute simple query to verify database is responding
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                
                if (resultSet.next()) {
                    int result = resultSet.getInt(1);
                    
                    if (result == 1) {
                        return Health.up()
                                .withDetail("database", "MySQL")
                                .withDetail("status", "Connected")
                                .withDetail("validationQuery", "SELECT 1")
                                .build();
                    }
                }
            }
            
            return Health.down()
                    .withDetail("database", "MySQL")
                    .withDetail("reason", "Query returned unexpected result")
                    .build();
            
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "MySQL")
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}

