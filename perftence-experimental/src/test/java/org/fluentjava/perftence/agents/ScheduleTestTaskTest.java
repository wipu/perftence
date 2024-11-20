package org.fluentjava.perftence.agents;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ScheduleTestTaskTest {

    @Test
    public void scheduleToFuture() throws InterruptedException {
        long now = nanoTime();
        final long scheduled = now;
        final ScheduledTestTask task = new ScheduledTestTask(task(inMillis(250)), scheduled);
        assertFalse(task.isTimeToRun(now));
        sleep(1);
        now = nanoTime();
        assertFalse(task.isTimeToRun(now));
        sleep(100);
        now = nanoTime();
        assertFalse(task.isTimeToRun(now));
        sleep(100);
        now = nanoTime();
        assertFalse(task.isTimeToRun(now));
        sleep(50);
        now = nanoTime();
        assertTrue(task.isTimeToRun(now));
    }

    @Test
    public void scheduleNow() {
        final long now = nanoTime();
        assertTrue(new ScheduledTestTask(task(inMillis(0)), now).isTimeToRun(now));
    }

    @Test
    public void scheduleToPast() {
        final long now = nanoTime();
        assertTrue(new ScheduledTestTask(task(inMillis(-100)), now).isTimeToRun(now));
    }

    private static TestTask task(final Time when) {
        return new TestTask() {

            @Override
            public Time when() {
                return when;
            }

            @Override
            public void run(final TestTaskReporter reporter) throws Exception {
                // no implemementation
            }

            @Override
            public TestTask nextTaskIfAny() {
                return null;
            }

            @Override
            public TestTaskCategory category() {
                return null;
            }
        };
    }

    private static Time inMillis(final long time) {
        return TimeSpecificationFactory.inMillis(time);
    }

    private static long nanoTime() {
        return System.nanoTime();
    }
}
