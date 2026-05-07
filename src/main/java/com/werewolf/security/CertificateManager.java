package com.werewolf.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;

public class CertificateManager {

        // Emplacement cible pour les artefacts de certificats — emplacement externe en écriture en dehors du code source
    private static final String CERTS_DIR = System.getProperty("user.home") + File.separator
            + ".werewolf" + File.separator + "certificates" + File.separator;

    // Noms de fichiers des certificats selon les spécifications
    public static final String CA_CERT = CERTS_DIR + "rootCA.cer";
    public static final String CA_KEYSTORE = CERTS_DIR + "rootCA.p12"; // Internal keystore for the CA

    public static final String SERVER_KEYSTORE = CERTS_DIR + "server.p12";
    public static final String SERVER_TRUSTSTORE = CERTS_DIR + "server-truststore.p12";

    public static final String CLIENT_KEYSTORE = CERTS_DIR + "client.p12";
    public static final String CLIENT_TRUSTSTORE = CERTS_DIR + "client-truststore.p12";

        /**
         * Charge un {@link KeyStore} depuis un fichier PKCS12.
         *
         * @param path     Le chemin vers le fichier .p12
         * @param password Le mot de passe du keystore
         * @return Le KeyStore chargé
         */
        public static KeyStore loadKeyStore(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password.toCharArray());
        }
        return keyStore;
    }

        /**
         * Orchestration du processus de génération de certificats mTLS.
         * Crée une CA, signe les certificats serveur/client et génère les truststores.
         * @param password Le mot de passe partagé pour tous les stores dans cet environnement de développement
         */
        public static void initializeCertificates(String password) {
                File dir = new File(CERTS_DIR);
                if (!dir.exists()) {
                        dir.mkdirs();
                }

                // Vérifie l'existence de tous les artefacts de certificats attendus
                File[] expectedFiles = new File[] {
                                new File(CA_CERT),
                                new File(CA_KEYSTORE),
                                new File(SERVER_KEYSTORE),
                                new File(SERVER_TRUSTSTORE),
                                new File(CLIENT_KEYSTORE),
                                new File(CLIENT_TRUSTSTORE)
                };

                boolean allExist = Arrays.stream(expectedFiles).allMatch(File::exists);
                boolean anyExist = Arrays.stream(expectedFiles).anyMatch(File::exists);

                // Ignore la génération uniquement si tous les artefacts existent déjà et sont lisibles
                if (allExist) {
                        if (areExistingCertificatesUsable(password)) {
                                System.out.println("Certificates already exist and are usable. Skipping generation.");
                                return;
                        }

                        System.out.println("Existing certificate artifacts could not be loaded with the current password. Regenerating...");
                        deleteExpectedFiles(expectedFiles);
                }

                // En cas d'état partiellement généré, supprime les artefacts existants avant régénération
                if (anyExist && !allExist) {
                        System.out.println("Detected partially initialized certificate infrastructure. "
                                        + "Cleaning up existing certificate artifacts before regeneration.");
                        deleteExpectedFiles(expectedFiles);
                }

                System.out.println("Initializing mTLS Certificate Infrastructure...");

                try {
                        // Génère la CA (Autorité de certification)
                        runKeytool("-genkeypair", "-alias", "ca", "-keyalg", "RSA", "-keysize", "2048",
                                        "-storetype", "PKCS12", "-keystore", CA_KEYSTORE, "-storepass", password,
                                        "-validity", "3650", "-ext", "bc:c", "-dname", "CN=WerewolfCA, OU=GameDev, O=Werewolf, C=FR");

                        // Exporte le certificat public de la CA
                        runKeytool("-exportcert", "-alias", "ca", "-keystore", CA_KEYSTORE,
                                        "-storepass", password, "-file", CA_CERT);

                        // Génère et signe le certificat serveur
                        generateAndSignCert("server", "CN=WerewolfServer, OU=GameDev, O=Werewolf, C=FR", SERVER_KEYSTORE, password);

                        // Génère et signe le certificat client
                        generateAndSignCert("client", "CN=WerewolfClient, OU=GameDev, O=Werewolf, C=FR", CLIENT_KEYSTORE, password);

                        // Crée les truststores (importe le certificat CA afin que les deux côtés se fassent confiance)
                        runKeytool("-importcert", "-alias", "ca", "-keystore", SERVER_TRUSTSTORE,
                                        "-storepass", password, "-file", CA_CERT, "-noprompt");

                        runKeytool("-importcert", "-alias", "ca", "-keystore", CLIENT_TRUSTSTORE,
                                        "-storepass", password, "-file", CA_CERT, "-noprompt");

                        System.out.println("mTLS Infrastructure generated successfully in " + CERTS_DIR + " folder.");

                } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to initialize mTLS certificates", e);
                }
        }

        private static boolean areExistingCertificatesUsable(String password) {
                try {
                        loadKeyStore(CA_KEYSTORE, password);
                        loadKeyStore(SERVER_KEYSTORE, password);
                        loadKeyStore(CLIENT_KEYSTORE, password);
                        loadKeyStore(SERVER_TRUSTSTORE, password);
                        loadKeyStore(CLIENT_TRUSTSTORE, password);
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        private static void deleteExpectedFiles(File[] expectedFiles) {
                for (File file : expectedFiles) {
                        if (file.exists() && !file.delete()) {
                                System.err.println("Warning: Failed to delete existing certificate file: "
                                                + file.getAbsolutePath());
                        }
                }
        }

    /**
     * Méthode utilitaire pour générer une paire de clés, créer une CSR, la faire signer par la CA,
     * puis l'importer dans le keystore.
     */
    private static void generateAndSignCert(String alias, String dname, String keystorePath, String password)
            throws Exception {
        String csrFile = CERTS_DIR + alias + ".csr";
        String crtFile = CERTS_DIR + alias + ".crt";

        // Génère une paire de clés
        runKeytool("-genkeypair", "-alias", alias, "-keyalg", "RSA", "-keysize", "2048",
                "-storetype", "PKCS12", "-keystore", keystorePath, "-storepass", password,
                "-validity", "365", "-dname", dname);

        // Génère une demande de signature de certificat (CSR)
        runKeytool("-certreq", "-alias", alias, "-keystore", keystorePath,
                "-storepass", password, "-file", csrFile);

        // La CA signe la CSR
        runKeytool("-gencert", "-alias", "ca", "-keystore", CA_KEYSTORE, "-storepass", password,
                "-infile", csrFile, "-outfile", crtFile, "-validity", "365");

        // Importe le certificat CA dans le keystore cible (nécessaire pour établir la chaîne)
        runKeytool("-importcert", "-alias", "ca", "-keystore", keystorePath,
                "-storepass", password, "-file", CA_CERT, "-noprompt");

        // Importe le certificat nouvellement signé dans le keystore
        runKeytool("-importcert", "-alias", alias, "-keystore", keystorePath,
                "-storepass", password, "-file", crtFile);

        // Nettoie les fichiers temporaires CSR et CRT
        new File(csrFile).delete();
        new File(crtFile).delete();
    }

        /**
         * Utilitaire pour exécuter des commandes keytool via ProcessBuilder.
         * Résout le chemin de keytool depuis le `java.home` courant pour éviter les problèmes de PATH.
         * Capture stdout/stderr pour inclure la sortie dans les erreurs éventuelles.
         */
        private static void runKeytool(String... args) throws Exception {
        String keytoolPath = System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "keytool"
                + (System.getProperty("os.name", "").toLowerCase().contains("win") ? ".exe" : "");

        String[] command = new String[args.length + 1];
        command[0] = keytoolPath;
        System.arraycopy(args, 0, command, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // Fusionne stderr dans stdout pour éviter tout blocage

        Process process = pb.start();

        // Lit toute la sortie avant d'attendre pour éviter un blocage dû à un tampon plein
        String output;
        try (InputStream is = process.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            is.transferTo(baos);
            output = baos.toString();
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Keytool command failed with exit code " + exitCode
                    + ": " + Arrays.toString(command) + "\nOutput: " + output);
        }
    }
}
