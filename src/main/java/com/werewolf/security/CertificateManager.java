package com.werewolf.security;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Arrays;

public class CertificateManager {

    private static final String CERTS_DIR = "certs" + File.separator;
    public static final String CA_CERT = CERTS_DIR + "ca.crt";
    public static final String CA_KEYSTORE = CERTS_DIR + "ca.p12";

    public static final String SERVER_KEYSTORE = CERTS_DIR + "server.p12";
    public static final String SERVER_TRUSTSTORE = CERTS_DIR + "server_trust.p12";

    public static final String CLIENT_KEYSTORE = CERTS_DIR + "client.p12";
    public static final String CLIENT_TRUSTSTORE = CERTS_DIR + "client_trust.p12";

    /**
     * Loads a KeyStore from a PKCS12 file.
     *
     * @param path     The path to the .p12 file
     * @param password The password for the keystore
     * @return The loaded KeyStore object
     */
    public static KeyStore loadKeyStore(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password.toCharArray());
        }
        return keyStore;
    }

    /**
     * Orchestrates the entire mTLS certificate generation process.
     * Creates a CA, signs server/client certs, and builds truststores.
     * * @param password The shared password for all stores in this dev environment
     */
    public static void initializeCertificates(String password) {
        File dir = new File("certs");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Skip generation if the CA already exists
        if (new File(CA_KEYSTORE).exists() && new File(SERVER_KEYSTORE).exists()) {
            System.out.println("Certificates already exist. Skipping generation.");
            return;
        }

        System.out.println("Initializing mTLS Certificate Infrastructure...");

        try {
            // Generate CA (Certificate Authority)
            runKeytool("-genkeypair", "-alias", "ca", "-keyalg", "RSA", "-keysize", "2048",
                    "-storetype", "PKCS12", "-keystore", CA_KEYSTORE, "-storepass", password,
                    "-validity", "3650", "-ext", "bc:c", "-dname", "CN=WerewolfCA, OU=GameDev, O=Werewolf, C=FR");

            // Export CA public certificate
            runKeytool("-exportcert", "-alias", "ca", "-keystore", CA_KEYSTORE,
                    "-storepass", password, "-file", CA_CERT);

            // Generate and Sign Server Certificate
            generateAndSignCert("server", "CN=WerewolfServer, OU=GameDev, O=Werewolf, C=FR", SERVER_KEYSTORE, password);

            // Generate and Sign Client Certificate
            generateAndSignCert("client", "CN=WerewolfClient, OU=GameDev, O=Werewolf, C=FR", CLIENT_KEYSTORE, password);

            // Create Truststores (importing the CA cert so both sides trust each other)
            runKeytool("-importcert", "-alias", "ca", "-keystore", SERVER_TRUSTSTORE,
                    "-storepass", password, "-file", CA_CERT, "-noprompt");

            runKeytool("-importcert", "-alias", "ca", "-keystore", CLIENT_TRUSTSTORE,
                    "-storepass", password, "-file", CA_CERT, "-noprompt");

            System.out.println("mTLS Infrastructure generated successfully in /certs folder.");

        } catch (Exception e) {
            System.err.println("Error generating certificates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to generate a keypair, create a CSR, sign it with the CA, and
     * import it back.
     */
    private static void generateAndSignCert(String alias, String dname, String keystorePath, String password)
            throws Exception {
        String csrFile = CERTS_DIR + alias + ".csr";
        String crtFile = CERTS_DIR + alias + ".crt";

        // Generate Keypair
        runKeytool("-genkeypair", "-alias", alias, "-keyalg", "RSA", "-keysize", "2048",
                "-storetype", "PKCS12", "-keystore", keystorePath, "-storepass", password,
                "-validity", "365", "-dname", dname);

        // Generate Certificate Signing Request (CSR)
        runKeytool("-certreq", "-alias", alias, "-keystore", keystorePath,
                "-storepass", password, "-file", csrFile);

        // CA signs the CSR
        runKeytool("-gencert", "-alias", "ca", "-keystore", CA_KEYSTORE, "-storepass", password,
                "-infile", csrFile, "-outfile", crtFile, "-validity", "365");

        // Import CA cert into the target keystore (required to establish chain)
        runKeytool("-importcert", "-alias", "ca", "-keystore", keystorePath,
                "-storepass", password, "-file", CA_CERT, "-noprompt");

        // Import the newly signed certificate back into the keystore
        runKeytool("-importcert", "-alias", alias, "-keystore", keystorePath,
                "-storepass", password, "-file", crtFile);

        // Cleanup temporary CSR and CRT files
        new File(csrFile).delete();
        new File(crtFile).delete();
    }

    /**
     * Helper to execute keytool commands via ProcessBuilder.
     */
    private static void runKeytool(String... args) throws Exception {
        String[] command = new String[args.length + 1];
        command[0] = "keytool";
        System.arraycopy(args, 0, command, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Keytool command failed: " + Arrays.toString(command));
        }
    }
}
