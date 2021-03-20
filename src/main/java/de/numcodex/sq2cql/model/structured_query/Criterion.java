package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;

import java.util.stream.StreamSupport;

/**
 * A single, atomic criterion in Structured Query.
 *
 * @author Alexander Kiel
 */
public interface Criterion {

    /**
     * A criterion that always evaluates to {@code true}.
     */
    Criterion TRUE = mappingContext -> Container.of(BooleanExpression.TRUE);

    /**
     * A criterion that always evaluates to {@code false}.
     */
    Criterion FALSE = mappingContext -> Container.of(BooleanExpression.FALSE);

    @JsonCreator
    static Criterion create(@JsonProperty("termCode") TermCode concept,
                            @JsonProperty("valueFilter") ObjectNode valueFilter) {
        if (valueFilter == null) {
            return ConceptCriterion.of(concept);
        }

        var type = valueFilter.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            return NumericCriterion.of(concept, Comparator.fromJson(valueFilter.get("comparator").asText()),
                    valueFilter.get("value").decimalValue(),
                    valueFilter.get("quantityUnit").get("code").asText());
        }
        if ("quantity-range".equals(type)) {
            return RangeCriterion.of(concept,
                    valueFilter.get("minValue").decimalValue(),
                    valueFilter.get("maxValue").decimalValue(),
                    valueFilter.get("quantityUnit").get("code").asText());
        }
        if ("concept".equals(type)) {
            var selectedConcepts = valueFilter.get("selectedConcepts");
            if (selectedConcepts == null) {
                throw new IllegalArgumentException("Missing `selectedConcepts` key in concept criterion.");
            }
            return ValueSetCriterion.of(concept, StreamSupport.stream(selectedConcepts.spliterator(), false)
                    .map(TermCode::fromJsonNode).toArray(TermCode[]::new));
        }
        throw new IllegalArgumentException("unknown valueFilter type: " + type);
    }

    /**
     * Translates this criterion into a CQL expression.
     *
     * @param mappingContext contains the mappings needed to create the CQL expression
     * @return a {@link Container} of the CQL expression together with its used {@link CodeSystemDefinition
     * CodeSystemDefinitions}
     */
    Container<BooleanExpression> toCql(MappingContext mappingContext);
}
