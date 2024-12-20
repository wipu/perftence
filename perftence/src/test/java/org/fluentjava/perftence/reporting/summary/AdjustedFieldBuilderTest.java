package org.fluentjava.perftence.reporting.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fluentjava.perftence.formatting.FieldFormatter;
import org.junit.jupiter.api.Test;

public class AdjustedFieldBuilderTest {

    @Test
    public void formattedLongField() {
        AdjustedFieldBuilder builder = newBuilder();
        final Long value = 100L;
        AdjustedField<String> asFormatted = builder.field("long field", value).asFormatted();
        assertEquals("100", asFormatted.value());
        assertEquals("long field               ", asFormatted.name());
    }

    @Test
    public void formattedIntField() {
        AdjustedFieldBuilder builder = newBuilder();
        AdjustedField<String> asFormatted = builder.field("int field", 100).asFormatted();
        assertEquals("100", asFormatted.value());
        assertEquals("int field                ", asFormatted.name());
    }

    @Test
    public void formattedAsFormatted() {
        AdjustedFieldBuilder builder = newBuilder();
        AdjustedField<String> formatted = builder.field("int field", 100).asFormatted();
        assertEquals("100", formatted.value());
        assertEquals("int field                ", formatted.name());
        AdjustedField<String> asFormatted = formatted.asFormatted();
        assertEquals("100", asFormatted.value());
        assertEquals("int field                ", asFormatted.name());
    }

    private static AdjustedFieldBuilder newBuilder() {
        return new AdjustedFieldBuilder(new FieldFormatter(), new FieldAdjuster());
    }
}
