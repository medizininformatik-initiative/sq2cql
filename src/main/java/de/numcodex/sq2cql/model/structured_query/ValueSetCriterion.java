package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.Lists;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.MembershipExpression;
import de.numcodex.sq2cql.model.cql.SourceClause;

import java.util.List;

/**
 * A {@code ValueSetCriterion} will select all patients that have at least one resource represented by that concept and
 * coded value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a coded laboratory value.
 */
public final class ValueSetCriterion extends AbstractCriterion {

    private final List<TermCode> selectedConcepts;

    private ValueSetCriterion(Concept concept, List<AttributeFilter> attributeFilters, List<TermCode> selectedConcepts) {
        super(concept, attributeFilters);
        this.selectedConcepts = selectedConcepts;
    }

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param selectedConcepts at least one selected value concept
     * @return the {@code ValueSetCriterion}
     */
    public static ValueSetCriterion of(Concept concept, TermCode... selectedConcepts) {
        if (selectedConcepts == null || selectedConcepts.length == 0) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetCriterion(concept, List.of(), List.of(selectedConcepts));
    }

    /**
     * Returns a {@code ValueSetCriterion}.
     *
     * @param concept          the concept the criterion represents
     * @param selectedConcepts at least one selected value concept
     * @param attributeFilters additional filters on particular attributes
     * @return the {@code ValueSetCriterion}
     */
    public static ValueSetCriterion of(Concept concept, List<TermCode> selectedConcepts,
                                       AttributeFilter... attributeFilters) {
        if (selectedConcepts == null || selectedConcepts.isEmpty()) {
            throw new IllegalArgumentException("empty selected concepts");
        }
        return new ValueSetCriterion(concept, List.of(attributeFilters), List.copyOf(selectedConcepts));
    }

    public List<TermCode> getSelectedConcepts() {
        return selectedConcepts;
    }

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
        return retrieveExpr(mappingContext, termCode).flatMap(retrieveExpr -> {
            var alias = retrieveExpr.alias();
            var sourceClause = SourceClause.of(retrieveExpr, alias);
            var mapping = mappingContext.findMapping(termCode).orElseThrow(() -> new MappingNotFoundException(termCode));

            //TODO: Add switch case once all values have a valueType
            if (mapping.valueType() != null && mapping.valueType().equals("concept_coding")) {
                var codeExpr = InvocationExpression.of(alias, mapping.valueFhirPath());
                var selectedConceptsExpr = selectedConceptsExpr(mappingContext, codeExpr);
                var modifiers = Lists.concat(mapping.fixedCriteria(), resolveAttributeModifiers(mapping.attributeMappings()));
                if (modifiers.isEmpty()) {
                    return selectedConceptsExpr.map(expr -> existsExpr(sourceClause, expr));
                } else {
                    var modifiersExpr = modifiersExpr(modifiers, mappingContext, alias);
                    return Container.AND.apply(selectedConceptsExpr, modifiersExpr)
                        .map(expr -> existsExpr(sourceClause, expr));
                }
            }
            var codingExpr = InvocationExpression.of(InvocationExpression.of(alias, mapping.valueFhirPath()), "coding");
            var selectedConceptsExpr = selectedConceptsExpr(mappingContext, codingExpr);
            var modifiers = Lists.concat(mapping.fixedCriteria(), resolveAttributeModifiers(mapping.attributeMappings()));
            if (modifiers.isEmpty()) {
                return selectedConceptsExpr.map(expr -> existsExpr(sourceClause, expr));
            } else {
                var modifiersExpr = modifiersExpr(modifiers, mappingContext, alias);
                return Container.AND.apply(selectedConceptsExpr, modifiersExpr)
                        .map(expr -> existsExpr(sourceClause, expr));
            }
        });
    }

    private Container<BooleanExpression> selectedConceptsExpr(MappingContext mappingContext, Expression codingExpr) {
        return selectedConcepts.stream()
                .map(concept -> codeSelector(mappingContext, concept).map(terminology ->
                        (BooleanExpression) MembershipExpression.contains(codingExpr, terminology)))
                .reduce(Container.empty(), Container.OR);
    }
}
