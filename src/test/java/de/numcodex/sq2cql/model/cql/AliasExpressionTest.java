package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AliasExpressionTest {

    private static final String IDENTIFIER = "identifier-190631";

    @Test
    void constructor() {
        assertThrows(NullPointerException.class, () -> new AliasExpression(null));
    }

    @Test
    void print() {
        assertEquals(IDENTIFIER, AliasExpression.of(IDENTIFIER).print(PrintContext.ZERO));
    }
}
