package org.fluentjava.perftence.fluent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;

import org.fluentjava.perftence.StatisticsProvider;
import org.fluentjava.perftence.formatting.FieldFormatter;
import org.fluentjava.perftence.graph.GraphWriter;
import org.fluentjava.perftence.reporting.Duration;
import org.fluentjava.perftence.reporting.summary.FieldAdjuster;
import org.fluentjava.perftence.reporting.summary.SummaryAppender;
import org.fluentjava.perftence.reporting.summary.SummaryFieldFactory;
import org.fluentjava.perftence.setup.PerformanceTestSetup;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverallSummaryBuilderTest {
    private final static Logger LOG = LoggerFactory.getLogger(OverallSummaryBuilderTest.class);
    private final static DecimalFormat DF = new DecimalFormat("###.##");

    @Test
    public void overAllSummaryOfADurationBasedTest() {
        OverallSummaryBuilder builder = new OverallSummaryBuilder(durationBased(), statistics(),
                newSummaryFieldFactory(), new EstimatedInvocations());
        final String build = log(builder.build());
        assertNotNull("Summary was null!", build);
        assertTrue(build.contains("samples:                 286/286\n"));
        assertTrue(build.contains("max:                     998\n"));
        assertTrue(build.contains("average:                 " + DF.format(508.38) + "\n"));
        assertTrue(build.contains("median:                  488\n"));
        assertTrue(build.contains("95 percentile:           955\n"));
        assertTrue(build.contains("throughput:              " + DF.format(19.08) + "\n"));
        assertTrue(build.contains("execution time (ms):     15000\n"));
        assertTrue(build.contains("threads:                 10\n"));
        final String expected = "" + "samples:                 286/286\n" + "max:                     998\n"
                + "average:                 " + DF.format(508.38) + "\n" + "median:                  488\n"
                + "95 percentile:           955\n" + "throughput:              " + DF.format(19.08) + "\n"
                + "execution time (ms):     15000\n" + "threads:                 10\n";
        assertEquals(expected, build);
    }

    @Test
    public void overAllSummaryOfAThreadBasedTest() {
        OverallSummaryBuilder builder = new OverallSummaryBuilder(threadBased(), statistics(), newSummaryFieldFactory(),
                new EstimatedInvocations());
        final String build = log(builder.build());
        assertNotNull("Summary was null!", build);
        assertTrue(build.contains("samples:                 286/286\n"));
        assertTrue(build.contains("max:                     998\n"));
        assertTrue(build.contains("average:                 " + DF.format(508.38) + "\n"));
        assertTrue(build.contains("median:                  488\n"));
        assertTrue(build.contains("95 percentile:           955\n"));
        assertTrue(build.contains("throughput:              " + DF.format(19.08) + "\n"));
        assertTrue(build.contains("execution time (ms):     15000\n"));
        assertTrue(build.contains("threads:                 10\n"));
        final String expected = "" + "samples:                 286/286\n" + "max:                     998\n"
                + "average:                 " + DF.format(508.38) + "\n" + "median:                  488\n"
                + "95 percentile:           955\n" + "throughput:              " + DF.format(19.08) + "\n"
                + "execution time (ms):     15000\n" + "threads:                 10\n";
        assertEquals(expected, build);

    }

    private static String log(final String build) {
        log().info("{}{}", "\n", build);
        return build;
    }

    private static Logger log() {
        return LOG;
    }

    private static StatisticsProvider statistics() {
        return new StatisticsProvider() {

            @Override
            public long sampleCount() {
                return 286;
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
            public double throughput() {
                return 19.083234423432432;
            }

            @Override
            public long duration() {
                return 15000;
            }

            @Override
            public double averageLatency() {
                return 508.383234324324342;
            }
        };
    }

    private static PerformanceTestSetup durationBased() {
        return new PerformanceTestSetup() {

            @Override
            public int throughputRange() {
                return 0;
            }

            @Override
            public int threads() {
                return 10;
            }

            @Override
            public Collection<SummaryAppender> summaryAppenders() {
                return Collections.emptyList();
            }

            @Override
            public int invocations() {
                return 0;
            }

            @Override
            public int invocationRange() {
                return 0;
            }

            @Override
            public Collection<GraphWriter> graphWriters() {
                return Collections.emptyList();
            }

            @Override
            public int duration() {
                return Duration.seconds(15);
            }
        };
    }

    private static PerformanceTestSetup threadBased() {
        return new PerformanceTestSetup() {

            @Override
            public int throughputRange() {
                return 0;
            }

            @Override
            public int threads() {
                return 10;
            }

            @Override
            public Collection<SummaryAppender> summaryAppenders() {
                return Collections.emptyList();
            }

            @Override
            public int invocations() {
                return 286;
            }

            @Override
            public int invocationRange() {
                return 1000;
            }

            @Override
            public Collection<GraphWriter> graphWriters() {
                return Collections.emptyList();
            }

            @Override
            public int duration() {
                return 0;
            }
        };
    }

    private static SummaryFieldFactory newSummaryFieldFactory() {
        return SummaryFieldFactory.create(new FieldFormatter(), new FieldAdjuster());
    }
}
