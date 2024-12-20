package org.fluentjava.perftence.graph.jfreechart;

import java.io.File;

import org.fluentjava.perftence.PerftenceRuntimeException;
import org.fluentjava.perftence.graph.ChartWriter;
import org.fluentjava.volundr.fileio.FileUtil;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JFreeChartWriter implements ChartWriter<JFreeChart> {

    private final static Logger LOGGER = LoggerFactory.getLogger(JFreeChartWriter.class);

    private final String reportRootDirectory;

    public JFreeChartWriter(final String reportRootDirectory) {
        this.reportRootDirectory = reportRootDirectory;
    }

    @Override
    public void write(final String id, final JFreeChart chart, final int height, final int width) {
        final String outputFilePath = reportRootDirectory() + "/" + id + ".png";
        LOGGER.info("Writing chart as an image to file {}", outputFilePath);
        try {
            FileUtil.ensureDirectoryExists(newFile(reportRootDirectory()));
            ChartUtilities.saveChartAsPNG(newFile(outputFilePath), chart, width, height);
            LOGGER.info("Chart image successfully written to {}", outputFilePath);
        } catch (final Exception e) {
            throw new PerftenceRuntimeException(logError(outputFilePath, e), e);
        }
    }

    private static File newFile(final String path) {
        return new File(path);
    }

    private String reportRootDirectory() {
        return this.reportRootDirectory;
    }

    private static String logError(final String outputFilePath, final Throwable t) {
        final String errorMsg = "Writing file '" + outputFilePath + "' failed!";
        LOGGER.error(errorMsg, t);
        return errorMsg;
    }

}