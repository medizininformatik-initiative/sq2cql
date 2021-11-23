package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;

import java.math.BigDecimal;
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
    static Criterion create(@JsonProperty("termCode") TermCode termCode,
                            @JsonProperty("valueFilter") ObjectNode valueFilter,
                            @JsonProperty("timeRestriction") ObjectNode timeRestriction) {
        if (valueFilter == null) {
            return ConceptCriterion.of(termCode);
        }

        var type = valueFilter.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            var comparator = Comparator.fromJson(valueFilter.get("comparator").asText());
            var value = valueFilter.get("value").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return NumericCriterion.of(termCode, comparator, value);
            } else {
                return NumericCriterion.of(termCode, comparator, value, unit.get("code").asText());
            }
        }
        if ("quantity-range".equals(type)) {
            var lowerBound = valueFilter.get("minValue").decimalValue();
            var upperBound = valueFilter.get("maxValue").decimalValue();
            var unit = valueFilter.get("unit");
            if (unit == null) {
                return RangeCriterion.of(termCode, lowerBound, upperBound);
            } else {
                return RangeCriterion.of(termCode, lowerBound, upperBound, unit.get("code").asText());
            }
        }
        if ("concept".equals(type)) {
            var selectedConcepts = valueFilter.get("selectedConcepts");
            if (selectedConcepts == null) {
                throw new IllegalArgumentException("Missing `selectedConcepts` key in concept criterion.");
            }
            return ValueSetCriterion.of(termCode, StreamSupport.stream(selectedConcepts.spliterator(), false)
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
