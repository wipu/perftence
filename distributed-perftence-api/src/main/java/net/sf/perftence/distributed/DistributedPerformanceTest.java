package net.sf.perftence.distributed;

import java.util.concurrent.ExecutorService;

import net.sf.perftence.LatencyProvider;
import net.sf.perftence.RunNotifier;
import net.sf.perftence.TestFailureNotifier;
import net.sf.perftence.common.DefaultTestRuntimeReporterFactory;
import net.sf.perftence.common.InvocationStorage;
import net.sf.perftence.common.TestRuntimeReporterFactory;
import net.sf.perftence.common.ThroughputStorage;
import net.sf.perftence.fluent.FluentPerformanceTest;
import net.sf.perftence.fluent.TestBuilder;
import net.sf.perftence.reporting.TestRuntimeReporter;
import net.sf.perftence.reporting.summary.FailedInvocations;
import net.sf.perftence.setup.PerformanceTestSetup;

public final class DistributedPerformanceTest {

    private final String id;
    private final TestFailureNotifier testFailureNotifier;
    private final ExecutorService executorService;
    private final RunNotifier runNotifier;
    private final DefaultTestRuntimeReporterFactory defaultReporterFactory;
    private final RemoteLatencyReporter remoteReporter;

    public DistributedPerformanceTest(final String id,
            final TestFailureNotifier testFailureNotifier,
            final ExecutorService executorService,
            final RunNotifier runNotifier,
            final RemoteLatencyReporter remoteReporter) {
        this.id = id;
        this.testFailureNotifier = testFailureNotifier;
        this.executorService = executorService;
        this.runNotifier = runNotifier;
        this.remoteReporter = remoteReporter;
        this.defaultReporterFactory = new DefaultTestRuntimeReporterFactory();
    }

    public TestBuilder setup(final PerformanceTestSetup setup) {
        final TestRuntimeReporterFactory reporterFactory = new TestRuntimeReporterFactory() {
            @Override
            public TestRuntimeReporter newRuntimeReporter(
                    LatencyProvider latencyProvider,
                    boolean includeInvocationGraph, PerformanceTestSetup setup,
                    FailedInvocations failedInvocations) {
                return new DistributedTestRuntimeReporter(
                        defaultReporterFactory().newRuntimeReporter(
                                latencyProvider, includeInvocationGraph, setup,
                                failedInvocations), executorService(),
                        remoteReporter());
            }

            @Override
            public TestRuntimeReporter newRuntimeReporter(
                    LatencyProvider latencyProvider,
                    boolean includeInvocationGraph, PerformanceTestSetup setup,
                    FailedInvocations failedInvocations,
                    InvocationStorage invocationStorage,
                    ThroughputStorage throughputStorage) {
                return defaultReporterFactory()
                        .newRuntimeReporter(latencyProvider,
                                includeInvocationGraph, setup,
                                failedInvocations, invocationStorage,
                                throughputStorage);
            }

        };
        return new FluentPerformanceTest(this.testFailureNotifier,
                reporterFactory, this.runNotifier).test(id()).setup(setup);
    }

    private ExecutorService executorService() {
        return this.executorService;
    }

    private String id() {
        return this.id;
    }

    private DefaultTestRuntimeReporterFactory defaultReporterFactory() {
        return this.defaultReporterFactory;
    }

    private RemoteLatencyReporter remoteReporter() {
        return this.remoteReporter;
    }
}