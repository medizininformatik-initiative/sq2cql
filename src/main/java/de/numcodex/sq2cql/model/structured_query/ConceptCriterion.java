package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.Lists;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.AliasExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.ExistsExpression;
import de.numcodex.sq2cql.model.cql.QueryExpression;
import de.numcodex.sq2cql.model.cql.SourceClause;
import de.numcodex.sq2cql.model.cql.WhereClause;

import java.util.List;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented by that concept.
 * <p>
 * Examples are {@code Condition} resources representing the concept of a particular disease.
 */
public final class ConceptCriterion extends AbstractCriterion {

    private ConceptCriterion(TermCode concept, List<Modifier> modifiers) {
        super(concept, modifiers);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept   the concept the criterion represents
     * @param modifiers mofifiers to use in addition to {@code concept}
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(TermCode concept, Modifier... modifiers) {
        return new ConceptCriterion(concept, List.of(modifiers));
    }

    /**
     * Translates this criterion into a CQL expression.
     * <p>
     * Expands {@link #getTermCode() concept} returning a disjunction of exists expressions on multiple expansions.
     *
     * @param mappingContext contains the mappings needed to create the CQL expression
     * @return a {@link Container} of the CQL expression together with its used {@link CodeSystemDefinition
     * CodeSystemDefinitions}
     */
    public Container<BooleanExpression> toCql(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .map(c -> expr(mappingContext, c))
                .reduce(Container.empty(), Container.OR);
    }

    private Container<BooleanExpression> expr(MappingContext mappingContext, TermCode concept) {
        var modifiers = Lists.concat(mappingContext.getMapping(concept).getFixedCriteria(), this.modifiers);
        if (modifiers.isEmpty()) {
            return retrieveExpr(mappingContext, concept).map(ExistsExpression::of);
        } else {
            return retrieveExpr(mappingContext, concept).flatMap(retrieveExpr -> {
                var alias = AliasExpression.of(retrieveExpr.getResourceType().substring(0, 1));
                return modifiersExpr(modifiers, mappingContext, alias)
                        .map(modifiersExpr -> ExistsExpression.of(QueryExpression.of(SourceClause.of(retrieveExpr,
                                alias), WhereClause.of(modifiersExpr))));
            });
        }
    }
}
