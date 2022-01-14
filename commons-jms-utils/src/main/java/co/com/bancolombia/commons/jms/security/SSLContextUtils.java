package co.com.bancolombia.commons.jms.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertPathBuilder;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.X509CertSelector;
import java.util.EnumSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SSLContextUtils {
    public static final String TLS = "TLSv1.3";
    public static final String JKS = "JKS";
    public static final String PKIX = "PKIX";

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
}
