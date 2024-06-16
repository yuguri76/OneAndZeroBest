package com.sparta.oneandzerobest.config;

import com.sparta.oneandzerobest.auth.config.JwtConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@TestConfiguration
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TestJwtConfig extends JwtConfig {

    @Value("${jwt.secret.key}")
    private String secretKey;
    @Value("${jwt.token.expiration}")
    private long tokenExpiration;
    @Value("${jwt.refresh.token.expiration}")
    private long refreshTokenExpiration;

    @Override
    public String getSecretKey() {

        return "e36f112d-aaaa-bbbb-cccc-14dcdc16360b";
    }

    @Override
    public long getTokenExpiration() {
        return 1800000;
    }

    @Override
    public long getRefreshTokenExpiration() {
        return 1209600000;
    }
}
