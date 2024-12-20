package org.fluentjava.perftence.fluent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.fluentjava.perftence.Executable;
import org.fluentjava.perftence.PerfTestFailure;
import org.fluentjava.perftence.TestFailureNotifier;
import org.fluentjava.perftence.common.HtmlTestReport;
import org.fluentjava.perftence.common.TestRuntimeReporterFactory;
import org.fluentjava.perftence.graph.jfreechart.DefaultDatasetAdapterFactory;
import org.fluentjava.perftence.graph.jfreechart.TestRuntimeReporterFactoryUsingJFreeChart;
import org.fluentjava.perftence.reporting.Duration;
import org.fluentjava.perftence.reporting.summary.SummaryConsumer;
import org.fluentjava.perftence.reporting.summary.SummaryToCsv.CsvSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class FluentPerformanceTestTest {

    private TestInfo info;
    private boolean testFailed;
    private Throwable testFailure;

    @BeforeEach
    public void before(TestInfo info) {
        this.info = info;
        this.testFailed = false;
        this.testFailure = null;
    }

    @Test
    public void sanityCheck() {
        final FluentPerformanceTest fluentPerformanceTest = new FluentPerformanceTest(new FailIHaveNotifier(),
                newDefaultTestRuntimeReporter(), new DefaultRunNotifier(), new DefaultDatasetAdapterFactory(),
                new org.fluentjava.perftence.reporting.summary.SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                });
        final MultithreadWorker test = fluentPerformanceTest.test(id())
                .setup(fluentPerformanceTest.setup().threads(100).duration(Duration.seconds(3)).build())
                .executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        Thread.sleep(10);
                    }
                });
        assertNotNull(test);
        test.start();
        assertFalse(this.testFailed);
        assertNull(this.testFailure);
    }

    @Test
    public void requirementFailed() {
        final AtomicInteger i = new AtomicInteger();
        final FluentPerformanceTest fluent = fluent();
        assertThrows(PerfTestFailure.class, () -> {
            fluent.test(id()).setup(fluent.setup().threads(1).invocations(2).build())
                    .requirements(fluent.requirements().max(200).build()).executable(new Executable() {
                        @Override
                        public void execute() throws Exception {
                            i.incrementAndGet();
                            // 201 will fail constantly in windows
                            Thread.sleep(i.intValue() == 1 ? 100 : 202);
                        }
                    }).start();
        });
    }

    @Test
    public void percentile95RequirementSucceeds() {
        final FluentPerformanceTest fluent = fluent();
        fluent.test(id()).setup(fluent.setup().threads(1).invocations(5).build())
                .requirements(fluent.requirements().percentile95(100).build()).executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        Thread.sleep(99);
                    }
                }).start();
        assertFalse(this.testFailed);
        assertNull(this.testFailure);
    }

    @Test
    public void percentile95RequirementFails() {
        final FluentPerformanceTest fluent = fluent();
        assertThrows(PerfTestFailure.class, () -> {
            fluent.test(id()).setup(fluent.setup().threads(1).invocations(5).build())
                    .requirements(fluent.requirements().percentile95(50).build()).executable(new Executable() {
                        @Override
                        public void execute() throws Exception {
                            Thread.sleep(100);
                        }
                    }).start();
        });
    }

    @Test
    public void percentile95RequirementAndMaxSucceeds() {
        final FluentPerformanceTest fluent = fluent();
        fluent.test(id()).setup(fluent.setup().threads(1).invocations(5).build())
                .requirements(fluent.requirements().percentile95(102).max(102).build()).executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        Thread.sleep(99);
                    }
                }).start();
        assertFalse(this.testFailed);
        assertNull(this.testFailure);
    }

    @Test
    public void allowedException() {
        final AtomicInteger i = new AtomicInteger();
        final FluentPerformanceTest fluent = fluent();
        fluent.test(id()).noInvocationGraph().setup(fluent.setup().threads(1).invocations(5).build())
                .allow(FailIHave.class).executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        i.incrementAndGet();
                        Thread.sleep(100);
                        if (i.intValue() == 4) {
                            throw new FailIHave();
                        }
                    }
                }).start();
        assertFalse(this.testFailed);
        assertNull(this.testFailure);
    }

    @Test
    public void allowedExceptionDuringDurationBasedTest() {
        final AtomicInteger i = new AtomicInteger();
        final FluentPerformanceTest fluent = fluent();
        fluent.test(id()).noInvocationGraph().setup(fluent.setup().threads(1).duration(Duration.seconds(5)).build())
                .allow(FailIHave.class).executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        i.incrementAndGet();
                        Thread.sleep(100);
                        if (i.intValue() == 4) {
                            throw new FailIHave();
                        }
                    }
                }).start();
        assertFalse(this.testFailed);
        assertNull(this.testFailure);
    }

    @Test
    public void noTestSetup() {
        assertThrows(RuntimeException.class, () -> {
            fluent().test(id()).executable(new Executable() {

                @Override
                public void execute() throws Exception {
                    Thread.sleep(100);
                }
            }).start();
        });
    }

    @Test
    public void noSetup() {
        final FluentPerformanceTest fluent = fluent();
        assertThrows(RuntimeException.class, () -> {
            fluent.test(id()).setup(fluent.setup().noSetup()).executable(new Executable() {
                @Override
                public void execute() throws Exception {
                    Thread.sleep(100);
                }
            }).start();
        });
    }

    @Test
    public void invalidTestSetup() {
        final FluentPerformanceTest fluent = fluent();
        assertThrows(PerfTestFailure.class, () -> {
            fluent.test(id()).setup(fluent.setup().build()).executable(new Executable() {

                @Override
                public void execute() throws Exception {
                    Thread.sleep(100);
                }
            }).start();
        });
    }

    @Test
    public void failingUnexpectedlyInDurationBasedTest() {
        final AtomicInteger i = new AtomicInteger();
        final FluentPerformanceTest fluent = new FluentPerformanceTest(new FailIHaveNotifier(),
                newDefaultTestRuntimeReporter(), new DefaultRunNotifier(), new DefaultDatasetAdapterFactory(),
                new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                });
        fluent.test(id()).noInvocationGraph().setup(fluent.setup().threads(1).duration(Duration.seconds(5)).build())
                .executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        i.incrementAndGet();
                        Thread.sleep(100);
                        if (i.intValue() == 4) {
                            throw new FailIHave();
                        }
                    }
                }).start();
        assertTrue(this.testFailed);
        assertNotNull(this.testFailure);
    }

    @Test
    public void errorOccurredInDurationBasedTest() {
        final AtomicInteger i = new AtomicInteger();
        FluentPerformanceTest fluent = new FluentPerformanceTest(new ErrorFailureNotifier(),
                newDefaultTestRuntimeReporter(), new DefaultRunNotifier(), new DefaultDatasetAdapterFactory(),
                new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                });
        fluent.test(id()).noInvocationGraph().setup(fluent.setup().threads(1).duration(Duration.seconds(5)).build())
                .executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        i.incrementAndGet();
                        Thread.sleep(100);
                        if (i.intValue() == 4) {
                            throw new ErrorOccurred();
                        }
                    }
                }).start();
        assertTrue(this.testFailed);
        assertNotNull(this.testFailure);
    }

    private static TestRuntimeReporterFactory newDefaultTestRuntimeReporter() {
        return TestRuntimeReporterFactoryUsingJFreeChart.reporterFactory(HtmlTestReport.withDefaultReportPath());
    }

    @Test
    public void errorOccurredInInvocationBasedTest() {
        final AtomicInteger i = new AtomicInteger();
        FluentPerformanceTest fluent = new FluentPerformanceTest(new ErrorFailureNotifier(),
                newDefaultTestRuntimeReporter(), new DefaultRunNotifier(), new DefaultDatasetAdapterFactory(),
                new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                });
        fluent.test(id()).noInvocationGraph().setup(fluent.setup().threads(1).invocations(5).build())
                .executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        i.incrementAndGet();
                        Thread.sleep(100);
                        if (i.intValue() == 4) {
                            throw new ErrorOccurred();
                        }
                    }
                }).start();
        assertTrue(this.testFailed);
        assertNotNull(this.testFailure);
    }

    @Test
    public void invocationsNotSpreadEvenlyBetweenThreads() {
        final AtomicInteger i = new AtomicInteger();
        FluentPerformanceTest fluent = new FluentPerformanceTest(new ErrorFailureNotifier(),
                newDefaultTestRuntimeReporter(), new DefaultRunNotifier(), new DefaultDatasetAdapterFactory(),
                new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                });
        fluent.test(id()).noInvocationGraph().setup(fluent.setup().threads(3).invocations(10).build())
                .executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        i.incrementAndGet();
                        Thread.sleep(100);
                    }
                }).start();
        assertFalse(this.testFailed);
        assertNull(this.testFailure);
    }

    @SuppressWarnings({ "unused" })
    @Test
    public void nullNotifier() {
        assertThrows(TestFailureNotifier.NoTestNotifierException.class, () -> {
            new FluentPerformanceTest(null, newDefaultTestRuntimeReporter(), new DefaultRunNotifier(),
                    new DefaultDatasetAdapterFactory(), new SummaryConsumer() {

                        @Override
                        public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                            // no impl
                        }

                        @Override
                        public void consumeSummary(String summaryId, String summary) {
                            // no impl
                        }

                    });
        });
    }

    @Test
    public void startable() throws Exception {
        final AtomicBoolean executed = new AtomicBoolean(false);
        final Random random = new Random();
        final FluentPerformanceTest fluent = fluent();
        final MultithreadWorker durationWorker = fluent.test(id() + ".1")
                .setup(fluent.setup().threads(10).duration(Duration.seconds(2)).build()).noInvocationGraph()
                .executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        executed.set(true);
                        Thread.sleep(random.nextInt(10) + 1);
                    }
                });
        final TestBuilder startable = fluent.test("root").noInvocationGraph().startable(durationWorker);
        assertFalse(startable.includeInvocationGraph());
        assertFalse(durationWorker.includeInvocationGraph());
        startable.start();
        assertTrue(executed.get());
    }

    @Test
    public void whenTestLastsMoreThanExpectedRunnerInterruptsTheThreads() {
        final AtomicInteger i = new AtomicInteger();
        final FluentPerformanceTest fluent = new FluentPerformanceTest(new TestFailureNotifier() {

            @Override
            public void testFailed(final Throwable t) {
                FluentPerformanceTestTest.this.testFailed = true;
                FluentPerformanceTestTest.this.testFailure = t;
            }
        }, newDefaultTestRuntimeReporter(), new DefaultRunNotifier(), new DefaultDatasetAdapterFactory(),
                new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                });
        fluent.test(id()).noInvocationGraph().setup(fluent.setup().threads(6).duration(Duration.seconds(2)).build())
                .executable(new Executable() {
                    @Override
                    public void execute() throws Exception {
                        Thread.sleep(i.get() == 6 ? 8000 : 600);
                        i.incrementAndGet();
                    }
                }).start();
        assertTrue(this.testFailed);
        assertNotNull(this.testFailure);
        assertEquals(InterruptedException.class, this.testFailure.getClass());
    }

    private FluentPerformanceTest fluent() {
        return new FluentPerformanceTest(new PerfTestFailedNotifier(), newDefaultTestRuntimeReporter(),
                new DefaultRunNotifier(), new DefaultDatasetAdapterFactory(), new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                });
    }

    private class PerfTestFailedNotifier implements TestFailureNotifier {

        @Override
        public void testFailed(final Throwable t) {
            FluentPerformanceTestTest.this.testFailed = true;
            FluentPerformanceTestTest.this.testFailure = t;
            assertTrue(t.getClass().equals(PerfTestFailure.class));
        }
    }

    private class FailIHaveNotifier implements TestFailureNotifier {
        @Override
        public void testFailed(final Throwable t) {
            FluentPerformanceTestTest.this.testFailed = true;
            FluentPerformanceTestTest.this.testFailure = t;
            assertTrue(t.getClass().equals(FailIHave.class));
        }
    }

    private static class FailIHave extends Exception {//
    }

    private class ErrorFailureNotifier implements TestFailureNotifier {

        @Override
        public void testFailed(final Throwable t) {
            FluentPerformanceTestTest.this.testFailed = true;
            FluentPerformanceTestTest.this.testFailure = t;
            assertTrue(t.getClass().equals(ErrorOccurred.class));

        }
    }

    private static class ErrorOccurred extends Error {//
    }

    private String id() {
        return this.info.getDisplayName();
    }

}
