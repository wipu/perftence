package org.fluentjava.perftence.common;

import java.util.ArrayList;
import java.util.List;

import org.fluentjava.perftence.graph.ImageData;
import org.fluentjava.perftence.graph.LineChartAdapterProvider;
import org.fluentjava.perftence.reporting.ReportingOptions;

public final class DefaultInvocationStorage implements InvocationStorage {

    private final List<Integer> totalInvocations;
    private boolean reportedLatencyBeingBelowOne = false;
    private final ReportingOptions reportingOptions;
    private final ImageDataFactory imageDataFactory;

    private DefaultInvocationStorage(final int totalInvocations, final ReportingOptions reportingOptions,
            final ImageDataFactory imageDataFactory) {
        this.reportingOptions = reportingOptions;
        this.totalInvocations = initialize(totalInvocations);
        this.imageDataFactory = imageDataFactory;
    }

    public static InvocationStorage newDefaultStorage(final int totalInvocations,
            final ReportingOptions reportingOptions, final LineChartAdapterProvider<?, ?> lineChartAdapterProvider) {
        return new DefaultInvocationStorage(totalInvocations, reportingOptions,
                new ImageDataFactory(lineChartAdapterProvider));
    }

    private static List<Integer> initialize(final int invocations) {
        return invocations > 0 ? new ArrayList<>(invocations) : new ArrayList<>();
    }

    @Override
    public void store(final int latency) {
        reportLatencyBeingBelowOne(latency);
        addLatency(latency);
    }

    private boolean addLatency(final int latency) {
        return invocations().add(latency == 0 ? 1 : latency);
    }

    private void reportLatencyBeingBelowOne(final int latency) {
        if (latency == 0 && !reportedLatencyBeingBelowOne()) {
            markReportLatenciesBeingBelowOne();
        }
    }

    private synchronized void markReportLatenciesBeingBelowOne() {
        this.reportedLatencyBeingBelowOne = true;
    }

    @Override
    public synchronized boolean reportedLatencyBeingBelowOne() {
        return this.reportedLatencyBeingBelowOne;
    }

    @Override
    public Statistics statistics() {
        return Statistics.fromLatencies(invocations());
    }

    private List<Integer> invocations() {
        return this.totalInvocations;
    }

    @Override
    public boolean isEmpty() {
        return invocations().isEmpty();
    }

    @Override
    public ImageData imageData() {
        return imageData(invocations());
    }

    private ImageData imageData(final List<Integer> invocations) {
        final ImageData imageData = imageDataFactory().newImageDataForLineChart(reportingOptions(), statistics());
        int i = 0;
        for (final Integer latency : invocations) {
            imageData.add(i, latency);
            i++;
        }
        return imageData;
    }

    private ImageDataFactory imageDataFactory() {
        return this.imageDataFactory;
    }

    private ReportingOptions reportingOptions() {
        return this.reportingOptions;
    }

    public static InvocationStorage invocationStorageWithNoSamples(
            final LineChartAdapterProvider<?, ?> lineChartAdapterProvider) {
        return new InvocationStorage() {
            @Override
            public void store(final int latency) {
                // left empty intentionally
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
                return true;
            }

            @Override
            public ImageData imageData() {
                return ImageData.statistics("no samples", "X-axis title", 100, statistics(),
                        lineChartAdapterProvider.forLineChart("legend title"));
            }
        };
    }
}
