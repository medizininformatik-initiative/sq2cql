package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionDefinitionTest {

    @Test
    void print_query() {
        var sourceClause = SourceClause.of(AliasedQuerySource.of(RetrieveExpression.of("Observation"), IdentifierExpression.of("O")));
        var expr = ExpressionDefinition.of("foo-bar", QueryExpression.of(sourceClause, WhereClause.of(BooleanExpression.TRUE)));

        var s = expr.print(PrintContext.ZERO);

        assertEquals("""
                define "foo-bar":
                  [Observation]""", s);
    }
}
