package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryExpressionTest {

    @Nested
    class Print {

        private final SourceClause sourceClause = SourceClause.of(AliasedQuerySource.of(
                RetrieveExpression.of("Observation"), IdentifierExpression.of("O")));

        @Test
        void sourceOnly() {
            var expr = QueryExpression.of(sourceClause);

            var s = expr.print(PrintContext.ZERO);

            assertEquals("[Observation]", s);
        }

        @Test
        void withWhereClause() {
            var expr = QueryExpression.of(sourceClause, WhereClause.of(BooleanExpression.FALSE));

            var s = expr.print(PrintContext.ZERO);

            assertEquals("""
                    from [Observation] O
                      where false""", s);
        }

        @Test
        void withReturnClause() {
            var expr = QueryExpression.of(sourceClause, ReturnClause.of(BooleanExpression.TRUE));

            var s = expr.print(PrintContext.ZERO);

            assertEquals("""
                    from [Observation] O
                      return true""", s);
        }

        @Test
        void withWithClause() {
            var withSource = AliasedQuerySource.of(RetrieveExpression.of("Condition"), IdentifierExpression.of("C"));
            var withClause = WithClause.of(withSource, BooleanExpression.TRUE);
            var expr = QueryExpression.of(sourceClause, WhereClause.of(BooleanExpression.FALSE)).appendQueryInclusionClause(withClause);

            var s = expr.print(PrintContext.ZERO);

            assertEquals("""
                    from [Observation] O
                      with [Condition] C
                        such that true
                      where false""", s);
        }

        @Test
        void insideExists() {
            var expr = ExistsExpression.of(QueryExpression.of(sourceClause, WhereClause.of(BooleanExpression.FALSE)));

            var s = expr.print(PrintContext.ZERO);

            assertEquals("""
                    exists (from [Observation] O
                      where false)""", s);
        }
    }
}
