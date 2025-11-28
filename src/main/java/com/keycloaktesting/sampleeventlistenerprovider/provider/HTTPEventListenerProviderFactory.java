package com.keycloaktesting.sampleeventlistenerprovider.provider;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class HTTPEventListenerProviderFactory implements EventListenerProviderFactory {
    private String serverUri;
    private String username;
    private String password;
    private String topic;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new HTTPEventListenerProvider(serverUri, username, password, topic);
    }

    @Override
    public void init(Config.Scope config) {
        serverUri = config.get("serverUri", "http://localhost:8888/api/v1/keycloak/registration");
        username = config.get("username", null);
        password = config.get("password", null);
        topic = config.get("topic", "keycloak/events");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }
    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "http";
    }
}

