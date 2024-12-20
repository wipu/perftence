package org.fluentjava.perftence.agents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.perftence.DefaultLatencyProviderFactory;
import org.fluentjava.perftence.LatencyProvider;
import org.fluentjava.perftence.reporting.TestRuntimeReporter;
import org.junit.jupiter.api.Test;

public final class CategorySpecificLatenciesTest implements ReporterFactoryForCategorySpecificLatencies {

    @Test
    @SuppressWarnings("unused")
    public void reporterFactoryNull() {
        assertThrows(NullPointerException.class, () -> {
            new CategorySpecificLatencies(null, this);
        });
    }

    @Test
    @SuppressWarnings({ "unused", "static-method" })
    public void invocationReporterNull() {
        assertThrows(NullPointerException.class, () -> {
            new CategorySpecificLatencies(
                    new DefaultCategorySpecificReporterFactory("id", new DefaultLatencyProviderFactory()), null);
        });
    }

    @Test
    public void empty() {
        assertFalse(new CategorySpecificLatencies(
                new DefaultCategorySpecificReporterFactory("id", new DefaultLatencyProviderFactory()), this)
                        .hasCategorySpecificReporters());
    }

    @Test
    public void oneAppender() {
        final DefaultCategorySpecificReporterFactory defaultCategorySpecificReporterFactory = new DefaultCategorySpecificReporterFactory(
                "name", new DefaultLatencyProviderFactory());
        final CategorySpecificLatencies categorySpecificLatencies = new CategorySpecificLatencies(
                defaultCategorySpecificReporterFactory, this);
        assertFalse(categorySpecificLatencies.hasCategorySpecificReporters());
        final Category categoryOne = Category.One;
        categorySpecificLatencies.register(categoryOne,
                defaultCategorySpecificReporterFactory.adapterFor(this, categoryOne));
        assertTrue(categorySpecificLatencies.hasCategorySpecificReporters());
        categorySpecificLatencies.startAdapters();
        categorySpecificLatencies.reportLatencyFor(1000, categoryOne);
        categorySpecificLatencies.reportFailure(categoryOne, new IFail());
        categorySpecificLatencies.stop();
        categorySpecificLatencies.summaryTime();
    }

    class IFail extends Exception {//
    }

    enum Category implements TestTaskCategory {
        One
    }

    @Override
    public TestRuntimeReporter newReporter(final LatencyProvider latencyProvider, final int threads) {
        return new TestRuntimeReporter() {

            @Override
            public void throughput(final long currentDuration, final double throughput) {
                throw new RuntimeException("Don't come here!");
            }

            @Override
            public void summary(final String id, final long elapsedTime, final long sampleCount, final long startTime) {
                assertEquals("name-One-statistics", id);
                assertEquals(1, sampleCount);
            }

            @Override
            public void latency(final int latency) {
                assertEquals(1000, latency);
            }

            @Override
            public void invocationFailed(final Throwable t) {
                assertTrue(t.getClass().equals(IFail.class));
            }

            @Override
            public boolean includeInvocationGraph() {
                return true;
            }
        };
    }
}
