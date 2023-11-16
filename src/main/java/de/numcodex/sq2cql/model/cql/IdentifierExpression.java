package de.numcodex.sq2cql.model.cql;

import java.util.regex.Pattern;

public interface IdentifierExpression extends Expression<IdentifierExpression> {

    Pattern SAFE_CHARS_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    @Override
    default boolean isIdentifier() {
        return true;
    }
}
