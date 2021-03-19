package uk.gov.hmcts.reform.waworkflowapi.exceptions;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class IdempotencyTaskWorkerExceptionTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = IdempotencyTaskWorkerException.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.CONSTRUCTOR)
            .areWellImplemented();
    }
}
