package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.*;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record ReferenceModifier(String path, String targetType, List<Criterion> criteria) implements Modifier {

    public ReferenceModifier {
        requireNonNull(path);
        requireNonNull(targetType);
        criteria = List.copyOf(criteria);
    }

    public static ReferenceModifier of(String path, String targetType, List<Criterion> criteria) {
        return new ReferenceModifier(path, targetType, criteria);
    }

    @Override
    public Container<QueryExpression> updateQuery(MappingContext mappingContext, Container<QueryExpression> queryContainer) {
        return queryContainer.flatMap(query -> getReferenceExpr(mappingContext)
                .moveToPatientContextWithUniqueName(referenceExprName())
                .map(referencesExprName -> {
                    var referenceExpr = InvocationExpression.of(query.sourceAlias(), path);
                    var alias = StandardIdentifierExpression.of(targetType.substring(0, 1));
                    var ref = AdditionExpressionTerm.of(StringLiteralExpression.of(targetType + "/"), InvocationExpression.of(alias, "id"));
                    var comparatorExpr = MembershipExpression.contains(referenceExpr, ref);
                    return query.appendQueryInclusionClause(WithClause.of(AliasedQuerySource.of(referencesExprName, alias), comparatorExpr));
                }));
    }

    private Container<DefaultExpression> getReferenceExpr(MappingContext mappingContext) {
        return criteria.stream()
                .map(criterion -> criterion.toReferencesCql(mappingContext))
                .reduce(Container.empty(), Container.UNION);
    }

    private String referenceExprName() {
        return criteria.stream()
                .map(Criterion::getConcept)
                .map(concept -> concept.context().code() + " " +
                        concept.concept().termCodes().stream().map(TermCode::code).collect(Collectors.joining()))
                .collect(Collectors.joining(" and "));
    }
}
