package org.fluentjava.perftence.agents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.fluentjava.perftence.DefaultLatencyProviderFactory;
import org.fluentjava.perftence.TestFailureNotifier;
import org.fluentjava.perftence.common.HtmlTestReport;
import org.fluentjava.perftence.common.TestRuntimeReporterFactory;
import org.fluentjava.perftence.graph.jfreechart.DefaultDatasetAdapterFactory;
import org.fluentjava.perftence.graph.jfreechart.TestRuntimeReporterFactoryUsingJFreeChart;
import org.fluentjava.perftence.reporting.summary.SummaryConsumer;
import org.fluentjava.perftence.reporting.summary.SummaryToCsv.CsvSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AgentBasedTestTest {
    private final static Logger LOG = LoggerFactory.getLogger(AgentBasedTestTest.class);
    private boolean testFailed = false;
    private Throwable testFailure;
    private AtomicInteger taskRun = new AtomicInteger();

    @BeforeEach
    public void before() {
        taskRun().set(0);
    }

    @Test
    public void sanityCheck() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        final TestBuilder test = new AgentBasedTest(new MyFailureTestNotifier(), new DefaultLatencyProviderFactory(),
                testRuntimeReporterFactory(), adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }
                }).test("sanityCheck");
        assertNotNull(test);
        assertEquals("sanityCheck", test.id());
        test.agents(failingAgent()).start();
        assertTrue(this.testFailed);
        assertTrue(this.testFailure.getClass().equals(MyFailure.class));
        assertEquals(1, taskRun().get());
    }

    @Test
    public void noInvocationGraph() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        final TestBuilder test = new AgentBasedTest(new MyFailureTestNotifier(), new DefaultLatencyProviderFactory(),
                testRuntimeReporterFactory(), adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("noInvocationGraph").noInvocationGraph();
        assertNotNull(test);
        assertEquals("noInvocationGraph", test.id());
        test.agents(failingAgent()).start();
        assertTrue(this.testFailed);
        assertTrue(this.testFailure.getClass().equals(MyFailure.class));
        assertEquals(1, taskRun().get());
    }

    @Test
    public void oneAgentOneTask() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        new AgentBasedTest(new TestingNotifier(), new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(),
                adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("oneAgentOneTask").agents(agentsWithOneTask(1)).latencyGraphFor(Category.One).start();
        assertFalse(this.testFailed);
        assertEquals(1, taskRun().get());
    }

    @Test
    public void oneAgentOneTaskWithSummary() {
        final AtomicBoolean summaryConsumed = new AtomicBoolean(false);
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        new AgentBasedTest(new TestingNotifier(), new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(),
                adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        summaryConsumed.set(true);
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("oneAgentOneTask").agents(agentsWithOneTask(1)).latencyGraphFor(Category.One).start();
        assertFalse(this.testFailed);
        assertEquals(1, taskRun().get());
        assertTrue(summaryConsumed.get());
    }

    @Test
    public void manyAgentsOneTask() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        new AgentBasedTest(new TestingNotifier(), new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(),
                adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("manyAgentsOneTask").agents(agentsWithOneTask(5)).latencyGraphFor(Category.One).start();
        assertFalse(this.testFailed);
        assertEquals(5, taskRun().get());
    }

    @Test
    public void oneAgentManyTasks() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        new AgentBasedTest(new TestingNotifier(), new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(),
                adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("oneAgentManyTasks").agents(agentsWithManyTasks(1)).latencyGraphFor(Category.One).start();
        assertFalse(this.testFailed);
        assertEquals(2, taskRun().get());
    }

    @Test
    public void manyAgentsManyTasks() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        new AgentBasedTest(new TestingNotifier(), new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(),
                adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("manyAgentsManyTasks").agents(agentsWithManyTasks(2)).latencyGraphFor(Category.One).start();
        assertFalse(this.testFailed);
        assertEquals(4, taskRun().get());
    }

    @Test
    public void manyAgentsManyTasksLatencyGraphForCategoryOne() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        new AgentBasedTest(new TestingNotifier(), new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(),
                adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("manyAgentsManyTasks").agents(agentsWithManyTasks(2)).latencyGraphFor(Category.One).start();
        assertFalse(this.testFailed);
        assertEquals(4, taskRun().get());
    }

    @Test
    public void manyAgentsManyTasksLatencyGraphForAll() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        new AgentBasedTest(new TestingNotifier(), new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(),
                adapterProvider, adapterProvider, new SummaryConsumer() {

                    @Override
                    public void consumeSummary(String summaryId, CsvSummary convertToCsv) {
                        // no impl
                    }

                    @Override
                    public void consumeSummary(String summaryId, String summary) {
                        // no impl
                    }

                }).test("manyAgentsManyTasks").agents(agentsWithManyTasks(2)).latencyGraphForAll().start();
        assertFalse(this.testFailed);
        assertEquals(4, taskRun().get());
    }

    @Test
    @SuppressWarnings({ "static-method", "unused" })
    public void nullNotifier() {
        final DefaultDatasetAdapterFactory adapterProvider = new DefaultDatasetAdapterFactory();
        assertThrows(TestFailureNotifier.NoTestNotifierException.class, () -> {
            new AgentBasedTest(null, new DefaultLatencyProviderFactory(), testRuntimeReporterFactory(), adapterProvider,
                    adapterProvider, new SummaryConsumer() {

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

    private static TestRuntimeReporterFactory testRuntimeReporterFactory() {
        return TestRuntimeReporterFactoryUsingJFreeChart.reporterFactory(HtmlTestReport.withDefaultReportPath());
    }

    private Collection<TestAgent> agentsWithManyTasks(final int agentCount) {
        final List<TestAgent> agents = new ArrayList<>();
        for (int i = 0; i < agentCount; i++) {
            agents.add(new AgentWithManyTasks());
        }
        return agents;
    }

    class AgentWithManyTasks implements TestAgent {

        @Override
        public TestTask firstTask() {
            return newTestTask(TimeSpecificationFactory.now(), 100, Category.One,
                    newTestTask(TimeSpecificationFactory.inMillis(100), 50, Category.Two, null));
        }
    }

    private TestTask newTestTask(final Time scheduled, final int sleep, final TestTaskCategory category,
            final TestTask next) {
        return new TestTask() {

            @Override
            public Time when() {
                return scheduled;
            }

            @Override
            public void run(final TestTaskReporter reporter) throws Exception {
                Thread.sleep(sleep);
                LOG.info("Time spent so far = " + reporter.timeSpentSoFar());
                taskRun().incrementAndGet();

            }

            @Override
            public TestTask nextTaskIfAny() {
                return next;
            }

            @Override
            public TestTaskCategory category() {
                return category;
            }
        };
    }

    private Collection<TestAgent> agentsWithOneTask(final int agentCount) {
        final List<TestAgent> agents = new ArrayList<>();
        for (int i = 0; i < agentCount; i++) {
            agents.add(new AgentWithOneTask());
        }
        return agents;
    }

    class AgentWithOneTask implements TestAgent {

        @Override
        public TestTask firstTask() {
            return new TestTask() {

                @Override
                public Time when() {
                    return TimeSpecificationFactory.someMillisecondsFromNow(1500);
                }

                @Override
                public void run(TestTaskReporter reporter) throws Exception {
                    Thread.sleep(100);
                    LOG.info("Time spent so far = " + reporter.timeSpentSoFar());
                    taskRun().incrementAndGet();
                }

                @Override
                public TestTask nextTaskIfAny() {
                    return null;
                }

                @Override
                public TestTaskCategory category() {
                    return Category.One;
                }
            };
        }
    }

    class TestingNotifier implements TestFailureNotifier {

        @Override
        public void testFailed(Throwable t) {
            testFailureOf(t);
        }

    }

    private void testFailureOf(final Throwable t) {
        this.testFailed = true;
        this.testFailure = t;
    }

    private Collection<TestAgent> failingAgent() {
        final List<TestAgent> agents = new ArrayList<>();
        agents.add(new FailingAgent());
        return agents;
    }

    private class FailingAgent implements TestAgent {

        @Override
        public TestTask firstTask() {
            return new TestTask() {

                @Override
                public Time when() {
                    return TimeSpecificationFactory.now();
                }

                @Override
                public void run(final TestTaskReporter reporter) throws Exception {
                    Thread.sleep(50);
                    log().info("Time spent so far = " + reporter.timeSpentSoFar());
                    taskRun().incrementAndGet();
                }

                @Override
                public TestTask nextTaskIfAny() {
                    return new TestTask() {

                        @Override
                        public Time when() {
                            return TimeSpecificationFactory.now();
                        }

                        @Override
                        public void run(TestTaskReporter reporter) throws Exception {
                            throw new MyFailure("I fail!");
                        }

                        @Override
                        public TestTask nextTaskIfAny() {
                            return null;
                        }

                        @Override
                        public TestTaskCategory category() {
                            return Category.Fail;
                        }
                    };
                }

                @Override
                public TestTaskCategory category() {
                    return Category.One;
                }
            };
        }
    }

    private static class MyFailure extends Exception {

        public MyFailure(final String message) {
            super(message);
        }
    }

    private enum Category implements TestTaskCategory {
        One, Fail, Two
    }

    private class MyFailureTestNotifier implements TestFailureNotifier {
        @Override
        public void testFailed(final Throwable t) {
            testFailureOf(t);
            assertTrue(t.getClass().equals(MyFailure.class));
        }
    }

    private AtomicInteger taskRun() {
        return this.taskRun;
    }

    private static Logger log() {
        return LOG;
    }
}
