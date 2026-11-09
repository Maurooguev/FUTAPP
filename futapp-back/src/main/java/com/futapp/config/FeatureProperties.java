package com.futapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "futapp.features")
public record FeatureProperties(
        boolean playerAccounts,
        boolean teamDirectorAccounts,
        boolean claims,
        boolean notifications,
        boolean favorites
) {
}
