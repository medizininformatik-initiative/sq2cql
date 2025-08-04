package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.mapping.Mapping;
import de.numcodex.sq2cql.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.cql.Container;
import de.numcodex.sq2cql.model.cql.DefaultExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;

import java.util.LinkedList;
import java.util.List;

/**
 * A {@code ConceptCriterion} will select all patients that have at least one resource represented by that concept.
 * <p>
 * Examples are {@code Condition} resources representing the concept of a particular disease.
 */
public final class ConceptCriterion extends AbstractCriterion<ConceptCriterion> {

    private ConceptCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
                             TimeRestriction timeRestriction) {
        super(concept, attributeFilters, timeRestriction);
    }

    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept the concept the criterion represents
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(ContextualConcept concept) {
        return new ConceptCriterion(concept, List.of(), null);
    }


    /**
     * Returns a {@code ConceptCriterion}.
     *
     * @param concept         the concept the criterion represents
     * @param timeRestriction the time restriction on the critieria
     * @return the {@code ConceptCriterion}.
     */
    public static ConceptCriterion of(ContextualConcept concept, TimeRestriction timeRestriction) {
        return new ConceptCriterion(concept, List.of(), timeRestriction);
    }

    @Override
    public ConceptCriterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.attributeFilters);
        attributeFilters.add(attributeFilter);
        return new ConceptCriterion(concept, attributeFilters, timeRestriction);
    }

    @Override
    Container<DefaultExpression> valueExpr(MappingContext mappingContext, Mapping mapping, IdentifierExpression sourceAlias) {
        return Container.empty();
    }
}
