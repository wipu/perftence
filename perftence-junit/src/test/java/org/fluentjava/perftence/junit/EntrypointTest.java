package org.fluentjava.perftence.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(DefaultTestRunner.class)
public class EntrypointTest extends AbstractMultiThreadedTest {

    @Test
    public void checkSetup() {
        assertNotNull(setup());
    }

    @Test
    public void checkRequirements() {
        assertNotNull(requirements());
    }

    @Test
    public void checkAgent() {
        assertNotNull(agentBasedTest());
    }

    @Test
    public void checkNamedAgent() {
        assertNotNull(agentBasedTest("name"));
    }

    @Test
    public void checkFluent() {
        assertNotNull(test());
    }

    @Test
    public void checkNamedFluent() {
        assertNotNull(test("name"));
    }

    @Test
    public void checkFailureNotifier() {
        assertNotNull(failureNotifier());
    }

    @Test
    public void checkTestMethodName() {
        assertEquals("checkTestMethodName", testMethodName());
    }

    @Test
    public void checkId() {
        assertEquals("org.fluentjava.perftence.junit.EntrypointTest.checkId", id());
    }

    @Test
    public void checkFullyQualifiedMethodNameWithClassName() {
        assertEquals("org.fluentjava.perftence.junit.EntrypointTest.checkFullyQualifiedMethodNameWithClassName",
                fullyQualifiedMethodNameWithClassName());
    }

}
