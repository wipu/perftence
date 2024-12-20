package org.fluentjava.perftence.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.fluentjava.perftence.junit.AbstractMultiThreadedTest;
import org.fluentjava.perftence.junit.DefaultTestRunner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(DefaultTestRunner.class)
public class StatisticsPerformanceTest extends AbstractMultiThreadedTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsPerformanceTest.class);

    @Test
    public void performance1Million() {
        start();
        measure(measure(sampleList(1000000)));
        done();
    }

    @Test
    public void performance5Million() {
        start();
        measure(measure(sampleList(5000000)));
        done();
    }

    @Disabled
    @Test
    public void performance7Point5Million() {
        start();
        measure(measure(sampleList(7500000)));
        done();
    }

    @Disabled
    @Test
    public void performance10Million() {
        start();
        measure(measure(sampleList(10000000)));
        done();
    }

    @Disabled
    @Test
    public void performance15Million() {
        start();
        measure(measure(sampleList(15000000)));
        done();
    }

    @Disabled
    @Test
    public void performance20Million() {
        start();
        measure(measure(sampleList(20000000)));
        done();
    }

    private static void measure(final Statistics stats) {
        measureMean(stats);
        measureStandardDeviation(stats);
        measureVariance(stats);
    }

    private static void measureVariance(Statistics stats) {
        start("statistics.variance");
        stats.variance();
        done("statistics.variance");
    }

    private static void measureStandardDeviation(final Statistics stats) {
        start("statistics.standarddeviation");
        stats.standardDeviation();
        done("statistics.standarddeviation");
    }

    private static void measureMean(final Statistics stats) {
        start("statistics.mean");
        stats.mean();
        done("statistics.mean");
    }

    private static Statistics measure(final List<Integer> sampleList) {
        final long start = System.nanoTime();
        Statistics fromLatencies = Statistics.fromLatencies(sampleList);
        final long stop = System.nanoTime();
        log().info("Finished performance test for {} items in {} ms", sampleList.size(), ((stop - start) / 1000000.00));
        return fromLatencies;
    }

    private static List<Integer> sampleList(int size) {
        log().debug("Warming up the sample list...");
        final List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(RANDOM.nextInt(1000));
        }
        log().debug("Warmed up the sample list.");
        return list;
    }

    private void done() {
        done(fullyQualifiedMethodNameWithClassName());
    }

    private static void done(String fullyQualifiedMethodNameWithClassName) {
        log().info("Done: {}", fullyQualifiedMethodNameWithClassName);
    }

    private void start() {
        start(fullyQualifiedMethodNameWithClassName());
    }

    private static void start(String fullyQualifiedMethodNameWithClassName) {
        log().info("Start: {}", fullyQualifiedMethodNameWithClassName);
    }

    private static Logger log() {
        return LOGGER;
    }

}
