package de.numcodex.sq2cql.model.cql;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AndExpressionTest {

    @Test
    void createWithTrueFirst() {
        var expr = ExistsExpression.of(QuantityExpression.of(BigDecimal.ONE));

        var result = AndExpression.of(BooleanExpression.TRUE, expr);

        assertThat(result).isEqualTo(expr);
    }

    @Test
    void createWithTrueSecond() {
        var expr = ExistsExpression.of(QuantityExpression.of(BigDecimal.ONE));

        var result = AndExpression.of(expr, BooleanExpression.TRUE);

        assertThat(result).isEqualTo(expr);
    }
}
