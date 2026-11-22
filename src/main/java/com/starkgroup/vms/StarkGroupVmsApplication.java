package com.starkgroup.vms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@SpringBootApplication
public class StarkGroupVmsApplication {

    public static void main(String[] args) {
        loadDotenv();
        checkSystemEnv();
        SpringApplication.run(StarkGroupVmsApplication.class, args);
    }

    /**
     * Checks if unified DB_STRING or DATABASE_URL is provided directly
     * via system environment variables (e.g. cloud hosting like Render).
     */
    private static void checkSystemEnv() {
        String dbStr = System.getenv("DB_STRING");
        if (dbStr == null || dbStr.isBlank()) {
            dbStr = System.getenv("DATABASE_URL");
        }
        if (dbStr != null && !dbStr.isBlank()) {
            applyDbString(dbStr);
        }
    }

    /**
     * Loads secrets and configuration dynamically from local .env file.
     * The .env file is strictly ignored by git and never committed.
     */
    private static void loadDotenv() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex).trim();
                    String value = line.substring(eqIndex + 1).trim();

                    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1).trim();
                    }

                    if ("DB_STRING".equalsIgnoreCase(key) || "DATABASE_URL".equalsIgnoreCase(key)) {
                        applyDbString(value);
                    } else {
                        setIfAbsent(key, value);
                        mapToSpringProperty(key, value);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void mapToSpringProperty(String key, String value) {
        switch (key) {
            case "SERVER_PORT":
                setIfAbsent("server.port", value);
                break;
            case "SPRING_DATASOURCE_URL":
                setIfAbsent("spring.datasource.url", value);
                break;
            case "SPRING_DATASOURCE_USERNAME":
                setIfAbsent("spring.datasource.username", value);
                break;
            case "SPRING_DATASOURCE_PASSWORD":
                setIfAbsent("spring.datasource.password", value);
                break;
            case "VMS_ADMIN_USERNAME":
                setIfAbsent("vms.auth.admin.username", value);
                break;
            case "VMS_ADMIN_PASSWORD":
                setIfAbsent("vms.auth.admin.password", value);
                break;
            case "VMS_ADMIN_FULL_NAME":
                setIfAbsent("vms.auth.admin.fullName", value);
                break;
            case "VMS_ADMIN_ROLE":
                setIfAbsent("vms.auth.admin.role", value);
                break;
            case "VMS_CORS_ORIGINS":
                setIfAbsent("vms.cors.allowed-origins", value);
                break;
            case "VMS_JWT_SECRET":
                setIfAbsent("vms.jwt.secret", value);
                break;
            case "VMS_JWT_EXPIRATION_MS":
                setIfAbsent("vms.jwt.expiration-ms", value);
                break;
            case "VMS_SEED_ENABLED":
                setIfAbsent("vms.seed.enabled", value);
                break;
            default:
                break;
        }
    }

    private static void setIfAbsent(String propName, String value) {
        if (System.getProperty(propName) == null && System.getenv(propName) == null) {
            System.setProperty(propName, value);
        }
    }

    /**
     * Parses a single unified database connection string:
     * Format: mysql://username:password@host:port/database
     * And maps it to spring.datasource.url, username, and password.
     */
    static void applyDbString(String dbString) {
        try {
            if (dbString == null || dbString.isBlank()) {
                return;
            }
            String clean = dbString.trim();

            if (clean.startsWith("jdbc:mysql://")) {
                System.setProperty("spring.datasource.url", clean);
                System.setProperty("SPRING_DATASOURCE_URL", clean);
                return;
            }

            if (clean.startsWith("mysql://")) {
                clean = clean.substring("mysql://".length());
            }

            int atIndex = clean.lastIndexOf('@');
            if (atIndex > 0) {
                String userPass = clean.substring(0, atIndex);
                String hostPortDb = clean.substring(atIndex + 1);

                String username = userPass;
                String password = "";
                int colonIndex = userPass.indexOf(':');
                if (colonIndex > 0) {
                    username = userPass.substring(0, colonIndex);
                    password = userPass.substring(colonIndex + 1);
                }

                try {
                    username = java.net.URLDecoder.decode(username, java.nio.charset.StandardCharsets.UTF_8);
                    password = java.net.URLDecoder.decode(password, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                }

                String jdbcUrl;
                if (hostPortDb.contains("?")) {
                    jdbcUrl = "jdbc:mysql://" + hostPortDb;
                    if (!hostPortDb.contains("sslMode=")) {
                        jdbcUrl += "&sslMode=PREFERRED";
                    }
                    if (!hostPortDb.contains("serverTimezone=")) {
                        jdbcUrl += "&serverTimezone=UTC";
                    }
                    if (!hostPortDb.contains("allowPublicKeyRetrieval=")) {
                        jdbcUrl += "&allowPublicKeyRetrieval=true";
                    }
                } else {
                    jdbcUrl = "jdbc:mysql://" + hostPortDb + "?sslMode=PREFERRED&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
                }

                System.setProperty("spring.datasource.url", jdbcUrl);
                System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
                System.setProperty("spring.datasource.username", username);
                System.setProperty("SPRING_DATASOURCE_USERNAME", username);
                System.setProperty("spring.datasource.password", password);
                System.setProperty("SPRING_DATASOURCE_PASSWORD", password);
            }
        } catch (Exception ignored) {
        }
    }
}
