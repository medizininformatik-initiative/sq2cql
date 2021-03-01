package com.codex.sq2cql.data_model.common;

import java.util.HashMap;

public enum Comparator {
    EQUAL,
    UNEQUAL,
    LESS_EQUAL, // ≤
    LESS_THAN, // <
    GREATER_EQUAL, // ≥
    GREATER_THAN; // >

    static private final HashMap<Comparator, String> comparatorStringMap = new HashMap<>(){{
        put(EQUAL, "=");
        put(UNEQUAL, "!=");
        put(LESS_EQUAL, "<=");
        put(LESS_THAN, "<");
        put(GREATER_EQUAL, ">=");
        put(GREATER_THAN, "<=");
    }};

    public String toString()
    {
        return comparatorStringMap.get(this);
    }
}
