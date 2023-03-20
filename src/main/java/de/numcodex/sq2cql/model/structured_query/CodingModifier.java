package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.MembershipExpression;

import java.util.List;
import java.util.Objects;

import static de.numcodex.sq2cql.model.structured_query.AbstractCriterion.codeSelector;

/**
 * @author Alexander Kiel
 */
public final class CodingModifier extends AbstractModifier {

    private final List<TermCode> concepts;

    private CodingModifier(String path, List<TermCode> concepts) {
        super(path);
        this.concepts = concepts;
    }

    public static CodingModifier of(String path, TermCode... concepts) {
        return new CodingModifier(path, List.of(concepts));
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression identifier) {
        var codingExpr = InvocationExpression.of(InvocationExpression.of(identifier, path), "coding");
        return concepts.stream()
                .map(concept -> codeSelector(mappingContext, concept).map(terminology ->
                        (BooleanExpression) MembershipExpression.contains(codingExpr, terminology)))
                .reduce(Container.empty(), Container.OR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodingModifier that = (CodingModifier) o;
        return path.equals(that.path) && concepts.equals(that.concepts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, concepts);
    }
}
