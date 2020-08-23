package com.fundingsocieties.jumpcloudbot.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fundingsocieties.jumpcloudbot.handlers.BaseHandlers;
import com.fundingsocieties.jumpcloudbot.services.CredentialService;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SlackApp {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private BaseHandlers baseHandlers;

    @Value("${base-command}")
    private String baseCommand;

    @Bean
    public AppConfig loadAppConfig() throws JsonProcessingException {
        AppConfig config = new AppConfig();
        ClassLoader classLoader = SlackApp.class.getClassLoader();
        config.setSigningSecret(credentialService.getSlackSigningSecret());
        config.setSingleTeamBotToken(credentialService.getSlackToken());
        return config;
    }

    @Bean
    public App initSlackApp(AppConfig config) {
        App app = new App(config);
        app.command(baseCommand, (req, ctx) -> baseHandlers.baseHandler(req, ctx));
        return app;
    }


}
