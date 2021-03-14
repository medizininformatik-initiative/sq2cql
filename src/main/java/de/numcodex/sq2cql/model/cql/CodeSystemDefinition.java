package de.numcodex.sq2cql.model.cql;

import java.util.Objects;

/**
 * A definition of a code system identifier.
 * <p>
 * Instances are immutable and implement {@code equals} and {@code hashCode} based on {@link #getName()} name}.
 *
 * @author Alexander Kiel
 */
public final class CodeSystemDefinition {

    private final String name;
    private final String system;

    private CodeSystemDefinition(String name, String system) {
        this.name = Objects.requireNonNull(name);
        this.system = Objects.requireNonNull(system);
    }

    public static CodeSystemDefinition of(String name, String system) {
        return new CodeSystemDefinition(name, system);
    }

    public String getName() {
        return name;
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
