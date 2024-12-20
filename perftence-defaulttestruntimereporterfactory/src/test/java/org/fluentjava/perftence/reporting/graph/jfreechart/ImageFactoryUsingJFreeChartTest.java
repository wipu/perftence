package org.fluentjava.perftence.reporting.graph.jfreechart;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.fluentjava.perftence.DefaultLatencyProviderFactory;
import org.fluentjava.perftence.LatencyProvider;
import org.fluentjava.perftence.common.DefaultInvocationStorage;
import org.fluentjava.perftence.common.FrequencyStorageFactory;
import org.fluentjava.perftence.common.HtmlTestReport;
import org.fluentjava.perftence.common.InvocationStorage;
import org.fluentjava.perftence.common.ReportingOptionsFactory;
import org.fluentjava.perftence.common.Statistics;
import org.fluentjava.perftence.graph.DatasetAdapter;
import org.fluentjava.perftence.graph.ImageData;
import org.fluentjava.perftence.graph.jfreechart.DefaultDatasetAdapterFactory;
import org.fluentjava.perftence.graph.jfreechart.ImageFactoryUsingJFreeChart;
import org.fluentjava.perftence.graph.jfreechart.JFreeChartWriter;
import org.fluentjava.perftence.graph.jfreechart.ScatterPlotGraphData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageFactoryUsingJFreeChartTest {
    private final static Logger LOG = LoggerFactory.getLogger(ImageFactoryUsingJFreeChartTest.class);
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static ImageFactoryUsingJFreeChart imageFactory;

    @BeforeAll
    public static void beforeClass() {
        imageFactory = new ImageFactoryUsingJFreeChart(
                new JFreeChartWriter(HtmlTestReport.withDefaultReportPath().reportRootDirectory()));
    }

    @BeforeEach
    public void before(TestInfo info) {
        log().info("Start {}", info.getDisplayName());
    }

    @AfterEach
    public void after(TestInfo info) {
        log().info("Done {}", info.getDisplayName());
    }

    private static InvocationStorage newDefaultInvocationStorage(final int value) {
        log().info("Warming up invocation storage...");
        final InvocationStorage storage = DefaultInvocationStorage.newDefaultStorage(value,
                ReportingOptionsFactory.latencyOptionsWithStatistics(120), new DefaultDatasetAdapterFactory());
        for (int i = 0; i < value; i++) {
            storage.store(randomValue());
        }
        log().info("Warmed up invocation storage...");
        return storage;
    }

    private static int randomValue() {
        return RANDOM.nextInt(100) + 1;
    }

    private static int storageSize() {
        return 100000;
    }

    @Test
    public void createImageWithStatistics() throws Exception {
        ImageData imageData = newDefaultInvocationStorage(storageSize()).imageData();
        imageFactory().createXYLineChart(id("with-statistics"), imageData);
    }

    @Disabled
    @Test
    public void hugeDataSet() {
        ImageData imageData = newDefaultInvocationStorage(20000000).imageData();
        imageFactory().createXYLineChart(id("huge-with-statistics"), imageData);
    }

    @Test
    public void createImageWithoutStatistics() throws Exception {
        ImageData imageDataWithoutStatistics = imageDataWithoutStatistics();
        imageFactory().createXYLineChart(id("without-statistics"), imageDataWithoutStatistics);
    }

    @Test
    public void createBarChart() {
        ImageData imageDataWithoutStatistics = imageDataWithSmallAmountOfData();
        imageFactory().createBarChart(id("barchart-without-statistics"), imageDataWithoutStatistics);
    }

    @Test
    public void createScatterPlot() {
        ImageData imageDataWithoutStatistics = imageDataForScatterPlot();
        imageFactory().createScatterPlot(id("scatterplot-without-statistics"), imageDataWithoutStatistics);
    }

    private static ImageData imageDataForScatterPlot() {
        final ImageData data = ImageData.noStatistics("Scatter Plot Title", "xTitle",
                scatterPlotAdapter("legendTitle", "yAxisTitle"));
        Random r = new Random();
        for (int i = 0; i <= 100; i++) {
            double x = r.nextDouble();
            double y = r.nextDouble();
            data.add(x, y);
        }
        return data;
    }

    private static DatasetAdapter<ScatterPlotGraphData, Paint> scatterPlotAdapter(final String legendTitle,
            final String yAxisTitle) {
        return new DefaultDatasetAdapterFactory().forScatterPlot(legendTitle, yAxisTitle);
    }

    private static ImageData imageDataWithSmallAmountOfData() {
        log().info("Warming up latency counter...");
        final LatencyProvider latencyProvider = newLatencyProvider();
        latencyProvider.start();
        for (int i = 0; i < 100; i++) {
            latencyProvider.addSample(200);
        }
        for (int i = 0; i < 10; i++) {
            latencyProvider.addSample(204);
        }
        for (int i = 0; i < 3; i++) {
            latencyProvider.addSample(403);
        }
        for (int i = 0; i < 5; i++) {
            latencyProvider.addSample(500);
        }
        latencyProvider.stop();

        return warmedUp(newStorage(toString(latencyProvider)).imageData());
    }

    private static InvocationStorage newStorage(final LatencyProvider latencyProvider) {
        return new InvocationStorage() {

            @Override
            public void store(int latency) {
                latencyProvider.addSample(latency);
            }

            @Override
            public Statistics statistics() {
                return Statistics.fromLatencies(new ArrayList<>());
            }

            @Override
            public boolean reportedLatencyBeingBelowOne() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return !latencyProvider.hasSamples();
            }

            @Override
            public ImageData imageData() {
                final ImageData data = ImageData.noStatistics("title", "xAxisTitle",
                        new DefaultDatasetAdapterFactory().forBarChart("legendTitle"));
                final Collection<Long> samples = latencyProvider.uniqueSamples();
                for (final Long sample : samples) {
                    data.add(latencyProvider.latencyCount(sample), sample);
                }
                data.range(latencyProvider.sampleCount());
                return data;
            }
        };
    }

    private static ImageData imageDataWithoutStatistics() {
        log().info("Warming up latency counter...");
        return warmedUp(FrequencyStorageFactory.newFrequencyStorage(
                toString(newCounterWithRandomContent(storageSize())), new DefaultDatasetAdapterFactory()).imageData());
    }

    private static Logger log() {
        return LOG;
    }

    private static ImageData warmedUp(final ImageData imageData) {
        log().info("Warmed up...");
        return imageData;
    }

    private static LatencyProvider toString(final LatencyProvider latencyProvider) {
        log().info("newProvider = {}", latencyProvider.toString());
        return latencyProvider;
    }

    private static LatencyProvider newCounterWithRandomContent(final int size) {
        final LatencyProvider latencyProvider = newLatencyProvider();
        latencyProvider.start();
        for (int i = 0; i < size; i++) {
            final int n = RANDOM.nextInt(1000) + 1;
            final int latency = RANDOM.nextInt(n) + 1;
            latencyProvider.addSample(latency);
        }
        latencyProvider.stop();
        return latencyProvider;
    }

    private static ImageFactoryUsingJFreeChart imageFactory() {
        return imageFactory;
    }

    private static String id(final String id) {
        return ImageFactoryUsingJFreeChartTest.class.getName() + "-" + id;
    }

    private static LatencyProvider newLatencyProvider() {
        return new DefaultLatencyProviderFactory().newInstance();
    }
}
