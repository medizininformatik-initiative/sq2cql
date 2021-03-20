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
public class ConceptNode {

    private final TermCode concept;
    private final List<ConceptNode> children;

    private ConceptNode(TermCode concept, List<ConceptNode> children) {
        this.concept = concept;
        this.children = children;
    }

    public static ConceptNode of() {
        return new ConceptNode(null, List.of());
    }

    @JsonCreator
    public static ConceptNode of(@JsonProperty("termCode") TermCode concept,
                                 @JsonProperty("children") ConceptNode... children) {
        return new ConceptNode(Objects.requireNonNull(concept), children == null ? List.of() : List.of(children));
    }

    public TermCode getConcept() {
        return concept;
    }

    public List<ConceptNode> getChildren() {
        return children;
    }

    public Stream<TermCode> expand(TermCode concept) {
        if (Objects.requireNonNull(concept).equals(this.concept)) {
            return leafConcepts();
        } else if (children.isEmpty()) {
            return Stream.of();
        } else {
            return children.stream().flatMap(n -> n.expand(concept));
        }
    }

    private Stream<TermCode> leafConcepts() {
        if (children.isEmpty()) {
            return Stream.of(concept);
        } else {
            return children.stream().flatMap(ConceptNode::leafConcepts);
        }
    }
}
