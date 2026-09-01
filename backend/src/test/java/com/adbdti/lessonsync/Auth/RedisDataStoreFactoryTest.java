package com.adbdti.lessonsync.Auth;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Exercises the credential store against a real Redis, since persisting the refresh token is what
 * removes the need to keep an access token in the source.
 *
 * <p>Skipped when no Redis is listening on localhost.
 */
class RedisDataStoreFactoryTest {

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;

    private String keyPrefix;
    private RedisDataStoreFactory factory;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory();
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();

        boolean reachable;
        try {
            reachable = "PONG".equalsIgnoreCase(
                    redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<String>)
                            conn -> conn.ping()));
        } catch (RuntimeException e) {
            reachable = false;
        }
        assumeTrue(reachable, "Redis is not running on localhost:6379");
    }

    @BeforeEach
    void setUp() {
        keyPrefix = "test:oauth:" + UUID.randomUUID();
        factory = new RedisDataStoreFactory(redisTemplate, keyPrefix);
    }

    @AfterEach
    void tearDown() {
        if (redisTemplate != null && keyPrefix != null) {
            redisTemplate.delete(redisTemplate.keys(keyPrefix + "*"));
        }
    }

    @Test
    void storesAndReadsBackACredential() throws IOException {
        DataStore<StoredCredential> store = factory.getDataStore("credentials");
        StoredCredential stored = new StoredCredential()
                .setAccessToken("access-token")
                .setRefreshToken("refresh-token")
                .setExpirationTimeMilliseconds(1_700_000_000_000L);

        store.set("user-1", stored);

        StoredCredential loaded = store.get("user-1");
        assertThat(loaded).isNotNull();
        assertThat(loaded.getAccessToken()).isEqualTo("access-token");
        assertThat(loaded.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(loaded.getExpirationTimeMilliseconds()).isEqualTo(1_700_000_000_000L);
    }

    @Test
    void returnsNullForAnUnknownUser() throws IOException {
        DataStore<StoredCredential> store = factory.getDataStore("credentials");

        assertThat(store.get("nobody")).isNull();
    }

    @Test
    void keepsUsersApart() throws IOException {
        DataStore<StoredCredential> store = factory.getDataStore("credentials");
        store.set("user-1", new StoredCredential().setRefreshToken("refresh-1"));
        store.set("user-2", new StoredCredential().setRefreshToken("refresh-2"));

        assertThat(store.get("user-1").getRefreshToken()).isEqualTo("refresh-1");
        assertThat(store.get("user-2").getRefreshToken()).isEqualTo("refresh-2");
        assertThat(store.keySet()).containsExactlyInAnyOrder("user-1", "user-2");
        assertThat(store.values()).hasSize(2);
    }

    @Test
    void deletesASingleUserWithoutTouchingTheRest() throws IOException {
        DataStore<StoredCredential> store = factory.getDataStore("credentials");
        store.set("user-1", new StoredCredential().setRefreshToken("refresh-1"));
        store.set("user-2", new StoredCredential().setRefreshToken("refresh-2"));

        store.delete("user-1");

        assertThat(store.get("user-1")).isNull();
        assertThat(store.get("user-2")).isNotNull();
    }

    @Test
    void clearsEverything() throws IOException {
        DataStore<StoredCredential> store = factory.getDataStore("credentials");
        store.set("user-1", new StoredCredential().setRefreshToken("refresh-1"));

        store.clear();

        assertThat(store.keySet()).isEmpty();
        assertThat(store.get("user-1")).isNull();
    }

    /**
     * A new factory stands in for a restarted application: the refresh token has to still be there.
     */
    @Test
    void credentialsOutliveTheFactoryThatWroteThem() throws IOException {
        factory.getDataStore("credentials")
                .set("user-1", new StoredCredential().setRefreshToken("refresh-token"));

        RedisDataStoreFactory restarted = new RedisDataStoreFactory(redisTemplate, keyPrefix);
        StoredCredential loaded = restarted.<StoredCredential>getDataStore("credentials").get("user-1");

        assertThat(loaded).isNotNull();
        assertThat(loaded.getRefreshToken()).isEqualTo("refresh-token");
    }

    /**
     * The end goal: a token response persisted through the flow can be loaded back as a usable
     * {@link Credential}, with the refresh token the client library needs to renew access on its own.
     */
    @Test
    void authorizationFlowRoundTripsACredentialThroughRedis() throws IOException {
        GoogleAuthorizationCodeFlow flow = newFlow();

        TokenResponse tokenResponse = new TokenResponse()
                .setAccessToken("access-token")
                .setRefreshToken("refresh-token")
                .setExpiresInSeconds(3600L);
        flow.createAndStoreCredential(tokenResponse, "google-subject-1");

        Credential credential = newFlow().loadCredential("google-subject-1");

        assertThat(credential).isNotNull();
        assertThat(credential.getAccessToken()).isEqualTo("access-token");
        assertThat(credential.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(credential.getExpiresInSeconds()).isPositive();
    }

    @Test
    void authorizationFlowReportsNoCredentialForAnUnknownUser() throws IOException {
        assertThat(newFlow().loadCredential("never-signed-in")).isNull();
    }

    private GoogleAuthorizationCodeFlow newFlow() throws IOException {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(
                new GoogleClientSecrets.Details()
                        .setClientId("test-client-id")
                        .setClientSecret("test-client-secret"));

        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                List.of("https://www.googleapis.com/auth/calendar"))
                .setDataStoreFactory(new RedisDataStoreFactory(redisTemplate, keyPrefix))
                .setMethod(BearerToken.authorizationHeaderAccessMethod())
                .setAccessType("offline")
                .build();
    }
}
