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
import java.util.Objects;

/**
 * Abstract criterion holding the concept, every non-static criterion has.
 */
public abstract class AbstractCriterion implements Criterion {

    final TermCode termCode;
    final List<Modifier> modifiers;

    AbstractCriterion(TermCode termCode, List<Modifier> modifiers) {
        this.termCode = Objects.requireNonNull(termCode);
        this.modifiers = Objects.requireNonNull(modifiers);
    }

    /**
     * Returns the code selector expression according to the given concept.
     *
     * @param mappingContext the mapping context to determine the code system definition of the concept
     * @param concept        the concept to use
     * @return a {@link Container} of the code selector expression together with its used {@link CodeSystemDefinition}
     */
    static Container<CodeSelector> codeSelector(MappingContext mappingContext, TermCode concept) {
        var codeSystemDefinition = mappingContext.codeSystemDefinition(concept.getSystem());
        return Container.of(CodeSelector.of(concept.getCode(), codeSystemDefinition.getName()), codeSystemDefinition);
    }

    /**
     * Returns the retrieve expression according to the given concept.
     * <p>
     * Uses the mapping context to determine the resource type of the retrieve expression and the code system definition
     * of the concept.
     *
     * @param mappingContext a map of concept (term code) to mapping
     * @param concept        the concept to use
     * @return a {@link Container} of the retrieve expression together with its used {@link CodeSystemDefinition}
     * @throws TranslationException if the {@link RetrieveExpression} can't be build
     */
    static Container<RetrieveExpression> retrieveExpr(MappingContext mappingContext, TermCode concept) {
        return codeSelector(mappingContext, concept).map(terminology -> {
            var mapping = mappingContext.getMapping(concept).orElseThrow(() -> new MappingNotFoundException(concept));
            return RetrieveExpression.of(mapping.getResourceType(), terminology);
        });
    }

    static Container<BooleanExpression> modifiersExpr(List<Modifier> modifiers, MappingContext mappingContext,
                                                      AliasExpression alias) {
        return modifiers.stream()
                .map(m -> m.expression(mappingContext, alias))
                .reduce(Container.empty(), Container.AND);
    }

    public TermCode getTermCode() {
        return termCode;
    }
}
