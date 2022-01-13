package co.com.bancolombia.commons.jms.mq.config.utils;

import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationUtils {

    public static int resolveRetries(String maxRetriesStr) {
        try {
            int maxRetries = Integer.parseInt(maxRetriesStr);
            if (maxRetries < 0) {
                maxRetries = -1;
            }
            return maxRetries;
        } catch (Exception ignored) {
            return MQProperties.DEFAULT_MAX_RETRIES;
        }
    }

    public static int resolveConcurrency(int concurrencyAnnotation, int concurrencyProperties) {
        if (concurrencyAnnotation > 0) {
            return concurrencyAnnotation;
        }
        if (concurrencyProperties > 0) {
            return concurrencyProperties;
        }
        return MQProperties.DEFAULT_CONCURRENCY;
    }

    public static String resolveQueue(String primaryAnnotation, String secondaryValue, String queueProperties) {
        if (StringUtils.hasText(primaryAnnotation)) {
            return primaryAnnotation;
        }
        if (!StringUtils.hasText(secondaryValue) && StringUtils.hasText(queueProperties)) {
            return queueProperties;
        }
        return null;
    }
}
