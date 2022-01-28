package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.stream.StreamSupport;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface AttributeFilter {

    static AttributeFilter fromJsonNode(JsonNode attributeFilter) {
        var attributeCode = TermCode.fromJsonNode(attributeFilter.get("attributeCode"));
        var type = attributeFilter.get("type").asText();
        if ("quantity-comparator".equals(type)) {
            var comparator = Comparator.fromJson(attributeFilter.get("comparator").asText());
            var value = attributeFilter.get("value").decimalValue();
            var unit = attributeFilter.get("unit");
            if (unit == null) {
                return NumericAttributeFilter.of(attributeCode, comparator, value);
            } else {
                return NumericAttributeFilter.of(attributeCode, comparator, value, unit.get("code").asText());
            }
        }
        if ("quantity-range".equals(type)) {
            var lowerBound = attributeFilter.get("minValue").decimalValue();
            var upperBound = attributeFilter.get("maxValue").decimalValue();
            var unit = attributeFilter.get("unit");
            if (unit == null) {
                return RangeAttributeFilter.of(attributeCode, lowerBound, upperBound);
            } else {
                return RangeAttributeFilter.of(attributeCode, lowerBound, upperBound, unit.get("code").asText());
            }
        }
        if ("concept".equals(type)) {
            var selectedConcepts = attributeFilter.get("selectedConcepts");
            if (selectedConcepts == null) {
                throw new IllegalArgumentException("Missing `selectedConcepts` key in concept criterion.");
            }
            return ValueSetAttributeFilter.of(attributeCode, StreamSupport.stream(selectedConcepts.spliterator(), false)
                    .map(TermCode::fromJsonNode).toArray(TermCode[]::new));
        }
        throw new IllegalArgumentException("unknown valueFilter type: " + type);
    }

    TermCode attributeCode();

    Modifier toModifier(AttributeMapping attributeMapping);
}
