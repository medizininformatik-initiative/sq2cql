package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * A single, atomic criterion in Structured Query.
 *
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
    static Criterion create(@JsonProperty("termCodes") List<TermCode> termCodes,
                            @JsonProperty("valueFilter") ObjectNode valueFilter,
                            @JsonProperty("timeRestriction") TimeRestriction conceptTimeRestriction,
                            @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters) {
        var concept = Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes"));

        AbstractCriterion<?> criterion;

        if (valueFilter == null) {
            criterion = ConceptCriterion.of(concept, conceptTimeRestriction);
        } else {
            var type = valueFilter.get("type").asText();
            switch (type) {
                case "quantity-comparator" -> {
                    var comparator = Comparator.fromJson(valueFilter.get("comparator").asText());
                    var value = valueFilter.get("value").decimalValue();
                    var unit = valueFilter.get("unit");
                    criterion = unit == null
                        ? NumericCriterion.of(concept, comparator, value, conceptTimeRestriction)
                        : NumericCriterion.of(concept, comparator, value, unit.get("code").asText(),
                            conceptTimeRestriction);
                }
                case "quantity-range" -> {
                    var lowerBound = valueFilter.get("minValue").decimalValue();
                    var upperBound = valueFilter.get("maxValue").decimalValue();
                    var unit = valueFilter.get("unit");
                    criterion = unit == null
                        ? RangeCriterion.of(concept, lowerBound, upperBound, conceptTimeRestriction)
                        : RangeCriterion.of(concept, lowerBound, upperBound,
                            unit.get("code").asText(),
                            conceptTimeRestriction);
                }
                case "concept" -> {
                    var selectedConcepts = valueFilter.get("selectedConcepts");
                    if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                        throw new IllegalArgumentException(
                            "Missing or empty `selectedConcepts` key in concept criterion.");
                    }
                    criterion = ValueSetCriterion.of(concept,
                        StreamSupport.stream(selectedConcepts.spliterator(), false)
                            .map(TermCode::fromJsonNode).toList(), conceptTimeRestriction);
                }
                default -> throw new IllegalArgumentException("unknown valueFilter type: " + type);
            }
        }

        var attributes = (attributeFilters == null ? List.<ObjectNode>of() : attributeFilters).stream()
            .map(AttributeFilter::fromJsonNode)
            .flatMap(Optional::stream)
            .toList();
        for (var filter : attributes) {
            criterion = criterion.appendAttributeFilter(filter);
        }
        return criterion;
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
