package org.fluentjava.perftence.fluent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DefaultRunNotifierTest {

    @Test
    public void isFinished() {
        DefaultRunNotifier notifier = new DefaultRunNotifier();
        final String id = "id";
        assertFalse(notifier.isFinished(id));
        notifier.finished(id);
        assertTrue(notifier.isFinished(id));
    }
}
