package de.medizininformatikinitiative.cctb;

import de.medizininformatikinitiative.cctb.model.cql.Container;
import de.medizininformatikinitiative.cctb.model.cql.DefaultExpression;
import org.junit.jupiter.api.Test;

import static de.medizininformatikinitiative.cctb.model.cql.Expression.TRUE;
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
