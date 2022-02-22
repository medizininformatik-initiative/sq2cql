package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;

import java.util.List;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented by that concept.
 * <p>
 * Examples are {@code Condition} resources representing the concept of a particular disease.
 */
public final class ConceptCriterion extends AbstractCriterion {

    private ConceptCriterion(Concept concept, List<AttributeFilter> attributeFilters) {
        super(concept, attributeFilters);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(Concept concept, AttributeFilter... attributeFilters) {
        return new ConceptCriterion(concept, List.of(attributeFilters));
    }

    @Override
    Container<BooleanExpression> valueExpr(MappingContext mappingContext,
                                           Mapping mapping, IdentifierExpression identifier) {
        return Container.empty();
    }
}
