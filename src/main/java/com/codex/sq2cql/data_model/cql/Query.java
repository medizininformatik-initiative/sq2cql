package com.codex.sq2cql.data_model.cql;

public class Query implements Expression {
    private final SourceClause sourceClause;
    private WhereClause whereClause;

    public Query(SourceClause sourceClause) {
        this.sourceClause = sourceClause;
    }

    public Query(SourceClause sourceClause, WhereClause whereClause)
    {
        this.sourceClause = sourceClause;
        this.whereClause = whereClause;
    }

    @Override
    public String toString() {
        return "%s\n%s".formatted(sourceClause.toString(), whereClause == null ? "" : whereClause.toString());
    }
}
