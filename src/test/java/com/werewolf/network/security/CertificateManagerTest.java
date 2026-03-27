package com.werewolf.network.security;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.werewolf.security.CertificateManager;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CertificateManagerTest {

    private static final String TEST_PASSWORD = "test_password";

    @Test
    void testCertificateGenerationAndLoading() throws Exception {
        CertificateManager.initializeCertificates(TEST_PASSWORD);

        assertTrue(new File(CertificateManager.CA_CERT).exists(), "Root CA certificate is missing");
        assertTrue(new File(CertificateManager.CA_KEYSTORE).exists(), "CA keystore is missing");
        assertTrue(new File(CertificateManager.SERVER_KEYSTORE).exists(), "Server keystore is missing");
        assertTrue(new File(CertificateManager.SERVER_TRUSTSTORE).exists(), "Server truststore is missing");
        assertTrue(new File(CertificateManager.CLIENT_KEYSTORE).exists(), "Client keystore is missing");
        assertTrue(new File(CertificateManager.CLIENT_TRUSTSTORE).exists(), "Client truststore is missing");

        KeyStore serverKeyStore = CertificateManager.loadKeyStore(CertificateManager.SERVER_KEYSTORE, TEST_PASSWORD);
        assertNotNull(serverKeyStore, "Server KeyStore could not be loaded");
        assertTrue(serverKeyStore.containsAlias("server"), "KeyStore should contain the alias 'server'");
    }

    @Test
    void testLoadKeyStoreWithInvalidPassword() {
        CertificateManager.initializeCertificates(TEST_PASSWORD);
        
        Exception exception = assertThrows(Exception.class, () -> {
            CertificateManager.loadKeyStore(CertificateManager.SERVER_KEYSTORE, "wrong_password");
        });
        
        assertNotNull(exception, "An exception should have been raised for an incorrect password");
    }

    @Test
    void testLoadKeyStoreWithMissingFile() {
        Exception exception = assertThrows(Exception.class, () -> {
            CertificateManager.loadKeyStore("src/main/resources/certificates/non_existent.p12", TEST_PASSWORD);
        });
        
        assertNotNull(exception, "An exception should have been raised for a non-existent file");
    }

    @Test
    void testCertificateValidityAndChain() throws Exception {
        CertificateManager.initializeCertificates(TEST_PASSWORD);
        KeyStore serverKeyStore = CertificateManager.loadKeyStore(CertificateManager.SERVER_KEYSTORE, TEST_PASSWORD);
        
        X509Certificate cert = (X509Certificate) serverKeyStore.getCertificate("server");
        assertNotNull(cert, "Server certificate should be present");
        
        // Verify that the certificate is valid at the current date and time (not expired)
        assertDoesNotThrow(() -> cert.checkValidity(new Date()), "Generated certificate should be valid today");
    }

    @AfterAll
    static void cleanUp() {
        new File(CertificateManager.CA_CERT).delete();
        new File(CertificateManager.CA_KEYSTORE).delete();
        new File(CertificateManager.SERVER_KEYSTORE).delete();
        new File(CertificateManager.SERVER_TRUSTSTORE).delete();
        new File(CertificateManager.CLIENT_KEYSTORE).delete();
        new File(CertificateManager.CLIENT_TRUSTSTORE).delete();
    }
}