package de.numcodex.sq2cql.model.structured_query;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
abstract class AbstractModifier implements Modifier {

    final String path;

    AbstractModifier(String path) {
        this.path = requireNonNull(path);
    }
}
