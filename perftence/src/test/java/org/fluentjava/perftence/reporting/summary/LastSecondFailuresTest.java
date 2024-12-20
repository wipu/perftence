package org.fluentjava.perftence.reporting.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fluentjava.perftence.formatting.DefaultDoubleFormatter;
import org.fluentjava.perftence.formatting.FieldFormatter;
import org.junit.jupiter.api.Test;

public class LastSecondFailuresTest {

    @Test
    public void noFailures() throws InterruptedException {
        final LastSecondFailures lastSecondFailures = newInstance();
        final StringBuffer sb = new StringBuffer();
        final IntermediateSummary newSummary = newSummary(sb);
        lastSecondFailures.provideIntermediateSummary(newSummary);
        assertEquals("", sb.toString());
        Thread.sleep(1100);
        lastSecondFailures.provideIntermediateSummary(newSummary);
        assertEquals("", sb.toString());
    }

    @Test
    public void failures() throws InterruptedException {
        final LastSecondFailures lastSecondFailures = newInstance();
        lastSecondFailures.more(new FailIHave());
        final StringBuffer sb = new StringBuffer();
        final IntermediateSummary newSummary = newSummary(sb);
        lastSecondFailures.provideIntermediateSummary(newSummary);
        assertEquals("", sb.toString());
        Thread.sleep(1000);
        lastSecondFailures.provideIntermediateSummary(newSummary);
        assertEquals(
                "field|name:failed invocations:      |value:1endoflinetext|Exception summary:endoflinefield|name:org.fluentjava.perftence.reporting.summary.LastSecondFailuresTest$FailIHave:|value:1endofline",
                sb.toString());
    }

    private static LastSecondFailures newInstance() {
        return new LastSecondFailures(new FailedInvocationsFactory(new DefaultDoubleFormatter(),
                new AdjustedFieldBuilder(new FieldFormatter(), new FieldAdjuster())));
    }

    private static IntermediateSummary newSummary(final StringBuffer appendable) {
        return new IntermediateSummary() {

            @Override
            public IntermediateSummary text(String string) {
                appendable.append("text|" + string);
                return this;
            }

            @Override
            public IntermediateSummary field(final AdjustedField<?> field) {
                appendable.append("field|name:" + field.name() + "|value:" + field.value());
                return this;
            }

            @Override
            public IntermediateSummary endOfLine() {
                appendable.append("endofline");
                return this;
            }
        };
    }

    static class FailIHave extends RuntimeException {//
    }
}
