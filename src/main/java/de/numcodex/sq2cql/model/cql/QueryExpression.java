package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public record QueryExpression(SourceClause sourceClause, WhereClause whereClause, ReturnClause returnClause) implements Expression {

    public QueryExpression {
        requireNonNull(sourceClause);
    }

    public static QueryExpression of(SourceClause sourceClause, WhereClause whereClause) {
        return new QueryExpression(sourceClause, whereClause, null);
    }

    public static QueryExpression of(SourceClause sourceClause, ReturnClause returnClause) {
        return new QueryExpression(sourceClause, null, returnClause);
    }

    @Override
    public String print(PrintContext printContext) {
        var sourcePrintContext = printContext.resetPrecedence();
        if (!Objects.isNull(whereClause)) {
            var wherePrintContext = sourcePrintContext.increase();
            return printContext.parenthesizeZero("%s\n%s%s".formatted(sourceClause.print(sourcePrintContext),
                wherePrintContext.getIndent(),
                whereClause.print(wherePrintContext)));
        }
        else if (!Objects.isNull(returnClause)) {
            var returnPrintContext = sourcePrintContext.increase();
            return printContext.parenthesizeZero("%s\n%s%s".formatted(sourceClause.print(sourcePrintContext),
                returnPrintContext.getIndent(),
                returnClause.print(returnPrintContext)));
        }
        else return "";
    }
}
