package com.codex.sq2cql.data_model.cql;

import java.util.HashMap;

public enum AccessModifier {
    PRIVATE,
    PUBLIC;

    static private final HashMap<AccessModifier, String> comparatorStringMap = new HashMap<>() {{
        put(PRIVATE, "private");
        put(PUBLIC, "public");
    }};

    public String toString()
    {
        return comparatorStringMap.get(this);
    }
}
