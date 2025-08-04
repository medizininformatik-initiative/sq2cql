package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.*;

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
    public Container<DefaultExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var codingExpr = InvocationExpression.of(sourceAlias, path);
        return concepts.stream()
                .map(concept -> codeSelector(mappingContext, concept).map(terminology ->
                        MembershipExpression.contains(codingExpr, terminology)))
                .reduce(Container.empty(), Container.OR);
    }
}
