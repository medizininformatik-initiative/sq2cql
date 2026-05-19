package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.StreamSupport;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface AttributeFilter {

    Logger logger = LoggerFactory.getLogger(AttributeFilter.class);

    /**
     * Tries to parse an {@code AttributeFilter}.
     * <p>
     * Returns {@link Optional#empty() nothing} if the filter is of type concept and there are no concepts given.
     *
     * @param node the JSON representation of the {@code AttributeFilter}
     * @return either the {@code AttributeFilter} or {@link Optional#empty() nothing} if the {@code AttributeFilter} is empty
     */
    static Optional<AttributeFilter> fromJsonNode(JsonNode node) {
        var attributeCode = TermCode.fromJsonNode(node.get("attributeCode"));
        var type = node.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            var comparator = Comparator.fromJson(node.get("comparator").asText());
            var value = node.get("value").decimalValue();
            var unit = node.get("unit");
            if (unit == null) {
                return Optional.of(NumericAttributeFilter.of(attributeCode, comparator, value));
            } else {
                return Optional.of(NumericAttributeFilter.of(attributeCode, comparator, value, unit.get("code").asText()));
            }
        }
        if ("quantity-range".equals(type)) {
            var lowerBound = node.get("minValue").decimalValue();
            var upperBound = node.get("maxValue").decimalValue();
            var unit = node.get("unit");
            if (unit == null) {
                return Optional.of(RangeAttributeFilter.of(attributeCode, lowerBound, upperBound));
            } else {
                return Optional.of(RangeAttributeFilter.of(attributeCode, lowerBound, upperBound, unit.get("code").asText()));
            }
        }
        if ("concept".equals(type)) {
            var selectedConcepts = node.get("selectedConcepts");
            if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                logger.warn("Skip attribute filter with code `{}` because of empty selected concepts.", attributeCode.code());
                return Optional.empty();
            } else {
                return Optional.of(ValueSetAttributeFilter.of(attributeCode,
                        StreamSupport.stream(selectedConcepts.spliterator(), false)
                                .map(TermCode::fromJsonNode).toArray(TermCode[]::new)));
            }
        }
        if ("reference".equals(type)) {
            var criteria = node.get("criteria");
            if (criteria == null || criteria.isEmpty()) {
                logger.warn("Skip attribute filter with code `{}` because of empty criteria.", attributeCode.code());
                return Optional.empty();
            } else {
                return Optional.of(ReferenceAttributeFilter.of(attributeCode,
                        StreamSupport.stream(criteria.spliterator(), false)
                                .map(Criterion::fromJsonNode).toArray(Criterion[]::new)));
            }
        }
        throw new IllegalArgumentException("unknown attribute filter type: " + type);
    }

    TermCode attributeCode();

    Modifier toModifier(AttributeMapping attributeMapping);
}
