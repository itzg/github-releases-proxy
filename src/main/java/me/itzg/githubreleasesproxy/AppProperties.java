package me.itzg.githubreleasesproxy;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("app")
public record AppProperties(
    Map<String, String> appPaths
) {
}
