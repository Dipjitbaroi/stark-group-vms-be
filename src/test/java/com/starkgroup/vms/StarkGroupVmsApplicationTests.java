package com.starkgroup.vms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StarkGroupVmsApplicationTests {

    static {
        TestEnvHelper.loadDotenv();
    }

    @Test
    void contextLoads() {
    }
}
