package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.ExistsExpression;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented by that concept.
 * <p>
 * Examples are {@code Condition} resources representing the concept of a particular disease.
 */
public final class ConceptCriterion extends AbstractCriterion {

    private ConceptCriterion(TermCode concept) {
        super(concept);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept the concept the criterion represents
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(TermCode concept) {
        return new ConceptCriterion(concept);
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
                .map(c -> retrieveExpr(mappingContext, c).map(r -> (BooleanExpression) ExistsExpression.of(r)))
                .reduce(Container.empty(), Container.OR);
    }
}
