package com.keycloaktesting.sampleeventlistenerprovider.provider;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import java.util.Arrays;
import java.lang.Exception;

import okhttp3.*;

import java.io.IOException;


public class HTTPEventListenerProvider implements EventListenerProvider {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String serverUri;
    private final String username;
    private final String password;
    public String TOPIC;

    public HTTPEventListenerProvider(String serverUri, String username, String password, String topic) {
        this.serverUri = serverUri;
        this.username = username;
        this.password = password;
        this.TOPIC = topic;
    }

    @Override
    public void onEvent(Event event) {
        //if (event.getRealmId().equals("realm uuid") && event.getType() == EventType.REGISTER) {
        if (event.getType() == EventType.REGISTER) {
            System.out.println("EVENT REGISTER DETECTED!");
            StringBuilder sb = new StringBuilder();
            sb.append("{'eventType': '");
            sb.append(event.getType());
            sb.append("'realmId': '");
            sb.append(event.getRealmId());
            sb.append("'");
            if (event.getDetails() != null) {
                sb.append(", 'id': '");
                sb.append(event.getUserId());
                sb.append("', 'type': '");
                sb.append(event.getDetails().get("type"));
                sb.append("', 'email': '");
                sb.append(event.getDetails().get("email"));
                sb.append("', 'firstName': '");
                sb.append(event.getDetails().get("given_name"));
                sb.append("', 'lastName': '");
                sb.append(event.getDetails().get("family_name"));
                sb.append("'");
            } else {
                sb.append(", 'id': '', 'type': '', 'email': '', 'first_name': '', 'last_name': ''");
            }
            sb.append("}");
            String stringEvent = sb.toString();

            try {
                RequestBody formBody = new FormBody.Builder()
                        .add("json", stringEvent)
                        .build();

                okhttp3.Request.Builder builder = new Request.Builder()
                        .url(this.serverUri);
                        //.addHeader("User-Agent", "KeycloakHttp Bot");


//                if (this.username != null && this.password != null) {
//                    builder.addHeader("Authorization", "Basic " + this.username + ":" + Arrays.toString(this.password.toCharArray()));
//                }

                Request request = builder.post(formBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    if (response.body() != null) {
                        System.out.println(response.body().string());
                    }
                }
            } catch (Exception e) {
                System.out.println("UH OH!! " + e);
            }
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean b) {
    }

    @Override
    public void close() {
    }
}
