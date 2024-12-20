package org.fluentjava.perftence.agents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.fluentjava.perftence.formatting.FieldFormatter;
import org.fluentjava.perftence.reporting.summary.FieldAdjuster;
import org.fluentjava.perftence.reporting.summary.SummaryField;
import org.fluentjava.perftence.reporting.summary.SummaryFieldFactory;
import org.junit.jupiter.api.Test;

public class SummaryFieldFactoryForAgentBasedTestsTest {

    @Test
    public void lastTaskToBeRun() {
        final SummaryFieldFactoryForAgentBasedTests factory = newFactory();
        final SummaryField<String> lastTaskToBeRun = factory.lastTaskToBeRun(TimeSpecificationFactory.inMillis(1000));
        assertNotNull(lastTaskToBeRun.value());
        assertEquals("in 1000 (ms)", lastTaskToBeRun.value());
        assertEquals("last task to be run:     ", lastTaskToBeRun.name());

    }

    @Test
    public void lastTaskToBeRunTimeNotAvailable() {
        final SummaryFieldFactoryForAgentBasedTests factory = newFactory();
        final SummaryField<String> lastTaskToBeRun = factory.lastTaskToBeRun(null);
        assertNotNull(lastTaskToBeRun.value());
        assertEquals("<not available>", lastTaskToBeRun.value());
        assertEquals("last task to be run:     ", lastTaskToBeRun.name());
    }

    private static SummaryFieldFactoryForAgentBasedTests newFactory() {
        return new SummaryFieldFactoryForAgentBasedTests(
                SummaryFieldFactory.create(new FieldFormatter(), new FieldAdjuster()));
    }
}
