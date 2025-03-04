package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.MappingContext;
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
public record CodeEquivalentModifier(String path, List<TermCode> concepts) implements SimpleModifier {

    public CodeEquivalentModifier {
        requireNonNull(path);
        concepts = List.copyOf(concepts);
    }

    public static CodeEquivalentModifier of(String path, TermCode... concepts) {
        return new CodeEquivalentModifier(path, List.of(concepts));
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
        return "provision.provision.code".equals(path)
                ? multiValuedComparatorExpression(leftHandSide, terminology)
                : ComparatorExpression.equivalent(leftHandSide, terminology);
    }

    private static DefaultExpression multiValuedComparatorExpression(InvocationExpression leftHandSide, CodeSelector terminology) {
        var alias = StandardIdentifierExpression.of("C");
        return ExistsExpression.of(QueryExpression.of(SourceClause.of(AliasedQuerySource.of(leftHandSide, alias)),
                WhereClause.of(ComparatorExpression.equivalent(alias, terminology))));
    }
}
