package com.werewolf.network.security;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.werewolf.security.CertificateManager;
import com.werewolf.security.SSLContextFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SSLContextFactoryTest {

    private static final String TEST_PASSWORD = "test_password";

    @BeforeAll
    static void setUp() {
        CertificateManager.initializeCertificates(TEST_PASSWORD);
    }

    @Test
    void testCreateServerSSLContext() throws Exception {
        SSLContext serverContext = SSLContextFactory.createServerSSLContext(
                CertificateManager.SERVER_KEYSTORE, TEST_PASSWORD,
                CertificateManager.SERVER_TRUSTSTORE, TEST_PASSWORD
        );

        assertNotNull(serverContext);
        assertEquals("TLSv1.3", serverContext.getProtocol(), "Protocol must be TLSv1.3");
    }

    @Test
    void testCreateClientSSLContext() throws Exception {
        SSLContext clientContext = SSLContextFactory.createClientSSLContext(
                CertificateManager.CLIENT_KEYSTORE, TEST_PASSWORD,
                CertificateManager.CLIENT_TRUSTSTORE, TEST_PASSWORD
        );

        assertNotNull(clientContext);
        assertEquals("TLSv1.3", clientContext.getProtocol(), "Protocol must be TLSv1.3");
    }

    @Test
    void testCreateContextWithInvalidPassword() {
        assertThrows(Exception.class, () -> {
            SSLContextFactory.createServerSSLContext(
                    CertificateManager.SERVER_KEYSTORE, "wrong_password",
                    CertificateManager.SERVER_TRUSTSTORE, TEST_PASSWORD
            );
        }, "Creating a context with an incorrect password should fail");
    }

    @Test
    void testMTLSHandshakeEndToEnd() throws Exception {
        // Initialize SSL contexts
        SSLContext serverContext = SSLContextFactory.createServerSSLContext(
                CertificateManager.SERVER_KEYSTORE, TEST_PASSWORD,
                CertificateManager.SERVER_TRUSTSTORE, TEST_PASSWORD
        );
        SSLContext clientContext = SSLContextFactory.createClientSSLContext(
                CertificateManager.CLIENT_KEYSTORE, TEST_PASSWORD,
                CertificateManager.CLIENT_TRUSTSTORE, TEST_PASSWORD
        );

        SSLServerSocketFactory ssf = serverContext.getServerSocketFactory();
        
        // Start server on a random available port (port 0)
        try (SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(0)) {
            serverSocket.setNeedClientAuth(true); // requirement for mTLS
            int port = serverSocket.getLocalPort();

            CountDownLatch latch = new CountDownLatch(1);
            Exception[] serverException = new Exception[1];

            // Server thread to listen for incoming connection
            Thread serverThread = new Thread(() -> {
                try (SSLSocket acceptedSocket = (SSLSocket) serverSocket.accept()) {
                    acceptedSocket.startHandshake();
                } catch (Exception e) {
                    serverException[0] = e;
                } finally {
                    latch.countDown(); // Ensure latch is always decremented on success or failure
                }
            });
            serverThread.start();

            SSLSocketFactory csf = clientContext.getSocketFactory();
            try (SSLSocket clientSocket = (SSLSocket) csf.createSocket("localhost", port)) {
                clientSocket.startHandshake(); // Triggers mutual authentication
            }
            
            // Wait maximally for 2 seconds to avoid infinite blocking
            boolean completed = latch.await(2, TimeUnit.SECONDS);
            
            assertTrue(completed, "TLS handshake timed out or failed");
            assertNull(serverException[0], "Server threw an exception during mTLS handshake");
        }
    }

    @AfterAll
    static void cleanUp() {
        // Systematic cleanup to ensure repeatable tests
        new File(CertificateManager.CA_CERT).delete();
        new File(CertificateManager.CA_KEYSTORE).delete();
        new File(CertificateManager.SERVER_KEYSTORE).delete();
        new File(CertificateManager.SERVER_TRUSTSTORE).delete();
        new File(CertificateManager.CLIENT_KEYSTORE).delete();
        new File(CertificateManager.CLIENT_TRUSTSTORE).delete();
    }
}