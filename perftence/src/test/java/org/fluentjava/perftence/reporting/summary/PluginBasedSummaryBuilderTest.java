package org.fluentjava.perftence.reporting.summary;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.perftence.RuntimeStatisticsProvider;
import org.fluentjava.perftence.formatting.FieldFormatter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginBasedSummaryBuilderTest {
    private final static Logger LOG = LoggerFactory.getLogger(PluginBasedSummaryBuilderTest.class);

    @Test
    public void withRuntimeStatistics() {
        final RuntimeStatisticsProvider statistics = statistics();
        final SummaryFieldPlugin<Long> plugin = new ExecutionTimePlugin(summaryFieldFactory(),
                new FieldValueResolverAdapterForRuntimeStatistics(statistics).forExecutionTime());
        PluginBasedSummaryBuilder pluginBasedSummaryBuilder = new PluginBasedSummaryBuilder() {

            @Override
            public boolean hasSamples() {
                return statistics.hasSamples();
            }
        }.register(plugin);
        final String summary = log(pluginBasedSummaryBuilder.build());
        assertNotNull("Summary was null!", summary);
        assertTrue(summary.contains("execution time (ms):     4444\n"));
    }

    private static String log(final String summary) {
        log().info("{}{}", "\n", summary);
        return summary;
    }

    private static Logger log() {
        return LOG;
    }

    private static SummaryFieldFactory summaryFieldFactory() {
        return SummaryFieldFactory.create(new FieldFormatter(), new FieldAdjuster());
    }

    private static RuntimeStatisticsProvider statistics() {
        return new RuntimeStatisticsProvider() {

            @Override
            public long sampleCount() {
                throw shouldNotCallThis();
            }

            @Override
            public long percentileLatency(int percentile) {
                throw shouldNotCallThis();
            }

            @Override
            public long minLatency() {
                throw shouldNotCallThis();
            }

            @Override
            public long median() {
                throw shouldNotCallThis();
            }

            @Override
            public long maxLatency() {
                throw shouldNotCallThis();
            }

            @Override
            public boolean hasSamples() {
                throw shouldNotCallThis();
            }

            @Override
            public double currentThroughput() {
                throw shouldNotCallThis();
            }

            @Override
            public long currentDuration() {
                return 4444;
            }

            @Override
            public double averageLatency() {
                throw shouldNotCallThis();
            }
        };
    }

    private static RuntimeException shouldNotCallThis() {
        return new RuntimeException("should not call this!");
    }

}
