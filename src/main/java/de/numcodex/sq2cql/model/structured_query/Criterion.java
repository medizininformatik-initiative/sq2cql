package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.numcodex.sq2cql.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.Container;
import de.numcodex.sq2cql.model.cql.DefaultExpression;
import de.numcodex.sq2cql.model.cql.Expression;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
    Criterion TRUE = new Criterion() {

        @Override
        public ContextualConcept getConcept() {
            return null;
        }

        @Override
        public Container<DefaultExpression> toCql(MappingContext mappingContext) {
            return Container.of(Expression.TRUE).moveToPatientContext("Criterion");
        }

        @Override
        public Container<DefaultExpression> toReferencesCql(MappingContext mappingContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AttributeFilter> attributeFilters() {
            return List.of();
        }

        @Override
        public TimeRestriction timeRestriction() {
            return null;
        }
    };

    /**
     * A criterion that always evaluates to {@code false}.
     */
    Criterion FALSE = new Criterion() {

        @Override
        public ContextualConcept getConcept() {
            return null;
        }

        @Override
        public Container<DefaultExpression> toCql(MappingContext mappingContext) {
            return Container.of(Expression.FALSE).moveToPatientContext("Criterion");
        }

        @Override
        public Container<DefaultExpression> toReferencesCql(MappingContext mappingContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AttributeFilter> attributeFilters() {
            return List.of();
        }

        @Override
        public TimeRestriction timeRestriction() {
            return null;
        }
    };

    @JsonCreator
    static Criterion create(@JsonProperty("context") TermCode context,
                            @JsonProperty("termCodes") List<TermCode> termCodes,
                            @JsonProperty("valueFilter") ObjectNode valueFilter,
                            @JsonProperty("timeRestriction") TimeRestriction timeRestriction,
                            @JsonProperty("attributeFilters") List<ObjectNode> attributeFilters) {
        var concept = ContextualConcept.of(requireNonNull(context, "missing JSON property: context"),
                Concept.of(requireNonNull(termCodes, "missing JSON property: termCodes")));

        AbstractCriterion<?> criterion;

        if (valueFilter == null) {
            criterion = ConceptCriterion.of(concept, timeRestriction);
        } else {
            var type = valueFilter.get("type").asText();
            switch (type) {
                case "quantity-comparator" -> {
                    var comparator = Comparator.fromJson(valueFilter.get("comparator").asText());
                    var value = valueFilter.get("value").decimalValue();
                    var unit = valueFilter.get("unit");
                    criterion = unit == null
                            ? NumericCriterion.of(concept, comparator, value, timeRestriction)
                            : NumericCriterion.of(concept, comparator, value, unit.get("code").asText(),
                            timeRestriction);
                }
                case "quantity-range" -> {
                    var lowerBound = valueFilter.get("minValue").decimalValue();
                    var upperBound = valueFilter.get("maxValue").decimalValue();
                    var unit = valueFilter.get("unit");
                    criterion = unit == null
                            ? RangeCriterion.of(concept, lowerBound, upperBound, timeRestriction)
                            : RangeCriterion.of(concept, lowerBound, upperBound,
                            unit.get("code").asText(),
                            timeRestriction);
                }
                case "concept" -> {
                    var selectedConcepts = valueFilter.get("selectedConcepts");
                    if (selectedConcepts == null || selectedConcepts.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Missing or empty `selectedConcepts` key in concept criterion.");
                    }
                    criterion = ValueSetCriterion.of(concept,
                            StreamSupport.stream(selectedConcepts.spliterator(), false)
                                    .map(TermCode::fromJsonNode).toList(), timeRestriction);
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

    static Criterion fromJsonNode(JsonNode node) {
        return Criterion.create(TermCode.fromJsonNode(node.get("context")),
                getAndMap(node, "termCodes", termCodesNode -> StreamSupport.stream(termCodesNode.spliterator(), false)
                        .map(TermCode::fromJsonNode).toList()),
                asObjectNode(node.get("valueFilter")),
                getAndMap(node, "timeRestriction", TimeRestriction::fromJsonNode),
                getAndMap(node, "attributeFilters", filtersNode ->
                        StreamSupport.stream(filtersNode.spliterator(), false)
                                .map(filterNode -> filterNode.isObject() ? (ObjectNode) filterNode : null).toList()));
    }

    private static ObjectNode asObjectNode(JsonNode node) {
        return node == null ? null : node.isObject() ? (ObjectNode) node : null;
    }

    private static <T> T getAndMap(JsonNode node, String name, Function<JsonNode, T> mapper) {
        var child = node.get(name);
        return child == null ? null : mapper.apply(child);
    }

    ContextualConcept getConcept();

    /**
     * Translates this criterion into a CQL expression.
     *
     * @param mappingContext contains the mappings needed to create the CQL expression
     * @return a {@link Container} of the CQL expression together with its used {@link CodeSystemDefinition
     * CodeSystemDefinitions}
     */
    Container<DefaultExpression> toCql(MappingContext mappingContext);

    Container<DefaultExpression> toReferencesCql(MappingContext mappingContext);

    List<AttributeFilter> attributeFilters();

    TimeRestriction timeRestriction();
}
