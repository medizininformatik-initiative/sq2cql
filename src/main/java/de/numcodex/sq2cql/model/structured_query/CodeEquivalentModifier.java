package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.mapping.Mapping;
import de.numcodex.sq2cql.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.*;

import java.util.List;

import static de.numcodex.sq2cql.model.structured_query.AbstractCriterion.codeSelector;
import static java.util.Objects.requireNonNull;

/**
 * A modifier for comparing possible multiple concepts with a value of type CodeableConcept or Coding.
 *
 * @param path a path to a value of type CodeableConcept or Coding
 * @param concepts possible multiple concepts
 * @author Alexander Kiel
 */
public record CodeEquivalentModifier(String path, Mapping.Cardinality cardinality, List<TermCode> concepts) implements SimpleModifier {

    public CodeEquivalentModifier {
        requireNonNull(path);
        requireNonNull(cardinality);
        concepts = List.copyOf(concepts);
    }

    public static CodeEquivalentModifier of(String path, TermCode... concepts) {
        return new CodeEquivalentModifier(path, Mapping.Cardinality.SINGLE, List.of(concepts));
    }

    public static CodeEquivalentModifier of(String path, Mapping.Cardinality cardinality, TermCode... concepts) {
        return new CodeEquivalentModifier(path, cardinality, List.of(concepts));
    }

    @Override
    public Container<DefaultExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var leftHandSide = InvocationExpression.of(sourceAlias, path);
        return concepts.stream()
                .map(concept -> codeSelector(mappingContext, concept).map(terminology ->
                        comparatorExpression(leftHandSide, terminology)))
                .reduce(Container.empty(), Container.OR);
    }

    private DefaultExpression comparatorExpression(InvocationExpression leftHandSide, CodeSelector terminology) {
        return cardinality == Mapping.Cardinality.MANY
                ? multiValuedComparatorExpression(leftHandSide, terminology)
                : ComparatorExpression.equivalent(leftHandSide, terminology);
    }

    private static DefaultExpression multiValuedComparatorExpression(InvocationExpression leftHandSide, CodeSelector terminology) {
        var alias = StandardIdentifierExpression.of("C");
        return ExistsExpression.of(QueryExpression.of(SourceClause.of(AliasedQuerySource.of(leftHandSide, alias)),
                WhereClause.of(ComparatorExpression.equivalent(alias, terminology))));
    }
}
