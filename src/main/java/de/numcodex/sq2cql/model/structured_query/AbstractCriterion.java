package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract criterion holding the concept, every non-static criterion has.
 */
abstract class AbstractCriterion<T extends AbstractCriterion<T>> implements Criterion {

    private static final IdentifierExpression PATIENT = StandardIdentifierExpression.of("Patient");

    final ContextualConcept concept;
    final List<AttributeFilter> attributeFilters;
    final TimeRestriction timeRestriction;

    AbstractCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
                      TimeRestriction timeRestriction) {
        this.concept = requireNonNull(concept);
        this.attributeFilters = List.copyOf(attributeFilters);
        this.timeRestriction = timeRestriction;
    }

    /**
     * Returns the code selector expression according to the given term code.
     *
     * @param mappingContext the mapping context to determine the code system definition of the
     *                       concept
     * @param termCode       the term code to use
     * @return a {@link Container} of the code selector expression together with its used {@link
     * CodeSystemDefinition}
     */
    static Container<CodeSelector> codeSelector(MappingContext mappingContext, TermCode termCode) {
        var codeSystemDefinition = mappingContext.findCodeSystemDefinition(termCode.system())
                .orElseThrow(() -> new IllegalStateException("code system alias for `%s` not found"
                        .formatted(termCode.system())));
        return Container.of(CodeSelector.of(termCode.code(), codeSystemDefinition.name()),
                codeSystemDefinition);
    }

    /**
     * Returns the retrieve expression according to the given term code.
     * <p>
     * Uses the mapping context to determine the resource type of the retrieve expression and the code
     * system definition of the concept.
     *
     * @param mappingContext the mapping context
     * @param termCode       the term code to use
     * @return a {@link Container} of the retrieve expression together with its used {@link
     * CodeSystemDefinition}
     * @throws TranslationException if the {@link RetrieveExpression} can't be build
     */
    static Container<RetrieveExpression> retrieveExpr(MappingContext mappingContext,
                                                      ContextualTermCode termCode) {
        var mapping = mappingContext.findMapping(termCode)
                .orElseThrow(() -> new MappingNotFoundException(termCode));

        return mapping.termCodeFhirPath() == null
                ? codeSelector(mappingContext, mapping.primaryCode())
                .map(terminology -> RetrieveExpression.of(mapping.resourceType(), terminology))
                : Container.of(RetrieveExpression.of(mapping.resourceType()));
    }

    private static String referenceName(TermCode termCode) {
        return termCode.code() + "Ref";
    }

    public abstract T appendAttributeFilter(AttributeFilter attributeFilter);

    @Override
    public List<AttributeFilter> attributeFilters() {
        return attributeFilters;
    }

    @Override
    public ContextualConcept getConcept() {
        return concept;
    }

    @Override
    public Container<DefaultExpression> toCql(MappingContext mappingContext) {
        var expr = fullExpr(mappingContext);
        if (expr.isEmpty()) {
            throw new TranslationException("Failed to expand the concept %s.".formatted(concept));
        }
        return expr.moveToPatientContext("Criterion");
    }

    @Override
    public Container<DefaultExpression> toReferencesCql(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .map(termCode -> refExpr(mappingContext, termCode))
                .reduce(Container.empty(), Container.UNION);
    }

    /**
     * Builds an OR-expression with an expression for each concept of the expansion of {@code
     * termCode}.
     */
    private Container<DefaultExpression> fullExpr(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .map(termCode -> expr(mappingContext, termCode))
                .reduce(Container.empty(), Container.OR);
    }

    private Container<DefaultExpression> expr(MappingContext mappingContext, ContextualTermCode termCode) {
        var mapping = mappingContext.findMapping(termCode)
                .orElseThrow(() -> new MappingNotFoundException(termCode));
        switch (mapping.resourceType()) {
            case "Patient" -> {
                return valueExpr(mappingContext, mapping, PATIENT);
            }
            case "MedicationAdministration" -> {
                var query = medicationReferencesExpr(mappingContext, termCode.termCode())
                        .moveToUnfilteredContext(referenceName(termCode.termCode()))
                        .map(medicationReferencesExpr -> {
                            var retrieveExpr = RetrieveExpression.of("MedicationAdministration");
                            var alias = retrieveExpr.alias();
                            var sourceClause = SourceClause.of(AliasedQuerySource.of(retrieveExpr, alias));
                            var referenceExpression = InvocationExpression.of(alias, "medication.reference");
                            var whereExpr = MembershipExpression.in(referenceExpression, medicationReferencesExpr);
                            return QueryExpression.of(sourceClause, WhereClause.of(whereExpr));
                        });
                return appendModifier(mappingContext, mapping, query).map(ExistsExpression::of);
            }
            default -> {
                return retrieveExpr(mappingContext, termCode).flatMap(retrieveExpr -> {
                    var alias = retrieveExpr.alias();
                    var sourceClause = SourceClause.of(AliasedQuerySource.of(retrieveExpr, alias));
                    var query = valueExpr(mappingContext, mapping, alias)
                            .map(valueExpr -> QueryExpression.of(sourceClause, WhereClause.of(valueExpr)))
                            .or(() -> QueryExpression.of(sourceClause));
                    return appendModifier(mappingContext, mapping, query).map(ExistsExpression::of);
                });
            }
        }
    }

    private Container<DefaultExpression> refExpr(MappingContext mappingContext, ContextualTermCode termCode) {
        var mapping = mappingContext.findMapping(termCode)
                .orElseThrow(() -> new MappingNotFoundException(termCode));
        return retrieveExpr(mappingContext, termCode).flatMap(retrieveExpr -> {
            var alias = retrieveExpr.alias();
            var sourceClause = SourceClause.of(AliasedQuerySource.of(retrieveExpr, alias));
            var query = valueExpr(mappingContext, mapping, alias)
                    .map(valueExpr -> QueryExpression.of(sourceClause, WhereClause.of(valueExpr)))
                    .or(() -> QueryExpression.of(sourceClause));
            return appendModifier(mappingContext, mapping, query).map(WrapperExpression::new);
        });
    }

    /*
     * Creates an expression from value criteria that will end up in the where clause.
     */
    abstract Container<DefaultExpression> valueExpr(MappingContext mappingContext, Mapping mapping,
                                                    IdentifierExpression sourceAlias);

    /*
     * Appends expressions from modifier criteria to the query.
     */
    private Container<QueryExpression> appendModifier(MappingContext mappingContext, Mapping mapping,
                                                      Container<QueryExpression> queryContainer) {
        var termCodeModifier = termCodeModifier(mapping);
        if (termCodeModifier != null) {
            queryContainer = termCodeModifier.updateQuery(mappingContext, queryContainer);
        }
        for (var modifier : mapping.fixedCriteria()) {
            queryContainer = modifier.updateQuery(mappingContext, queryContainer);
        }
        for (var modifier : resolveAttributeModifiers(mapping.attributeMappings())) {
            queryContainer = modifier.updateQuery(mappingContext, queryContainer);
        }
        if (timeRestriction != null) {
            queryContainer = timeRestriction.toModifier(mapping).updateQuery(mappingContext, queryContainer);
        }
        return queryContainer;
    }

    private Modifier termCodeModifier(Mapping mapping) {
        var termCodeFhirPath = mapping.termCodeFhirPath();
        return termCodeFhirPath == null ? null : new CodingModifier(termCodeFhirPath + ".coding", List.of(mapping.primaryCode()));
    }

    private List<Modifier> resolveAttributeModifiers(Map<TermCode, AttributeMapping> attributeMappings) {
        return attributeFilters.stream().map(attributeFilter -> {
            var key = attributeFilter.attributeCode();
            var mapping = Optional.ofNullable(attributeMappings.get(key)).orElseThrow(() ->
                    new AttributeMappingNotFoundException(key));
            return attributeFilter.toModifier(mapping);
        }).toList();
    }

    @Override
    public TimeRestriction timeRestriction() {
        return timeRestriction;
    }

    /**
     * Returns a query expression that returns all references of Medication with {@code code}.
     * <p>
     * Has to be placed into the Unfiltered context.
     */
    private Container<QueryExpression> medicationReferencesExpr(MappingContext mappingContext, TermCode code) {
        return codeSelector(mappingContext, code)
                .map(terminology -> RetrieveExpression.of("Medication", terminology))
                .map(retrieveExpr -> {
                    var alias = retrieveExpr.alias();
                    var sourceClause = SourceClause.of(AliasedQuerySource.of(retrieveExpr, alias));
                    var returnClause = ReturnClause.of(AdditionExpressionTerm.of(
                            StringLiteralExpression.of("Medication/"), InvocationExpression.of(alias, "id")));
                    return QueryExpression.of(sourceClause, returnClause);
                });
    }
}
