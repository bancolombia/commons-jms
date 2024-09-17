package co.com.bancolombia.commons.jms.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPathBuilder;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SSLContextUtils {
    public static final String TLS = "TLSv1.3";
    public static final String JKS = "JKS";
    public static final String PKIX = "PKIX";
    public static final String X_509 = "X.509";
    public static final String RSA = "RSA";

    public static final List<String> DEFAULT_CERT_PATHS =
            List.of("/usr/local/share/ca-certificates", "/etc/pki/ca-trust/source/anchor");

    @SneakyThrows
    public static SSLContext buildSSLContextFromJks(String jksPath, String jksPassword) {
        try (InputStream cert = new FileInputStream(jksPath)) {
            final KeyStore caCertsKeyStore = KeyStore.getInstance(JKS);
            if (jksPassword != null) {
                caCertsKeyStore.load(cert, jksPassword.toCharArray());
            }
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            CertPathBuilder cpb = CertPathBuilder.getInstance(PKIX);
            PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
            rc.setOptions(EnumSet.of(
                    PKIXRevocationChecker.Option.PREFER_CRLS,
                    PKIXRevocationChecker.Option.ONLY_END_ENTITY,
                    PKIXRevocationChecker.Option.SOFT_FAIL,
                    PKIXRevocationChecker.Option.NO_FALLBACK));
            PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(caCertsKeyStore, new X509CertSelector());
            pkixParams.addCertPathChecker(rc);
            if (jksPassword != null) {
                kmf.init(caCertsKeyStore, jksPassword.toCharArray());
            }
            tmf.init(new CertPathTrustManagerParameters(pkixParams));
            final SSLContext sslContext = SSLContext.getInstance(TLS);
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            return sslContext;
        }
    }

    @SneakyThrows
    public static SSLContext buildSSLContextFromPem(SSLCredentials credentials) {
        return buildSSLContextFromPem(credentials, DEFAULT_CERT_PATHS);
    }

    @SneakyThrows
    public static SSLContext buildSSLContextFromPem(SSLCredentials credentials, List<String> additionalTrustStore) {
        KeyStore keyStore = createKeyStore(credentials);
        KeyStore trustStore = createTrustStore(credentials, additionalTrustStore);
        return createSSLContext(credentials, keyStore, trustStore);
    }

    protected static KeyStore createKeyStore(SSLCredentials credentials) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(JKS);
        keyStore.load(null, null);

        CertificateFactory certFactory = CertificateFactory.getInstance(X_509);
        X509Certificate cert = (X509Certificate) certFactory
                .generateCertificate(new ByteArrayInputStream(credentials.getCer().getBytes()));

        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder()
                .decode(credentials.getKey().replaceAll("-----\\w+ PRIVATE KEY-----", "")
                        .replaceAll("\\s", ""))));

        KeyStore.PrivateKeyEntry keyEntry = new KeyStore.PrivateKeyEntry(privateKey, new Certificate[]{cert});
        keyStore.setEntry("MQ_KEY", keyEntry, new KeyStore.PasswordProtection(credentials.getPassphrase()
                .toCharArray()));

        return keyStore;
    }

    protected static KeyStore createTrustStore(SSLCredentials credentials, List<String> additionalTrustStore) throws CertificateException, KeyStoreException,
            IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(JKS);
        trustStore.load(null, null);
        AtomicInteger sequence = new AtomicInteger(0);
        CertificateFactory certFactory = CertificateFactory.getInstance(X_509);
        loadFromPem(sequence, certFactory, trustStore, credentials.getChain());
        log.info(sequence.get() + " entries loaded from MQ cert chain");
        for (String path : additionalTrustStore) {
            Path dir = Path.of(path);
            if (Files.exists(dir)) {
                loadFromDir(dir, sequence, certFactory, trustStore);
            }
        }
        log.info("Trust Store created with " + sequence.get() + " entries");
        return trustStore;
    }

    protected static void loadFromDir(Path dir, AtomicInteger sequence, CertificateFactory certFactory, KeyStore trustStore)
            throws IOException, KeyStoreException, CertificateException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {  // Check if the entry is a file
                    String pem = Files.readString(entry);
                    loadFromPem(sequence, certFactory, trustStore, pem);
                }
            }
        }
    }

    protected static void loadFromPem(AtomicInteger sequence, CertificateFactory certFactory, KeyStore trustStore,
                                      String certs) throws KeyStoreException, CertificateException {
        String[] pemCerts = certs.split("-----END CERTIFICATE-----");
        for (String pem : pemCerts) {
            if (pem.contains("BEGIN CERTIFICATE")) {
                String fullPem = pem + "-----END CERTIFICATE-----\n";
                X509Certificate cert = (X509Certificate) certFactory
                        .generateCertificate(new ByteArrayInputStream(fullPem.getBytes()));
                log.log(Level.FINE, "Loading cert: " + cert.getSubjectX500Principal().getName());
                trustStore.setCertificateEntry(Integer.toString(sequence.incrementAndGet()), cert);
            }
        }
    }

    protected static SSLContext createSSLContext(SSLCredentials credentials, KeyStore keyStore, KeyStore trustStore)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, credentials.getPassphrase().toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance(TLS);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }
}
