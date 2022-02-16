package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueSetAttributeFilterTest {

    private static final TermCode CODE = TermCode.of("foo", "bar", "baz");

    @Test
    void of_WithoutSelectedConcepts() {
        var error = assertThrows(IllegalArgumentException.class, () -> ValueSetAttributeFilter.of(CODE));

        assertEquals("empty selected concepts", error.getMessage());
    }
}
