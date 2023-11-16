package de.numcodex.sq2cql.model.cql;

import java.util.Map;

public interface DefaultExpression extends Expression<DefaultExpression> {

    @Override
    default DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return this;
    }

    default DefaultExpression and(DefaultExpression expr) {
        return AndExpression.of(this, expr);
    }
}
