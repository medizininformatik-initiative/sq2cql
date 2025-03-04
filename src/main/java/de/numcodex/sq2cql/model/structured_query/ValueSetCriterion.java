package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.*;

import java.util.LinkedList;
import java.util.List;

/**
 * A {@code ValueSetCriterion} will select all patients that have at least one resource represented
 * by that concept and coded value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a coded laboratory value.
 */
public final class ValueSetCriterion extends AbstractCriterion<ValueSetCriterion> {

    private final List<TermCode> selectedConcepts;

    private ValueSetCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
                              TimeRestriction timeRestriction, List<TermCode> selectedConcepts) {
        super(concept, attributeFilters, timeRestriction);
        this.selectedConcepts = selectedConcepts;
    }

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param selectedConcepts at least one selected value concept
     * @return the {@code ValueSetCriterion}
     */
    public static ValueSetCriterion of(ContextualConcept concept, TermCode... selectedConcepts) {
        if (selectedConcepts == null || selectedConcepts.length == 0) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetCriterion(concept, List.of(), null, List.of(selectedConcepts));
    }

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param timeRestriction  the timeRestriction applied to the concept
     * @param selectedConcepts at least one selected value concept
     * @return the {@code ValueSetCriterion}
     */
    public static ValueSetCriterion of(ContextualConcept concept, List<TermCode> selectedConcepts, TimeRestriction timeRestriction) {
        if (selectedConcepts == null || selectedConcepts.isEmpty()) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetCriterion(concept, List.of(), timeRestriction, List.copyOf(selectedConcepts));
    }

    @Override
    public ValueSetCriterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.attributeFilters);
        attributeFilters.add(attributeFilter);
        return new ValueSetCriterion(concept, attributeFilters, timeRestriction, selectedConcepts);
    }

    public List<TermCode> getSelectedConcepts() {
        return selectedConcepts;
    }

    @Override
    Container<DefaultExpression> valueExpr(MappingContext mappingContext, Mapping mapping, IdentifierExpression sourceAlias) {
        if (mapping.valueMapping().isEmpty()) {
            throw new IllegalStateException("Numeric criterion is used with the mapping of `%s` without value mapping.".formatted(mapping.key()));
        }

        var valueMapping = mapping.valueMapping().get();

        // Mapping ensures that the termCodeMapping has exactly one type
        return switch (valueMapping.types().get(0)) {
            case CODE -> selectedConcepts.stream()
                    .map(concept -> Container.of(ComparatorExpression.equal(
                            InvocationExpression.of(sourceAlias, valueMapping.path()),
                            StringLiteralExpression.of(concept.code()))))
                    .reduce(Container.empty(), Container.OR);
            case CODING, CODEABLE_CONCEPT -> selectedConcepts.stream()
                    .map(concept -> codeSelector(mappingContext, concept).map(terminology ->
                            ComparatorExpression.equivalent(InvocationExpression.of(sourceAlias, valueMapping.path()), terminology)))
                    .reduce(Container.empty(), Container.OR);
            default ->
                    throw new IllegalArgumentException("Unsupported type `%s` in value expression.".formatted(valueMapping.types().get(0).fhirTypeName()));
        };
    }
}
