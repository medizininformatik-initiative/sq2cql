package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ExistsExpression;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented by that concept.
 *
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

    public Container<BooleanExpression> toCql(MappingContext mappingContext) {
        return retrieveExpr(mappingContext).map(ExistsExpression::of);
    }
}
