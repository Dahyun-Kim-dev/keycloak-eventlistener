package com.keycloaktesting.sampleeventlistenerprovider.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import okhttp3.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPEventListenerProvider implements EventListenerProvider {

    private final String serverUri;
    private final String clientId = "konsultfy-event-listener";
    private final String clientSecret = "vBbt05cBxJ8qSZESRHx66SGpafuerH5g";
    private final String tokenUrl =
            "http://keycloak:8080/realms/konsultfy/protocol/openid-connect/token";

    public HTTPEventListenerProvider(String serverUri, String username, String password, String topic) {
        this.serverUri = serverUri;
    }

    @Override
    public void onEvent(Event event) {

        if (event.getType() != EventType.REGISTER) return;

        System.out.println("EVENT REGISTER DETECTED!");

        try {

            String token = fetchAccessToken();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = new HashMap<>();

            payload.put("eventType", event.getType());
            payload.put("id", event.getUserId());
            payload.put("type", event.getDetails().get("type"));
            payload.put("email", event.getDetails().get("email"));
            payload.put("firstName", event.getDetails().get("given_name"));
            payload.put("lastName", event.getDetails().get("family_name"));

            String json = mapper.writeValueAsString(payload);

            sendEvent(json, token);

        } catch (Exception e) {
            System.out.println("UH OH!! " + e.getMessage());
        }
    }

    private String fetchAccessToken() throws Exception {

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(tokenUrl);

            List<NameValuePair> params = Arrays.asList(
                    new BasicNameValuePair("grant_type", "client_credentials"),
                    new BasicNameValuePair("client_id", clientId),
                    new BasicNameValuePair("client_secret", clientSecret)
            );

            post.setEntity(new UrlEncodedFormEntity(params));
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(post)) {

                String body = EntityUtils.toString(response.getEntity());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(body);

                return node.get("access_token").asText();
            }
        }
    }

    private void sendEvent(String json, String token) throws Exception {

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(serverUri);

            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + token);

            post.setEntity(new StringEntity(json));

            try (CloseableHttpResponse response = client.execute(post)) {

                System.out.println("Status: " + response.getStatusLine().getStatusCode());
                System.out.println("Response: " + EntityUtils.toString(response.getEntity()));
            }
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {}

    @Override
    public void close() {}
}