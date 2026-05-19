package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeSelectorTest {

    static final String CODE = "code-191141";
    static final String SYSTEM = "system-191157";

    @Test
    void print() {
        assertEquals("Code '" + CODE + "' from " + SYSTEM, CodeSelector.of(CODE, SYSTEM).print(PrintContext.ZERO));
    }
}
