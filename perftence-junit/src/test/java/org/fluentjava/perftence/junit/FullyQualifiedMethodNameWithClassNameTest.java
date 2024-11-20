package org.fluentjava.perftence.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FullyQualifiedMethodNameWithClassNameTest {

    @Test
    public void idFor() {
        assertEquals("org.fluentjava.perftence.junit.FullyQualifiedMethodNameWithClassNameTest.id",
                new FullyQualifiedMethodNameWithClassName().idFor(this.getClass(), "id"));
    }
}
