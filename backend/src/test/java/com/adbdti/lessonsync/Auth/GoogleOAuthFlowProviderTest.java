package com.adbdti.lessonsync.Auth;

import com.adbdti.lessonsync.Config.GoogleOAuthProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleOAuthFlowProviderTest {

    private static final String WEB_CLIENT_JSON = """
            {
              "web": {
                "client_id": "json-client.apps.googleusercontent.com",
                "client_secret": "json-secret",
                "redirect_uris": ["https://api.example.com/auth/google/callback"]
              }
            }
            """;

    @TempDir
    Path tokenDirectory;

    @Test
    void isConfiguredWhenClientIdAndSecretAreSet() {
        GoogleOAuthProperties properties = properties();
        properties.setClientId("inline-client.apps.googleusercontent.com");
        properties.setClientSecret("inline-secret");

        assertThat(new GoogleOAuthFlowProvider(properties).isConfigured()).isTrue();
    }

    @Test
    void isConfiguredWhenCredentialsJsonIsSet() {
        GoogleOAuthProperties properties = properties();
        properties.setCredentialsJson(WEB_CLIENT_JSON);

        assertThat(new GoogleOAuthFlowProvider(properties).isConfigured()).isTrue();
    }

    @Test
    void isNotConfiguredWithoutAnyClientSecrets() {
        assertThat(new GoogleOAuthFlowProvider(properties()).isConfigured()).isFalse();
    }

    @Test
    void blankEnvironmentPlaceholdersDoNotCountAsConfigured() {
        GoogleOAuthProperties properties = properties();
        properties.setClientId("");
        properties.setClientSecret("");
        properties.setCredentialsJson("  ");

        assertThat(new GoogleOAuthFlowProvider(properties).isConfigured()).isFalse();
    }

    @Test
    void buildsAFlowFromCredentialsJson() {
        GoogleOAuthProperties properties = properties();
        properties.setCredentialsJson(WEB_CLIENT_JSON);

        GoogleOAuthFlowProvider provider = new GoogleOAuthFlowProvider(properties);

        assertThat(provider.getFlow().getClientAuthentication()).isNotNull();
    }

    @Test
    void rejectsUnreadableCredentialsJson() {
        GoogleOAuthProperties properties = properties();
        properties.setCredentialsJson("{not-json");

        GoogleOAuthFlowProvider provider = new GoogleOAuthFlowProvider(properties);

        assertThatThrownBy(provider::getFlow)
                .isInstanceOf(GoogleOAuthNotConfiguredException.class)
                .hasMessageContaining("GOOGLE_OAUTH_CREDENTIALS_JSON");
    }

    private GoogleOAuthProperties properties() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        properties.setTokenStoreDirectory(tokenDirectory.toString());
        return properties;
    }
}
