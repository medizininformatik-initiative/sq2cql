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

    private ConceptCriterion(Concept concept, List<Modifier> modifiers) {
        super(concept, modifiers);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept   the concept the criterion represents
     * @param modifiers modifiers to use in addition to {@code concept}
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(Concept concept, Modifier... modifiers) {
        return new ConceptCriterion(concept, List.of(modifiers));
    }

    /**
     * Translates this criterion into a CQL expression.
     * <p>
     * Expands {@link #getConcept() concept} returning a disjunction of exists expressions on multiple expansions.
     *
     * @param mappingContext contains the mappings needed to create the CQL expression
     * @return a {@link Container} of the {@link BooleanExpression} together with its used {@link CodeSystemDefinition
     * CodeSystemDefinitions}; never {@link Container#isEmpty() empty}
     * @throws TranslationException if this criterion can't be translated into a {@link BooleanExpression}
     */
    public Container<BooleanExpression> toCql(MappingContext mappingContext) {
        var expr = fullExpr(mappingContext);
        if (expr.isEmpty()) {
            throw new TranslationException("Failed to expand the concept %s.".formatted(concept));
        }
        return expr;
    }

    /**
     * Builds an OR-expression with an expression for each concept of the expansion of {@code termCode}.
     */
    private Container<BooleanExpression> fullExpr(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .map(termCode -> expr(mappingContext, termCode))
                .reduce(Container.empty(), Container.OR);
    }

    private Container<BooleanExpression> expr(MappingContext mappingContext, TermCode termCode) {
        var mapping = mappingContext.getMapping(termCode)
                .orElseThrow(() -> new MappingNotFoundException(termCode));
        var modifiers = Lists.concat(mapping.getFixedCriteria(), this.modifiers);
        if (modifiers.isEmpty()) {
            return retrieveExpr(mappingContext, termCode).map(ExistsExpression::of);
        } else {
            return retrieveExpr(mappingContext, termCode).flatMap(retrieveExpr -> {
                var alias = AliasExpression.of(retrieveExpr.getResourceType().substring(0, 1));
                return modifiersExpr(modifiers, mappingContext, alias)
                        .map(modifiersExpr -> ExistsExpression.of(QueryExpression.of(SourceClause.of(retrieveExpr,
                                alias), WhereClause.of(modifiersExpr))));
            });
        }
    }
}
