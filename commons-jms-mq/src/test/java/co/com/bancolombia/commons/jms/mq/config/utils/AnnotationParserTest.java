package co.com.bancolombia.commons.jms.mq.config.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.annotation.MergedAnnotation;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AnnotationParserTest {

    @Mock
    private MergedAnnotation<TestAnnotation> mergedAnnotation;

    @Test
    void testParseMergedAnnotation() {
        // Define annotation attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "TestValue");
        attributes.put("intValue", 123);

        // Stub the behavior of MergedAnnotation
        doReturn(attributes).when(mergedAnnotation).asMap(MergedAnnotation.Adapt.ANNOTATION_TO_MAP);
        doReturn(TestAnnotation.class).when(mergedAnnotation).getType();

        // Parse the mergedAnnotation
        TestAnnotation annotation = AnnotationParser.parseMergedAnnotation(mergedAnnotation);

        // Verify that the annotation was correctly parsed
        assertEquals("TestValue", annotation.value());
        assertEquals(123, annotation.intValue());
    }

    @Test
    void testParseMergedAnnotationWithArrayValue() {
        // Define annotation attributes with an array value
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("arrayValue", new String[]{"A", "B", "C"});

        // Stub the behavior of MergedAnnotation
        doReturn(attributes).when(mergedAnnotation).asMap(MergedAnnotation.Adapt.ANNOTATION_TO_MAP);
        doReturn(TestAnnotation.class).when(mergedAnnotation).getType();

        // Parse the mergedAnnotation
        TestAnnotation annotation = AnnotationParser.parseMergedAnnotation(mergedAnnotation);

        // Verify that the annotation was correctly parsed
        assertEquals("A", annotation.arrayValue()[0]);
        assertEquals("B", annotation.arrayValue()[1]);
        assertEquals("C", annotation.arrayValue()[2]);
    }

    // Define a custom annotation for testing purposes
    public @interface TestAnnotation {
        String value();

        int intValue();

        String[] arrayValue() default {};
    }
}
