package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.AliasExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSelector;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.RetrieveExpression;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Abstract criterion holding the concept, every non-static criterion has.
 */
public abstract class AbstractCriterion implements Criterion {

    final Concept concept;
    final List<Modifier> modifiers;

    AbstractCriterion(Concept concept, List<Modifier> modifiers) {
        this.concept = requireNonNull(concept);
        this.modifiers = requireNonNull(modifiers);
    }

    /**
     * Returns the code selector expression according to the given term code.
     *
     * @param mappingContext the mapping context to determine the code system definition of the concept
     * @param termCode       the term code to use
     * @return a {@link Container} of the code selector expression together with its used {@link CodeSystemDefinition}
     */
    static Container<CodeSelector> codeSelector(MappingContext mappingContext, TermCode termCode) {
        var codeSystemDefinition = mappingContext.findCodeSystemDefinition(termCode.system())
                .orElseThrow(() -> new IllegalStateException("code system alias for `%s` not found"
                        .formatted(termCode.system())));
        return Container.of(CodeSelector.of(termCode.code(), codeSystemDefinition.name()), codeSystemDefinition);
    }

    /**
     * Returns the retrieve expression according to the given term code.
     * <p>
     * Uses the mapping context to determine the resource type of the retrieve expression and the code system definition
     * of the concept.
     *
     * @param mappingContext the mapping context
     * @param termCode       the term code to use
     * @return a {@link Container} of the retrieve expression together with its used {@link CodeSystemDefinition}
     * @throws TranslationException if the {@link RetrieveExpression} can't be build
     */
    static Container<RetrieveExpression> retrieveExpr(MappingContext mappingContext, TermCode termCode) {
        return codeSelector(mappingContext, termCode).map(terminology -> {
            var mapping = mappingContext.findMapping(termCode)
                    .orElseThrow(() -> new MappingNotFoundException(termCode));
            return RetrieveExpression.of(mapping.resourceType(), terminology);
        });
    }

    static Container<BooleanExpression> modifiersExpr(List<Modifier> modifiers, MappingContext mappingContext,
                                                      AliasExpression alias) {
        return modifiers.stream()
                .map(m -> m.expression(mappingContext, alias))
                .reduce(Container.empty(), Container.AND);
    }

    public Concept getConcept() {
        return concept;
    }
}
