package co.com.bancolombia.commons.jms.api.model;

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
public class JmsMessage {
    private String messageID;
    private String correlationID;
    private String body;
    private long timestamp;
}
