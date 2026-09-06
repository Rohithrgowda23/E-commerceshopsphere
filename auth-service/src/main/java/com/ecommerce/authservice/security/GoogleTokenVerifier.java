package com.ecommerce.authservice.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Verifies a Google-issued ID token's signature, audience (our client id),
 * issuer, and expiry against Google's public keys. This is the entire
 * trust boundary for Google login — once a token passes this, its email
 * claim is treated as verified identity.
 */
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.oauth.client-id}") String googleClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidGoogleTokenException("Google ID token verification failed");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidGoogleTokenException("Could not verify Google ID token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidGoogleTokenException("Malformed Google ID token");
        }
    }
}