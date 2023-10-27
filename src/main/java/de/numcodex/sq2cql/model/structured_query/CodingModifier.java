package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.MembershipExpression;

import java.util.List;

import static de.numcodex.sq2cql.model.structured_query.AbstractCriterion.codeSelector;
import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
public record CodingModifier(String path, List<TermCode> concepts) implements SimpleModifier {

    public CodingModifier {
        requireNonNull(path);
        concepts = List.copyOf(concepts);
    }

    public static CodingModifier of(String path, TermCode... concepts) {
        return new CodingModifier(path, List.of(concepts));
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var codingExpr = InvocationExpression.of(InvocationExpression.of(sourceAlias, path), "coding");
        return concepts.stream()
                .map(concept -> codeSelector(mappingContext, concept).map(terminology ->
                        (BooleanExpression) MembershipExpression.contains(codingExpr, terminology)))
                .reduce(Container.empty(), Container.OR);
    }
}
