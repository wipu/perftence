package org.fluentjava.perftence.agents;

import static org.junit.jupiter.api.Assertions.fail;

import org.fluentjava.perftence.TestFailureNotifier;
import org.junit.jupiter.api.Test;

public class SchedulingServiceBasedOnTaskProviderTest {

    @Test
    public void schedule() {
        newBasedOnTaskProvider(inMillis(100), new RunnableAdapter() {

            @Override
            public Runnable adapt(final TestTask task, long timeItWasScheduled) {
                return new Runnable() {

                    @Override
                    public void run() {
                        try {
                            task.run(null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        }, 10, new TestFailureNotifier() {
            @Override
            public void testFailed(Throwable t) {
                fail("Test failed : " + t.getMessage());
            }
        });
    }

    private static Time inMillis(long time) {
        return TimeSpecificationFactory.inMillis(time);
    }

    private static TestTaskSchedulingService newBasedOnTaskProvider(final Time workerWaitTime,
            final RunnableAdapter adapter, final int workers, TestFailureNotifier testFailureNotifier) {
        return new SchedulingServiceBasedOnTaskProvider(new TaskProvider(workerWaitTime), adapter, workers,
                testFailureNotifier);
    }

}
