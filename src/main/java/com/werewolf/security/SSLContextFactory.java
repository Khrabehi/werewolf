package com.werewolf.security;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * factory used to generate SSLContext using CertificateManager those factory
 * will provide secured sockets.
 */
public class SSLContextFactory {
    private static final String TLS_VERSION = "TLSv1.3"; // Modern standard

    /**
     * Creates an SSLContext for the Server configured for mTLS.
     * Requires a KeyStore (Server's identity) and a TrustStore (To validate
     * Client's identity).
     */
    public static SSLContext createServerSSLContext(String keyStorePath, String keyStorePassword,
            String trustStorePath, String trustStorePassword) throws Exception {

        // Initialize KeyManager (Server Identity)
        KeyStore keyStore = CertificateManager.loadKeyStore(keyStorePath, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        // Initialize TrustManager (Validating Clients)
        KeyStore trustStore = CertificateManager.loadKeyStore(trustStorePath, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Create and initialize the SSLContext
        SSLContext sslContext = SSLContext.getInstance(TLS_VERSION);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    /**
     * Creates an SSLContext for the Client configured for mTLS.
     * Requires a KeyStore (Client's identity) and a TrustStore (To validate
     * Server's identity).
     */
    public static SSLContext createClientSSLContext(String keyStorePath, String keyStorePassword,
            String trustStorePath, String trustStorePassword) throws Exception {

        // Initialize KeyManager (Client Identity)
        KeyStore keyStore = CertificateManager.loadKeyStore(keyStorePath, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        // Initialize TrustManager (Validating Server)
        KeyStore trustStore = CertificateManager.loadKeyStore(trustStorePath, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Create and initialize the SSLContext
        SSLContext sslContext = SSLContext.getInstance(TLS_VERSION);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }
}
