package org.fluentjava.perftence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AllowedExceptionsTest {

    @Test
    public void test() {
        AllowedExceptions ae = new AllowedExceptions();
        MyException exception = new MyException();
        assertFalse(ae.isAllowed(exception));
        ae.allow(MyException.class);
        assertTrue(ae.isAllowed(exception));
    }

    final static class MyException extends Exception {
        public MyException() {
            super();
        }
    }
}
