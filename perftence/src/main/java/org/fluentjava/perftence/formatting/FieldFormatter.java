package org.fluentjava.perftence.formatting;

import java.text.DecimalFormat;

public final class FieldFormatter {
    private final static DecimalFormat DF = new DecimalFormat("#####.##");

    @SuppressWarnings("static-method")
    public String format(final Object value) {
        return DF.format(value);
    }

}
