package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class QueryExpression implements Expression {

    private final SourceClause sourceClause;
    private final WhereClause whereClause;

    private QueryExpression(SourceClause sourceClause, WhereClause whereClause) {
        this.sourceClause = Objects.requireNonNull(sourceClause);
        this.whereClause = Objects.requireNonNull(whereClause);
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
