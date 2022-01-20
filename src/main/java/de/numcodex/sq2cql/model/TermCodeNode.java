package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TermCodeNode {

    private final TermCode termCode;
    private final List<TermCodeNode> children;

    private TermCodeNode(TermCode termCode, List<TermCodeNode> children) {
        this.termCode = termCode;
        this.children = children;
    }

    public static TermCodeNode of() {
        return new TermCodeNode(null, List.of());
    }

    @JsonCreator
    public static TermCodeNode of(@JsonProperty("termCode") TermCode termCode,
                                  @JsonProperty("children") TermCodeNode... children) {
        return new TermCodeNode(Objects.requireNonNull(termCode), children == null ? List.of() : List.of(children));
    }

    public TermCode getTermCode() {
        return termCode;
    }

    public List<TermCodeNode> getChildren() {
        return children;
    }

    public Stream<TermCode> expand(TermCode termCode) {
        if (Objects.requireNonNull(termCode).equals(this.termCode)) {
            return leafConcepts();
        } else if (children.isEmpty()) {
            return Stream.of();
        } else {
            return children.stream().flatMap(n -> n.expand(termCode));
        }
    }

    private Stream<TermCode> leafConcepts() {
        if (children.isEmpty()) {
            return Stream.of(termCode);
        } else {
            return Stream.concat(Stream.of(termCode), children.stream().flatMap(TermCodeNode::leafConcepts));
        }
    }
}
