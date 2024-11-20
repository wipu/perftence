package org.fluentjava.perftence.agents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TimespecificationFactoryTest {

    @Test
    public void inNanos() {
        final Time inNanos = TimeSpecificationFactory.inNanos(100);
        assertEquals(100, inNanos.time());
        assertEquals(TimeUnit.NANOSECONDS, inNanos.timeUnit());
    }

    @Test
    public void toNanos() {
        assertEquals(100000000, TimeSpecificationFactory.toNanos(TimeSpecificationFactory.inMillis(100)));
    }

}
