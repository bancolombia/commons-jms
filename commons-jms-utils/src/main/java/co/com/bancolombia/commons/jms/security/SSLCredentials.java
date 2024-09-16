package co.com.bancolombia.commons.jms.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSLCredentials {
    private String cer;
    private String key;
    private String chain;
    private String passphrase;
}
