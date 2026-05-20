package de.numcodex.sq2cql.model.cql;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A definition of a code system identifier.
 * <p>
 * Instances are immutable and implement {@code equals} and {@code hashCode} based on {@link #name()} name}.
 *
 * @author Alexander Kiel
 */
public record CodeSystemDefinition(String name, String system) {

    public CodeSystemDefinition {
        requireNonNull(name);
        requireNonNull(system);
    }

    public static CodeSystemDefinition of(String name, String system) {
        return new CodeSystemDefinition(name, system);
    }

    public String print() {
        return "codesystem %s: '%s'".formatted(name, system);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeSystemDefinition that = (CodeSystemDefinition) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
