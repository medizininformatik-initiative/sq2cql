package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryExpressionTest {

    @Test
    void print_standalone() {
        var expr = query();

        var s = expr.print(PrintContext.ZERO);

        assertEquals("""
                from [Observation: Code '85354-9' from loinc] O
                  where true""", s);
    }

    @Test
    void print_inside_exists() {
        var expr = ExistsExpression.of(query());

        var s = expr.print(PrintContext.ZERO);

        assertEquals("""
                exists (from [Observation: Code '85354-9' from loinc] O
                  where true)""", s);
    }

    static QueryExpression query() {
        var retrieve = RetrieveExpression.of("Observation", CodeSelector.of("85354-9", "loinc"));
        var sourceClause = SourceClause.from(retrieve, IdentifierExpression.of("O"));
        return QueryExpression.of(sourceClause, WhereClause.of(BooleanExpression.TRUE));
    }
}
