package dev.rdcl.www.api.auth;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app.auth")
public interface AuthProperties {

    int sessionTokenLength();

    int verificationCodeLength();

    @WithDefault("120")
        // 2 minutes
    long authenticatorTimeoutSeconds();

    @WithDefault("360")
        // 6 minutes
    int maxLoginAttemptDurationSeconds();

    String defaultLoginCallbackUrl();

    String verificationEmailFrom();
}
