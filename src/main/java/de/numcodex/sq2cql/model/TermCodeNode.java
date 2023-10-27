package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TermCodeNode(ContextualTermCode contextualTermCode, List<TermCodeNode> children) {

    public TermCodeNode {
        requireNonNull(contextualTermCode);
        children = List.copyOf(children);
    }

    public static TermCodeNode of(ContextualTermCode termCode) {
        return new TermCodeNode(termCode, List.of());
    }

    public static TermCodeNode of(ContextualTermCode termCode, TermCodeNode... children) {
        return new TermCodeNode(termCode, List.of(children));
    }

    @JsonCreator
    public static TermCodeNode of(@JsonProperty("context") TermCode context,
                                  @JsonProperty("termCode") TermCode termCode,
                                  @JsonProperty("children") TermCodeNode... children) {
        var contextualTermCode = ContextualTermCode.of(context,
                requireNonNull(termCode, "missing JSON property: termCode"));
        return new TermCodeNode(contextualTermCode,
                children == null ? List.of() : List.of(children));
    }

    public Stream<ContextualTermCode> expand(ContextualTermCode termCode) {
        if (requireNonNull(termCode).equals(this.contextualTermCode)) {
            return leafConcepts();
        } else if (children.isEmpty()) {
            return Stream.of();
        } else {
            return children.stream().flatMap(n -> n.expand(termCode));
        }
    }

    private Stream<ContextualTermCode> leafConcepts() {
        if (children.isEmpty()) {
            return Stream.of(contextualTermCode);
        } else {
            return Stream.concat(Stream.of(contextualTermCode),
                    children.stream().flatMap(TermCodeNode::leafConcepts));
        }
    }
}
