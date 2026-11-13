package org.galactic.adapter;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@ApplicationScoped
public class GoogleAdapter {

    @ConfigProperty(name = "google.gmail.client-id")
    String clientId;

    @ConfigProperty(name = "google.gmail.client-secret")
    String clientSecret;

    @ConfigProperty(name = "google.gmail.refresh-token")
    String refreshToken;

    @ConfigProperty(name = "job.mail-fetch.maxResult")
    Long maxResult;

    @ConfigProperty(name = "job.mail-fetch.query")
    String query;

    private Gmail gmail;

    @PostConstruct
    void init() {
        try {
            UserCredentials credentials = UserCredentials.newBuilder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRefreshToken(refreshToken)
                    .build();

            gmail = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Failed to create Gmail client", e);
        }
    }

    public List<Message> getMessages() throws IOException {

        ListMessagesResponse response = this.gmail.users()
                .messages()
                .list("me")
                .setQ(this.query)
                .setMaxResults(this.maxResult)
                .execute();

        return response.getMessages() == null
                ? List.of()
                : response.getMessages();
    }

    public Message getMessage(String messageId)
            throws IOException {

        return this.gmail.users()
                .messages()
                .get("me", messageId)
                .setFormat("full")
                .execute();
    }
}
