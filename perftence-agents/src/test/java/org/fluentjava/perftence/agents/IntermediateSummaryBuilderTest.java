package org.fluentjava.perftence.agents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DecimalFormat;

import org.fluentjava.perftence.RuntimeStatisticsProvider;
import org.fluentjava.perftence.TestFailureNotifier;
import org.fluentjava.perftence.formatting.FieldFormatter;
import org.fluentjava.perftence.reporting.summary.CustomIntermediateSummaryProvider;
import org.fluentjava.perftence.reporting.summary.FieldAdjuster;
import org.fluentjava.perftence.reporting.summary.IntermediateSummary;
import org.fluentjava.perftence.reporting.summary.SummaryFieldFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntermediateSummaryBuilderTest implements TestFailureNotifier {
    private final static Logger LOG = LoggerFactory.getLogger(IntermediateSummaryBuilderTest.class);
    private final static DecimalFormat DF = new DecimalFormat("###.##");

    @Test
    public void buildSummary() {
        final IntermediateSummaryBuilder builder = new IntermediateSummaryBuilder(statistics(), activeThreads(100),
                scheduledTasks(), failureNotifier(false), newSummaryFieldFactory());
        final String build = log(builder.build());
        assertNotNull("Summary was null!", build);
        assertTrue(build.contains("finished tasks:          248\n"));
        assertTrue(build.contains("scheduled tasks:         1000\n"));
        assertTrue(build.contains("failed tasks:            0\n"));
        assertTrue(build.contains("threads running tasks:   100\n"));
        assertTrue(build.contains("max:                     998\n"));
        assertTrue(build.contains("average:                 " + DF.format(508.38) + "\n"));
        assertTrue(build.contains("median:                  488\n"));
        assertTrue(build.contains("95 percentile:           955\n"));
        assertTrue(build.contains("throughput:              " + DF.format(19.08) + "\n"));
        assertTrue(build.contains("execution time (ms):     13000\n"));
        assertTrue(build.contains("last task to be run:     in 2000 (ms)\n"));
        final String expected = "" + "finished tasks:          248\n" + "scheduled tasks:         1000\n"
                + "failed tasks:            0\n" + "threads running tasks:   100\n" + "max:                     998\n"
                + "average:                 " + DF.format(508.38) + "\n" + "median:                  488\n"
                + "95 percentile:           955\n" + "throughput:              " + DF.format(19.08) + "\n"
                + "execution time (ms):     13000\n" + "last task to be run:     in 2000 (ms)\n";
        assertEquals(expected, build);
    }

    @Test
    public void buildSummaryWithFailures() {
        final IntermediateSummaryBuilder builder = new IntermediateSummaryBuilder(statistics(), activeThreads(100),
                scheduledTasks(), failureNotifier(true), newSummaryFieldFactory());
        final String build = log(builder.build());
        assertNotNull("Summary was null!", build);
        assertTrue(build.contains("finished tasks:          248\n"));
        assertTrue(build.contains("scheduled tasks:         1000\n"));
        assertTrue(build.contains("failed tasks:            1\n"));
        assertTrue(build.contains("threads running tasks:   100\n"));
        assertTrue(build.contains("max:                     998\n"));
        assertTrue(build.contains("average:                 " + DF.format(508.38) + "\n"));
        assertTrue(build.contains("median:                  488\n"));
        assertTrue(build.contains("95 percentile:           955\n"));
        assertTrue(build.contains("throughput:              " + DF.format(19.08) + "\n"));
        assertTrue(build.contains("execution time (ms):     13000\n"));
        assertTrue(build.contains("last task to be run:     in 2000 (ms)\n"));

        final String expected = "" + "finished tasks:          248\n" + "scheduled tasks:         1000\n"
                + "failed tasks:            1\n" + "threads running tasks:   100\n" + "max:                     998\n"
                + "average:                 " + DF.format(508.38) + "\n" + "median:                  488\n"
                + "95 percentile:           955\n" + "throughput:              " + DF.format(19.08) + "\n"
                + "execution time (ms):     13000\n" + "last task to be run:     in 2000 (ms)\n";
        assertEquals(expected, build);
    }

    @Test
    public void customProvider() {
        final IntermediateSummaryBuilder builder = new IntermediateSummaryBuilder(statistics(), activeThreads(100),
                scheduledTasks(), failureNotifier(true), newSummaryFieldFactory());
        final CustomIntermediateSummaryProvider customSummaryProvider = new CustomIntermediateSummaryProvider() {
            @Override
            public void provideIntermediateSummary(final IntermediateSummary summary) {
                summary.endOfLine().text("Custom summary follows here").endOfLine();
            }
        };
        builder.customSummaryProviders(customSummaryProvider);
        final String build = log(builder.build());
        assertNotNull("Summary was null!", build);
        assertTrue(build.contains("finished tasks:          248\n"));
        assertTrue(build.contains("scheduled tasks:         1000\n"));
        assertTrue(build.contains("failed tasks:            1\n"));
        assertTrue(build.contains("threads running tasks:   100\n"));
        assertTrue(build.contains("max:                     998\n"));
        assertTrue(build.contains("average:                 " + DF.format(508.38) + "\n"));
        assertTrue(build.contains("median:                  488\n"));
        assertTrue(build.contains("95 percentile:           955\n"));
        assertTrue(build.contains("throughput:              " + DF.format(19.08) + "\n"));
        assertTrue(build.contains("execution time (ms):     13000\n"));
        assertTrue(build.contains("last task to be run:     in 2000 (ms)\n"));

        final String expected = "" + "finished tasks:          248\n" + "scheduled tasks:         1000\n"
                + "failed tasks:            1\n" + "threads running tasks:   100\n" + "max:                     998\n"
                + "average:                 " + DF.format(508.38) + "\n" + "median:                  488\n"
                + "95 percentile:           955\n" + "throughput:              " + DF.format(19.08) + "\n"
                + "execution time (ms):     13000\n" + "last task to be run:     in 2000 (ms)\n"
                + "\nCustom summary follows here\n";
        assertEquals(expected, build);

    }

    private static String log(final String build) {
        log().info("{}{}", "\n", build);
        return build;
    }

    private static Logger log() {
        return LOG;
    }

    private TestFailureNotifierDecorator failureNotifier(final boolean failure) {
        final TestFailureNotifierDecorator failures = new TestFailureNotifierDecorator(this);
        if (failure) {
            failures.testFailed(new NullPointerException());
        }
        return failures;
    }

    private static TaskSchedulingStatisticsProvider scheduledTasks() {
        return new TaskSchedulingStatisticsProvider() {
            @Override
            public int scheduledTasks() {
                return 1000;
            }

            @Override
            public Time lastTaskScheduledToBeRun() {
                return TimeSpecificationFactory.inMillis(2000);
            }
        };
    }

    private static ActiveThreads activeThreads(final int active) {
        final ActiveThreads activeThreads = new ActiveThreads();
        for (int i = 0; i < active; i++) {
            activeThreads.more();
        }
        return activeThreads;
    }

    private static RuntimeStatisticsProvider statistics() {
        return new RuntimeStatisticsProvider() {

            @Override
            public long sampleCount() {
                return 248;
            }

            @Override
            public long percentileLatency(int percentile) {
                return 955;
            }

            @Override
            public long minLatency() {
                return 0;
            }

            @Override
            public long median() {
                return 488;
            }

            @Override
            public long maxLatency() {
                return 998;
            }

            @Override
            public boolean hasSamples() {
                return true;
            }

            @Override
            public double currentThroughput() {
                return 19.08;
            }

            @Override
            public long currentDuration() {
                return 13000;
            }

            @Override
            public double averageLatency() {
                return 508.38;
            }
        };
    }

    @Override
    public void testFailed(final Throwable t) {
        log().info("Test failure reported: {}", t.getClass().getName());
    }

    private static SummaryFieldFactoryForAgentBasedTests newSummaryFieldFactory() {
        return new SummaryFieldFactoryForAgentBasedTests(
                SummaryFieldFactory.create(new FieldFormatter(), new FieldAdjuster()));
    }
}
