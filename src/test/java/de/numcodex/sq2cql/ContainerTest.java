package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.cql.Container;
import de.numcodex.sq2cql.model.cql.DefaultExpression;
import org.junit.jupiter.api.Test;

import static de.numcodex.sq2cql.model.cql.Expression.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class ContainerTest {

    @Test
    void flatMap_EmptyContainer() {
        var container = Container.empty().flatMap(Container::of);

        assertEquals(Container.empty(), container);
    }

    @Test
    void flatMap_ToEmptyContainer() {
        var container = Container.of(TRUE).flatMap(expr -> Container.<DefaultExpression>empty());

        assertEquals(Container.empty(), container);
    }
}
