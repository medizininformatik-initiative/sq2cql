package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record ReferenceAttributeFilter(TermCode attributeCode, List<Criterion> criteria) implements AttributeFilter {

    public ReferenceAttributeFilter {
        requireNonNull(attributeCode);
        criteria = List.copyOf(criteria);
    }

    /**
     * Returns a {@code ReferenceAttributeFilter}.
     *
     * @param attributeCode the code identifying the attribute
     * @param criteria      at least one criterion
     * @return the {@code ValueSetCriterion}
     * @throws IllegalArgumentException if {@code criteria} are empty
     * @throws NullPointerException     if any of the {@code criteria} is null
     */
    public static ReferenceAttributeFilter of(TermCode attributeCode, Criterion... criteria) {
        if (criteria == null || criteria.length == 0) {
            throw new IllegalArgumentException("empty criteria");
        }
        return new ReferenceAttributeFilter(attributeCode, List.of(criteria));
    }

    @Override
    public Modifier toModifier(AttributeMapping attributeMapping) {
        var types = attributeMapping.types();
        if (types.size() != 1) {
            throw new IllegalArgumentException("Expect exactly one type of the attribute mapping but was %d types."
                    .formatted(types.size()));
        }
        var type = types.get(0);
        if (!"Reference".equals(type)) {
            throw new IllegalArgumentException("The type of the attribute mapping has to be `Reference` but was `%s`."
                    .formatted(type));
        }
        var targetType = attributeMapping.referenceTargetType();
        if (targetType == null) {
            throw new IllegalArgumentException("The reference target type of the attribute mapping is missing.");
        }
        return ReferenceModifier.of(attributeMapping.path(), targetType, criteria);
    }
}
