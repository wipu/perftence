package org.fluentjava.perftence;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LatencyProviderTest {

    @Test
    public void tooHastyNotEventStarted() {
        assertThrows(IllegalStateException.class, () -> {
            newLatencyProvider().throughput();
        });
    }

    @Test
    public void tooHastyStartedButNotStopped() {
        final LatencyProvider provider = newLatencyProvider();
        provider.start();
        assertThrows(IllegalStateException.class, () -> {
            provider.throughput();
        });
    }

    private static LatencyProvider newLatencyProvider() {
        return new DefaultLatencyProviderFactory().newInstance();
    }
}
