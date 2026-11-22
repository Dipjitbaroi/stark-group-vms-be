package com.starkgroup.vms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility helper for automated tests to resolve values directly from the local .env file
 * or system environment variables, with ZERO hardcoded fallbacks or secrets.
 */
public final class TestEnvHelper {

    private static final Map<String, String> ENV_CACHE = new HashMap<>();

    static {
        loadDotenv();
    }

    private TestEnvHelper() {
    }

    public static void loadDotenv() {
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

                    ENV_CACHE.put(key, value);
                    System.setProperty(key, value);
                    mapToProperty(key, value);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void mapToProperty(String key, String value) {
        switch (key) {
            case "VMS_ADMIN_USERNAME" -> System.setProperty("vms.auth.admin.username", value);
            case "VMS_ADMIN_PASSWORD" -> System.setProperty("vms.auth.admin.password", value);
            case "VMS_ADMIN_FULL_NAME" -> System.setProperty("vms.auth.admin.fullName", value);
            case "VMS_ADMIN_ROLE" -> System.setProperty("vms.auth.admin.role", value);
            case "VMS_CORS_ORIGINS" -> System.setProperty("vms.cors.allowed-origins", value);
            case "VMS_JWT_SECRET" -> System.setProperty("vms.jwt.secret", value);
            case "VMS_JWT_EXPIRATION_MS" -> System.setProperty("vms.jwt.expiration-ms", value);
            case "VMS_SEED_ENABLED" -> System.setProperty("vms.seed.enabled", value);
            default -> {}
        }
    }

    /**
     * Strictly retrieves the required environment key from .env or system environment.
     * Throws an exception if missing, completely avoiding any hardcoded fallback.
     */
    public static String get(String key) {
        String sysVal = System.getenv(key);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal.trim();
        }
        String propVal = System.getProperty(key);
        if (propVal != null && !propVal.isBlank()) {
            return propVal.trim();
        }
        String fileVal = ENV_CACHE.get(key);
        if (fileVal != null && !fileVal.isBlank()) {
            return fileVal.trim();
        }
        throw new IllegalStateException("Missing required key '" + key + "' in .env file.");
    }

    public static long getLong(String key) {
        return Long.parseLong(get(key));
    }
}
