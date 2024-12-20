package org.fluentjava.perftence.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;

import org.fluentjava.perftence.LatencyProvider;
import org.fluentjava.perftence.PerftenceRuntimeException;
import org.fluentjava.perftence.common.InvocationStorage;
import org.fluentjava.perftence.common.ThroughputStorage;
import org.fluentjava.perftence.common.ThroughputStorageFactory;
import org.fluentjava.perftence.reporting.summary.FailedInvocations;
import org.fluentjava.perftence.reporting.summary.FailedInvocationsFactory;
import org.fluentjava.perftence.setup.PerformanceTestSetup;
import org.fluentjava.perftence.setup.PerformanceTestSetupPojo;
import org.fluentjava.volundr.LineReader;
import org.fluentjava.volundr.LineVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilebasedReportReader {

    private final static Logger LOG = LoggerFactory.getLogger(FilebasedReportReader.class);

    private final File reportDir;
    private final LatencyFileVisitor latencyVisitor;
    private final FailedInvocationsVisitor failedInvocationsVisitor;
    private final SummaryVisitor summaryVisitor;
    private final ThroughputStorageFactory throughputStorageFactory;

    private ThroughputStorage throughputStorage;
    private SetupReader setupReader;
    private FilebasedTestSetup setup;

    public FilebasedReportReader(final String id, final LatencyProvider latencyProvider,
            final InvocationStorage invocationStorage, final FailedInvocationsFactory failedInvocations,
            final ThroughputStorageFactory throughputStorageFactory, final File parentDirectory) {
        this.throughputStorageFactory = throughputStorageFactory;
        this.reportDir = new File(parentDirectory, id);
        this.latencyVisitor = new LatencyFileVisitor(latencyProvider, invocationStorage);
        this.failedInvocationsVisitor = new FailedInvocationsVisitor(failedInvocations.newInstance());
        this.summaryVisitor = new SummaryVisitor();
        this.setupReader = new SetupReader();
    }

    final class SetupReader {

        public FilebasedTestSetup read() {
            try {
                try (ObjectInputStream inputStream = new ObjectInputStream(
                        new FileInputStream(new File(reportDirectory(), "setup")))) {
                    return (FilebasedTestSetup) inputStream.readObject();
                }
            } catch (FileNotFoundException e) {
                throw new PerftenceRuntimeException(e);
            } catch (IOException e) {
                throw new PerftenceRuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new PerftenceRuntimeException(e);
            }

        }
    }

    final static class FilebasedReader {
        private final LineVisitor visitor;
        private final LineReader reader;
        private String file;
        private final File root;

        public FilebasedReader(final LineVisitor lineVisitor, final String file, final File root) {
            this.file = file;
            this.root = root;
            this.reader = new LineReader(Charset.defaultCharset());
            this.visitor = lineVisitor;
        }

        private LineReader reader() {
            return this.reader;
        }

        private LineVisitor lineVisitor() {
            return this.visitor;
        }

        public void read() throws FileNotFoundException, IOException {
            try (FileInputStream setupStream = new FileInputStream(new File(root(), file()))) {
                reader().read(setupStream, lineVisitor());
            }
        }

        private File root() {
            return this.root;
        }

        private String file() {
            return this.file;
        }
    }

    static class SummaryVisitor implements LineVisitor {

        private FilebasedSummary summary;

        @Override
        public void visit(final String line) {
            final String[] split = line.split(":");
            this.summary = new FilebasedSummary(toLong(split[1]), toLong(split[2]), toLong(split[3]));
        }

        @Override
        public void emptyLine() {
            log().warn("Ignored some empty lines from failed-invocations file");
        }

        public FilebasedSummary summary() {
            return this.summary;
        }
    }

    static class FailedInvocationsVisitor implements LineVisitor {

        private final FailedInvocations failedInvocations;

        public FailedInvocationsVisitor(final FailedInvocations failedInvocations) {
            this.failedInvocations = failedInvocations;
        }

        @Override
        public void visit(final String line) {
            this.failedInvocations.more(line);
        }

        @Override
        public void emptyLine() {
            log().warn("Ignored some empty lines from failed-invocations file");
        }

        public FailedInvocations failedInvocations() {
            return this.failedInvocations;
        }
    }

    static class ThroughputVisitor implements LineVisitor {

        private final ThroughputStorage throughputStorage;

        public ThroughputVisitor(final ThroughputStorage throughputStorage) {
            this.throughputStorage = throughputStorage;
        }

        @Override
        public void visit(final String line) {
            final String[] split = line.split(":");
            this.throughputStorage.store(toLong(split[0]), Double.parseDouble(split[1]));
        }

        @Override
        public void emptyLine() {
            log().warn("Ignored some empty lines from throughput file");
        }
    }

    static class SetupVisitor implements LineVisitor {

        private FilebasedTestSetup setup;

        @Override
        public void visit(final String line) {
            final String[] values = line.split(":");
            final PerformanceTestSetup testSetup = PerformanceTestSetupPojo.builder().duration(toInt(values[0]))
                    .threads(toInt(values[1])).invocations(toInt(values[2])).invocationRange(toInt(values[3]))
                    .throughputRange(toInt(values[4])).build();
            this.setup = new FilebasedTestSetup(testSetup, Boolean.parseBoolean(values[5]));
        }

        private static int toInt(final String value) {
            return Integer.parseInt(value);
        }

        @Override
        public void emptyLine() {
            log().warn("Ignored some empty lines from throughput file");
        }

        public FilebasedTestSetup setup() {
            return this.setup;
        }
    }

    static class LatencyFileVisitor implements LineVisitor {

        private final LatencyProvider latencyProvider;
        private final InvocationStorage invocationStorage;

        public LatencyFileVisitor(final LatencyProvider latencyProvider, final InvocationStorage invocationStorage) {
            this.latencyProvider = latencyProvider;
            this.invocationStorage = invocationStorage;
        }

        @Override
        public void visit(final String line) {
            final long latency = Long.parseLong(line);
            this.latencyProvider.addSample(latency);
            // FIXME:
            this.invocationStorage.store((int) latency);
        }

        @Override
        public void emptyLine() {
            log().warn("Ignored some empty lines from latency file");
        }
    }

    private static Logger log() {
        return LOG;
    }

    private File reportDirectory() {
        return this.reportDir;
    }

    public void read() {
        try {
            this.setup = setupReader().read();
            final FilebasedReader summaryReader = newFilebasedReader(summaryVisitor(), "summary");
            summaryReader.read();

            final FilebasedReader failedInvocations = newFilebasedReader(failedInvocationsVisitor(),
                    "failed-invocations");
            failedInvocations.read();

            final FilebasedReader latencies = newFilebasedReader(latencyVisitor(), "latencies");
            latencies.read();

            this.throughputStorage = throughputStorageFactory().forRange(setup().testSetup().throughputRange());
            final ThroughputVisitor throughputVisitor = new ThroughputVisitor(this.throughputStorage);
            final FilebasedReader throughput = newFilebasedReader(throughputVisitor, "throughput");
            throughput.read();
        } catch (FileNotFoundException e) {
            throw new PerftenceRuntimeException(e);
        } catch (IOException e) {
            throw new PerftenceRuntimeException(e);
        }
    }

    private ThroughputStorageFactory throughputStorageFactory() {
        return this.throughputStorageFactory;
    }

    private SetupReader setupReader() {
        return this.setupReader;
    }

    public ThroughputStorage throughputStorage() {
        return this.throughputStorage;
    }

    private FilebasedReader newFilebasedReader(final LineVisitor lineVisitor, final String name) {
        return new FilebasedReader(lineVisitor, name, reportDirectory());
    }

    private FailedInvocationsVisitor failedInvocationsVisitor() {
        return this.failedInvocationsVisitor;
    }

    private SummaryVisitor summaryVisitor() {
        return this.summaryVisitor;
    }

    private LatencyFileVisitor latencyVisitor() {
        return this.latencyVisitor;
    }

    public FilebasedTestSetup setup() {
        return this.setup;
    }

    public FailedInvocations failedInvocations() {
        return failedInvocationsVisitor().failedInvocations();
    }

    public FilebasedSummary summary() {
        return summaryVisitor().summary();
    }

    private static long toLong(final String value) {
        return Long.parseLong(value);
    }

}
