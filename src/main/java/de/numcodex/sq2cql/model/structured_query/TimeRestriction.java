package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.mapping.Mapping;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public record TimeRestriction(LocalDate afterDate, LocalDate beforeDate) {

    // Limits are derived from closed interval of allowed date values in HL7 CQL
    public static final LocalDate MIN_AFTER_DATE = LocalDate.of(1, 1, 1);
    public static final LocalDate MAX_BEFORE_DATE = LocalDate.of(9999, 12, 31);

    public TimeRestriction {
        requireNonNull(afterDate);
        requireNonNull(beforeDate);
        if (beforeDate.isBefore(afterDate)) {
            throw new IllegalArgumentException("Invalid time restriction: beforeDate `%s` is before afterDate `%s` but should not be."
                    .formatted(beforeDate, afterDate));
        }
        if (afterDate.isBefore(MIN_AFTER_DATE)) {
            throw new IllegalArgumentException("Invalid time restriction: afterDate `%s` is before minimum value `%s`."
                    .formatted(afterDate, MIN_AFTER_DATE));
        }
        if (beforeDate.isAfter(MAX_BEFORE_DATE)) {
            throw new IllegalArgumentException("Invalid time restriction: beforeDate `%s` is after maximum value `%s`."
                    .formatted(beforeDate, MAX_BEFORE_DATE));
        }
    }

    public static TimeRestriction of(LocalDate afterDate, LocalDate beforeDate) {
        return new TimeRestriction(afterDate, beforeDate);
    }

    @JsonCreator
    public static TimeRestriction create(@JsonProperty("afterDate") String afterDate,
                                         @JsonProperty("beforeDate") String beforeDate) {
        if (afterDate == null && beforeDate == null) {
            return null;
        } else if (afterDate == null) {
            return TimeRestriction.of(MIN_AFTER_DATE, LocalDate.parse(beforeDate));
        } else if (beforeDate == null) {
            return TimeRestriction.of(LocalDate.parse(afterDate), MAX_BEFORE_DATE);
        } else {
            return TimeRestriction.of(LocalDate.parse(afterDate), LocalDate.parse(beforeDate));
        }
    }

    public static TimeRestriction fromJsonNode(JsonNode node) {
        var afterDate = Optional.ofNullable(node.get("afterDate")).map(JsonNode::asText).orElse(null);
        var beforeDate = Optional.ofNullable(node.get("beforeDate")).map(JsonNode::asText).orElse(null);
        return TimeRestriction.create(afterDate, beforeDate);
    }

    public Modifier toModifier(Mapping mapping) {
        var path = mapping.timeRestrictionMapping()
                .orElseThrow(() -> new IllegalStateException("Missing time restriction in mapping with key %s."
                        .formatted(mapping.key())));
        return TimeRestrictionModifier.of(path, afterDate, beforeDate);
    }
}
