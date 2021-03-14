package de.numcodex.sq2cql.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A terminology code coding a concept.
 * <p>
 * Instances are immutable and implement {@code equals} and {@code hashCode} based on {@link #getSystem() system} and
 * {@link #getCode() code}.
 */
public final class TermCode {

    private final String system;
    private final String code;
    private final String display;

    private TermCode(String system, String code, String display) {
        this.system = Objects.requireNonNull(system);
        this.code = Objects.requireNonNull(code);
        this.display = Objects.requireNonNull(display);
    }

    /**
     * Returns a terminology code.
     *
     * @param system  the terminology to use (mostly represented by an URL)
     * @param code    the code within the terminology
     * @param display a human readable string of the concept coded
     * @return the terminology code
     */
    @JsonCreator
    public static TermCode of(@JsonProperty("system") String system, @JsonProperty("code") String code,
                              @JsonProperty("display") String display) {
        return new TermCode(system, code, display);
    }

    public String getSystem() {
        return system;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermCode termCode = (TermCode) o;
        return system.equals(termCode.system) && code.equals(termCode.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(system, code);
    }

    @Override
    public String toString() {
        return "TermCode{" +
                "system='" + system + '\'' +
                ", code='" + code + '\'' +
                ", display='" + display + '\'' +
                '}';
    }
}
