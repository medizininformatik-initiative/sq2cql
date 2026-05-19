package de.numcodex.sq2cql.model.common;

import org.junit.jupiter.api.Test;

import static de.numcodex.sq2cql.model.common.Comparator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Alexander Kiel
 */
class ComparatorTest {

    @Test
    void fromJson() {
        assertEquals(EQUAL, Comparator.fromJson("eq"));
        assertEquals(LESS_EQUAL, Comparator.fromJson("le"));
        assertEquals(LESS_THAN, Comparator.fromJson("lt"));
        assertEquals(GREATER_EQUAL, Comparator.fromJson("ge"));
        assertEquals(GREATER_THAN, Comparator.fromJson("gt"));
        assertThrows(IllegalArgumentException.class, () -> Comparator.fromJson("foo"));
    }
}
