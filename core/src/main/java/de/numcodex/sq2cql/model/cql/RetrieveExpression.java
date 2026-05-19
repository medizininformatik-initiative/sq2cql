package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public sealed interface RetrieveExpression extends Expression<RetrieveExpression> {

    static RetrieveExpression of(String resourceType) {
        return new Unfiltered(resourceType);
    }

    static RetrieveExpression of(String resourceType, Expression<?> terminology) {
        return new WithPrimaryCodeFilter(resourceType, terminology);
    }

    static RetrieveExpression of(String resourceType, Expression<?> terminology, String searchPath) {
        return new WithSearchCodeFilter(resourceType, terminology, searchPath);
    }

    default IdentifierExpression alias() {
        return StandardIdentifierExpression.of(resourceType().substring(0, 1));
    }

    String resourceType();

    @Override
    RetrieveExpression withIncrementedSuffixes(Map<String, Integer> increments);

    record Unfiltered(String resourceType) implements RetrieveExpression{

        public Unfiltered {
            requireNonNull(resourceType);
        }

        @Override
        public Unfiltered withIncrementedSuffixes(Map<String, Integer> increments) {
            return this;
        }

        @Override
        public String print(PrintContext printContext) {
            return "[%s]".formatted(resourceType);
        }
    }

    record WithPrimaryCodeFilter(String resourceType, Expression<?> terminology) implements RetrieveExpression {

        public WithPrimaryCodeFilter {
            requireNonNull(resourceType);
            requireNonNull(terminology);
        }

        @Override
        public WithPrimaryCodeFilter withIncrementedSuffixes(Map<String, Integer> increments) {
            return new WithPrimaryCodeFilter(resourceType, terminology.withIncrementedSuffixes(increments));
        }

        @Override
        public String print(PrintContext printContext) {
            return "[%s: %s]".formatted(resourceType, terminology.print(printContext.resetPrecedence()));
        }
    }

    record WithSearchCodeFilter(String resourceType, Expression<?> terminology, String searchPath) implements RetrieveExpression {

        public WithSearchCodeFilter {
            requireNonNull(resourceType);
            requireNonNull(terminology);
            requireNonNull(searchPath);
        }

        @Override
        public WithSearchCodeFilter withIncrementedSuffixes(Map<String, Integer> increments) {
            return new WithSearchCodeFilter(resourceType, terminology.withIncrementedSuffixes(increments), searchPath);
        }

        @Override
        public String print(PrintContext printContext) {
            return "[%s: %s ~ %s]".formatted(resourceType, searchPath, terminology.print(printContext.resetPrecedence()));
        }
    }
}
