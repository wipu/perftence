package org.fluentjava.perftence.common;

import java.io.File;
import java.nio.charset.Charset;

import org.fluentjava.perftence.PerftenceRuntimeException;
import org.fluentjava.perftence.reporting.TestReport;
import org.fluentjava.volundr.fileio.AppendToFileFailed;
import org.fluentjava.volundr.fileio.FileAppendHandler;
import org.fluentjava.volundr.fileio.FileAppender;
import org.fluentjava.volundr.fileio.FileUtil;
import org.fluentjava.volundr.fileio.WritingFileFailed;
import org.fluentjava.volundr.io.StringToBytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HtmlTestReport implements TestReport {
    private final static Logger LOG = LoggerFactory.getLogger(HtmlTestReport.class);

    private final String directory;
    private final FileAppender fileAppender;
    private final StringToBytes toBytes;

    private HtmlTestReport(final String reportRootDirectory, final StringToBytes toBytes) {
        this.directory = reportRootDirectory;
        this.toBytes = toBytes;
        this.fileAppender = new FileAppender(this.toBytes, new FileAppendHandler() {
            @Override
            public void failed(final String file, final AppendToFileFailed e) {
                LOG.error("Appending data to summary file '" + file + "' failed", e);
                throw newRuntimeException("Appending data to summary file '" + file + "' failed", e);
            }

            @Override
            public void ok(final String file) {
                LOG.info("Data to summary file '{}' appended", file);
            }

            @Override
            public void start(final String file) {
                LOG.info("Appending data to summary file '{}' ...", file);
            }
        });
    }

    /**
     * Factory method for creating test report with default values
     */
    public static TestReport withDefaultReportPath() {
        // TODO MUCH better and explicit control over output files than this!!
        // But for now this implicit output directory is at least
        // 1) ensured to exist
        // 2) be somewhat isolated i.e. owned by the caller
        // 2) exist under /tmp, to not pollute other locations
        String caller = new Exception().getStackTrace()[1].getClassName();
        File outDir = new File("/tmp/.org.fluentjava.perftence/default-report-path/" + caller);
        outDir.mkdirs();
        return testReport(outDir.getAbsolutePath(), Charset.defaultCharset());
    }

    /**
     * Factory method for creating test report
     */
    public static TestReport testReport(final String reportRootDirectory, final Charset charset) {
        return new HtmlTestReport(reportRootDirectory, StringToBytes.forCharset(charset));
    }

    @Override
    public String reportRootDirectory() {
        return this.directory;
    }

    private static String nameFor(final String id) {
        return "perftence" + "-" + id + ".html";
    }

    private String indexFile() {
        return reportRootDirectory() + "/" + "index.html";
    }

    @Override
    public void updateIndexFile(final String id) {
        fileAppender().appendToFile(indexFile(), asHref(id));
    }

    private FileAppender fileAppender() {
        return this.fileAppender;
    }

    private static String asHref(final String id) {
        return "<a href=" + nameFor(id) + ">" + id + "</a><br/>";
    }

    @Override
    public void writeSummary(final String id, final String data) {
        final String path = reportRootDirectory() + "/" + nameFor(id);
        LOG.debug("Writing summary to: " + path);
        try {
            FileUtil.writeToFile(path, toBytes(data));
        } catch (final WritingFileFailed cause) {
            throw newRuntimeException("Writing summary to '" + path + "'failed!", cause);
        }
        LOG.debug("Summary successfully written to '" + path + "' ");
    }

    private byte[] toBytes(final String data) {
        return toBytes().convert(data);
    }

    private StringToBytes toBytes() {
        return this.toBytes;
    }

    private static PerftenceRuntimeException newRuntimeException(final String msg, final Throwable cause) {
        return new PerftenceRuntimeException(logError(msg, cause), cause);
    }

    private static String logError(final String msg, final Throwable t) {
        LOG.error(msg, t);
        return msg;
    }
}
