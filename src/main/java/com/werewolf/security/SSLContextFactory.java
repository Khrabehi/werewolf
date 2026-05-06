package com.werewolf.security;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Fabrique utilisée pour générer des {@link SSLContext} en utilisant {@link CertificateManager}.
 * Cette fabrique fournit des sockets sécurisés (mTLS).
 */
public class SSLContextFactory {
    private static final String TLS_VERSION = "TLSv1.3"; // Standard moderne

    /**
     * Crée un {@link SSLContext} côté serveur configuré pour le mTLS.
     * Nécessite un KeyStore (identité du serveur) et un TrustStore (pour valider
     * l'identité des clients).
     */
    public static SSLContext createServerSSLContext(String keyStorePath, String keyStorePassword,
            String trustStorePath, String trustStorePassword) throws Exception {

        // Initialise le KeyManager (identité du serveur)
        KeyStore keyStore = CertificateManager.loadKeyStore(keyStorePath, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        // Initialise le TrustManager (validation des clients)
        KeyStore trustStore = CertificateManager.loadKeyStore(trustStorePath, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Crée et initialise le SSLContext
        SSLContext sslContext = SSLContext.getInstance(TLS_VERSION);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    /**
     * Crée un {@link SSLContext} côté client configuré pour le mTLS.
     * Nécessite un KeyStore (identité du client) et un TrustStore (pour valider
     * l'identité du serveur).
     */
    public static SSLContext createClientSSLContext(String keyStorePath, String keyStorePassword,
            String trustStorePath, String trustStorePassword) throws Exception {

        // Initialise le KeyManager (identité du client)
        KeyStore keyStore = CertificateManager.loadKeyStore(keyStorePath, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        // Initialise le TrustManager (validation du serveur)
        KeyStore trustStore = CertificateManager.loadKeyStore(trustStorePath, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Crée et initialise le SSLContext
        SSLContext sslContext = SSLContext.getInstance(TLS_VERSION);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }
}
