package com.codex.sq2cql.data_model.common;

import java.util.HashMap;

public enum LogicalOperator {
    AND,
    OR,
    XOR,
    NOT;

    static private final HashMap<LogicalOperator, String> comparatorStringMap = new HashMap<>(){{
        put(AND, "and");
        put(OR, "or");
        put(XOR, "xor");
        put(NOT, "not");
    }};

    public String toString()
    {
        return comparatorStringMap.get(this);
    }
}
