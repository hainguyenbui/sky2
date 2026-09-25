package com.example.spyfall.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private Linux linux = new Linux();
    private String win;
    private String phone;

    @Getter
    @Setter
    public static class Linux {
        private String url;
        private String folder;
    }
}
