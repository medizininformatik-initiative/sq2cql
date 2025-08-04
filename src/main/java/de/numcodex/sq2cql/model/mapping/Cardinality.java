package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Cardinality {

    SINGLE, MANY;

    @JsonCreator
    public static Cardinality fromJson(String s) {
        return switch (s) {
            case "single" -> SINGLE;
            case "many" -> MANY;
            default -> throw new IllegalArgumentException("unknown JSON comparator: " + s);
        };
    }

    @JsonValue
    @Override
    public String toString() {
        return switch (this) {
            case SINGLE -> "single";
            case MANY -> "many";
        };
    }

}
