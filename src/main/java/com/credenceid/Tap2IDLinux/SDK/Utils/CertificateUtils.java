package com.credenceid.Tap2IDLinux.SDK.Utils;

import java.io.*;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.io.InputStream;
import java.util.stream.Collectors;

public class CertificateUtils {

    private final static String reader_cert_file = "/etc/credenceid/cert/credenceid_mdl_reader_cert.pem";
    private final static String reader_cert_resource = "/certificates/credenceid_mdl_reader_cert.pem";

    private final static String root_cert_file = "/etc/credenceid/cert/credenceid_mdl_iaca_root.pem";
    private final static String root_cert_resource = "/certificates/credenceid_mdl_iaca_root.pem";
    private final static String EC_ALGO = "EC";
    private static String KEY =
            "MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDCaadxWbrTzJiM4ZoS8" +
                    "4OQyKzE+6SHDbjck6DUuUHMgbvR259rD+gvkmAQf6U/nx0ihZANiAASWU/+jB4yn" +
                    "C4iirHbR8+SH7hFLVPo+8meHFhSPiXF4zerUedSSfZXBfwTb5RHsZE5+lfQ9R54z" +
                    "Z402aLe1BBjjuu4HpFvRVLEW5ziz+WJlL/a3ycPuRLISxEL9LxeLvYA=";

    private static PrivateKey getPrivateKey(String privateKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(EC_ALGO);  // Assuming EC_ALGO is "EC"
        return kf.generatePrivate(spec);
    }

    private PrivateKey GetReaderCAPrivateKey(){
        try {
            PrivateKey privateKey = getPrivateKey(KEY);
            return privateKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static X509Certificate loadCertificateFromPem(String filePath) throws Exception {
        // Read all bytes from the file
        byte[] certBytes = Files.readAllBytes(new File(filePath).toPath());

        // Create CertificateFactory instance for X.509
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Generate certificate from bytes
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }


    public static X509Certificate getReaderCaCertificate() throws Exception {
        X509Certificate cert = getReaderCertificateFromResource();
        if(cert == null) {
            System.out.println("Unable to get ReaderCACertificate from Resource, now try file");
            cert = getReaderCertificateFromFile();
        }
        if(cert == null) {
            throw new RuntimeException("No certificate found in storage or resources");
        }
        return cert;
    }

    public static X509Certificate getRootCaCertificate() throws Exception {
        X509Certificate cert = getRootCertificateFromResource();
        if(cert == null) {
            System.out.println("Unable to get RootCACertificate from Resource, now try file");
            cert = getRootCertificateFromFile();
        }
        if(cert == null) {
            throw new RuntimeException("getRootCaCertificate Invalid Path");
        }
        System.out.println("RootCA Certificate is Proper");
        return cert;
    }

    private static X509Certificate getReaderCertificateFromStorage() {
        String rootDirPath = "/etc/credenceid"; // Replace with your actual storage path
        String folderReaderCertificate = "cert";
        File certFolder = new File(rootDirPath + folderReaderCertificate);
        File[] files = certFolder.listFiles();
        File certFile = (files != null && files.length > 0) ? files[0] : null;

        if (certFolder.exists() && certFile != null && certFile.exists()) {
            try (FileInputStream fis = new FileInputStream(certFile)) {
                CertificateFactory factory = CertificateFactory.getInstance("X509");
                return (X509Certificate) factory.generateCertificate(fis);
            } catch (Exception e) {
                System.err.println("[Exception] Failed to read Reader certificate from storage: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Local Reader Certificate path is either null or empty");
        }
        return null;
    }

    // Alternative: Load from filesystem if needed
    private static X509Certificate getReaderCertificateFromFile() throws Exception {
        byte[] certBytes = Files.readAllBytes(new File(reader_cert_file).toPath());
        CertificateFactory factory = CertificateFactory.getInstance("X509");
        return (X509Certificate) factory.generateCertificate(new java.io.ByteArrayInputStream(certBytes));
    }

    private static X509Certificate getReaderCertificateFromResource() throws Exception {
        // Load resource from classpath (e.g., src/main/resources/certificates/credenceid_mdl_reader_cert.pem)
        try (InputStream inputStream = CertificateUtils.class.getResourceAsStream(reader_cert_resource)) {
            if (inputStream == null) {
                System.out.println("Resource not found: " + reader_cert_resource);
                return null;
            }
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            return (X509Certificate) factory.generateCertificate(inputStream);
        }
    }

    private static X509Certificate getRootCertificateFromFile() throws Exception {
        byte[] certBytes = Files.readAllBytes(new File(root_cert_file).toPath());
        CertificateFactory factory = CertificateFactory.getInstance("X509");
        return (X509Certificate) factory.generateCertificate(new java.io.ByteArrayInputStream(certBytes));
    }

    private static X509Certificate getRootCertificateFromResource() throws Exception {
        // Load resource from classpath (e.g., src/main/resources/certificates/credenceid_mdl_reader_cert.pem)
        try (InputStream inputStream = CertificateUtils.class.getResourceAsStream(root_cert_resource)) {
            if (inputStream == null) {
                System.out.println("Resource not found: " + root_cert_resource);
                return null;
            }
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            return (X509Certificate) factory.generateCertificate(inputStream);
        }
    }

    public static PrivateKey getReaderCAPrivateKey() throws Exception {
        try {
            PrivateKey privateKey = getPrivateKey(KEY);
            return privateKey;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey getReaderPrivateKeyFromFile(String filePath) throws Exception {
        String keyString = readKeyFromFile(new File(filePath));
        return getPrivateKey(keyString);
    }

    private static String readKeyFromFile(File keyFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(keyFile)) {
            return readKeyFromStream(fis);
        }
    }

    private static String readKeyFromStream(InputStream inputStream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .filter(line -> !line.startsWith("-----BEGIN") && !line.startsWith("-----END"))
                    .collect(Collectors.joining())
                    .trim();
        }
    }


}
