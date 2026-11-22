package com.starkgroup.vms;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbStringParserTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("spring.datasource.url");
        System.clearProperty("SPRING_DATASOURCE_URL");
        System.clearProperty("spring.datasource.username");
        System.clearProperty("SPRING_DATASOURCE_USERNAME");
        System.clearProperty("spring.datasource.password");
        System.clearProperty("SPRING_DATASOURCE_PASSWORD");
    }

    @Test
    @DisplayName("Parses standard local MySQL connection string")
    void parse_LocalMysqlString() {
        StarkGroupVmsApplication.applyDbString("mysql://root:secretPass@localhost:3306/vms_db");

        assertEquals("root", System.getProperty("spring.datasource.username"));
        assertEquals("secretPass", System.getProperty("spring.datasource.password"));
        assertTrue(System.getProperty("spring.datasource.url").startsWith("jdbc:mysql://localhost:3306/vms_db"));
        assertTrue(System.getProperty("spring.datasource.url").contains("serverTimezone=UTC"));
        assertTrue(System.getProperty("spring.datasource.url").contains("sslMode=PREFERRED"));
    }

    @Test
    @DisplayName("Parses cloud MySQL connection string with custom port and special characters")
    void parse_CloudMysqlString() {
        StarkGroupVmsApplication.applyDbString("mysql://user.root:myPassword123@gateway01.aws.tidbcloud.com:4000/test");

        assertEquals("user.root", System.getProperty("spring.datasource.username"));
        assertEquals("myPassword123", System.getProperty("spring.datasource.password"));
        assertTrue(System.getProperty("spring.datasource.url").startsWith("jdbc:mysql://gateway01.aws.tidbcloud.com:4000/test"));
    }

    @Test
    @DisplayName("Parses connection string with URL-encoded characters in credentials")
    void parse_UrlEncodedCredentials() {
        StarkGroupVmsApplication.applyDbString("mysql://user%40corp:pass%23123@dbhost.internal:3306/production");

        assertEquals("user@corp", System.getProperty("spring.datasource.username"));
        assertEquals("pass#123", System.getProperty("spring.datasource.password"));
    }

    @Test
    @DisplayName("Parses connection string with existing query parameters")
    void parse_ExistingQueryParams() {
        StarkGroupVmsApplication.applyDbString("mysql://admin:pass@dbhost:3306/mydb?useUnicode=true&characterEncoding=UTF-8");

        assertEquals("admin", System.getProperty("spring.datasource.username"));
        assertEquals("pass", System.getProperty("spring.datasource.password"));
        assertTrue(System.getProperty("spring.datasource.url").contains("useUnicode=true"));
        assertTrue(System.getProperty("spring.datasource.url").contains("sslMode=PREFERRED"));
    }
}
