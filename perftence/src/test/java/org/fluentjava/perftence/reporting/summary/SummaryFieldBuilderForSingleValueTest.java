package org.fluentjava.perftence.reporting.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.text.DecimalFormat;

import org.fluentjava.perftence.formatting.FieldFormatter;
import org.junit.jupiter.api.Test;

public class SummaryFieldBuilderForSingleValueTest {

    @Test
    public void noFieldDefinitionNoValue() {
        final SummaryFieldBuilderForSingleValue<Double> builder = new SummaryFieldBuilderForSingleValue<>(
                new FieldFormatter(), new FieldAdjuster());
        final SummaryField<Double> field = builder.build();
        assertNull(field.value());
        assertEquals("<no name>                ", field.name());
        SummaryField<String> formatted = builder.asFormatted();
        assertEquals("<value was null>", formatted.value());
        assertEquals("<no name>                ", formatted.name());
    }

    @Test
    public void noFieldDefinitionButHasAValue() {
        final String separatedValue = "10" + decimalSepator() + "01";
        final BuildableSummaryField<Double> buildable = new SummaryFieldBuilderForSingleValue<Double>(
                new FieldFormatter(), new FieldAdjuster()).value(10.01);
        final SummaryField<Double> field = buildable.build();
        assertNotNull(field.value());
        assertEquals("<no name>                ", field.name());
        SummaryField<String> formatted = buildable.asFormatted();
        assertEquals(separatedValue, formatted.value());
        assertEquals("<no name>                ", formatted.name());
    }

    private static char decimalSepator() {
        return new DecimalFormat("###.##").getDecimalFormatSymbols().getDecimalSeparator();
    }

    @Test
    public void hasFieldDefinitionAndAValue() {
        final String separatedValue = "10" + decimalSepator() + "01";
        final BuildableSummaryField<Double> buildable = new SummaryFieldBuilderForSingleValue<Double>(
                new FieldFormatter(), new FieldAdjuster()).field(new FieldDefinition() {
                    @Override
                    public String fullName() {
                        return "double field";
                    }
                }).value(10.01);
        final SummaryField<Double> field = buildable.build();
        assertNotNull(field.value());
        assertEquals("double field             ", field.name());
        SummaryField<String> formatted = buildable.asFormatted();
        assertEquals(separatedValue, formatted.value());
        assertEquals("double field             ", formatted.name());

    }
}
