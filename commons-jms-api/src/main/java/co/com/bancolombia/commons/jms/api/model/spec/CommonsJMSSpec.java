package co.com.bancolombia.commons.jms.api.model.spec;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CommonsJMSSpec {
    private final Map<String, MQDomainSpec> domains;

    public static CommonsJMSSpecBuilder builder() {
        return new CommonsJMSSpecBuilder(new HashMap<>());
    }

    @AllArgsConstructor
    public static class CommonsJMSSpecBuilder {
        private final Map<String, MQDomainSpec> domains;

        public CommonsJMSSpec build() {
            return new CommonsJMSSpec(domains);
        }

        public CommonsJMSSpecBuilder withDomain(MQDomainSpec spec) {
            if (domains.containsKey(spec.getName())) {
                throw new IllegalArgumentException("Domain " + spec.getName() + " already exists");
            }
            domains.put(spec.getName(), spec);
            return this;
        }
    }
}
