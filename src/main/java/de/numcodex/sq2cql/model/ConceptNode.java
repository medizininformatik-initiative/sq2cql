package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Alexander Kiel
 */
public class ConceptNode {

    private final TermCode concept;
    private final List<ConceptNode> children;

    private ConceptNode(TermCode concept, List<ConceptNode> children) {
        this.concept = concept;
        this.children = children;
    }

    public static ConceptNode of() {
        return new ConceptNode(null, null);
    }

    public static ConceptNode of(TermCode concept) {
        return new ConceptNode(Objects.requireNonNull(concept), null);
    }

    @JsonCreator
    public static ConceptNode of(@JsonProperty("termCode") TermCode concept,
                                 @JsonProperty("children") List<ConceptNode> children) {
        return new ConceptNode(Objects.requireNonNull(concept), children == null || children.isEmpty() ? null :
                List.copyOf(children));
    }

    public Stream<TermCode> expand(TermCode concept) {
        if (Objects.requireNonNull(concept).equals(this.concept)) {
            return leafConcepts();
        } else if (children == null) {
            return Stream.of();
        } else {
            return children.stream().flatMap(n -> n.expand(concept));
        }
    }

    private Stream<TermCode> leafConcepts() {
        if (children == null) {
            return Stream.of(concept);
        } else {
            return children.stream().flatMap(ConceptNode::leafConcepts);
        }
    }
}
