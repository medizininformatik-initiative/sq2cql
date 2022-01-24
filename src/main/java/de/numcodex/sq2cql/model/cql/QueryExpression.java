package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record QueryExpression(SourceClause sourceClause, WhereClause whereClause) implements Expression {

    public QueryExpression {
        requireNonNull(sourceClause);
        requireNonNull(whereClause);
    }

    public static QueryExpression of(SourceClause sourceClause, WhereClause whereClause) {
        return new QueryExpression(sourceClause, whereClause);
    }

    @Override
    public String print(PrintContext printContext) {
        var wherePrintContext = printContext.increase();
        return "%s\n%s%s".formatted(sourceClause.toCql(printContext.resetPrecedence()), wherePrintContext.getIndent(),
                whereClause.toCql(wherePrintContext));
    }
}
