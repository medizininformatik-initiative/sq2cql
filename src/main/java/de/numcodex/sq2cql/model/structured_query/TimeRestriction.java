package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.Mapping;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record TimeRestriction(LocalDate afterDate, LocalDate beforeDate) {

    public TimeRestriction {
        requireNonNull(afterDate);
        requireNonNull(beforeDate);
        if (beforeDate.isBefore(afterDate)) {
            throw new IllegalArgumentException("Invalid time restriction: beforeDate `%s` is before afterDate `%s` but should not be."
                    .formatted(beforeDate, afterDate));
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
            return TimeRestriction.of(LocalDate.of(1900, 1, 1), LocalDate.parse(beforeDate));
        } else if (beforeDate == null) {
            return TimeRestriction.of(LocalDate.parse(afterDate), LocalDate.of(2040, 1, 1));
        } else {
            return TimeRestriction.of(LocalDate.parse(afterDate), LocalDate.parse(beforeDate));
        }
    }

    public static TimeRestriction fromJsonNode(JsonNode node) {
        return TimeRestriction.create(node.get("afterDate").asText(), node.get("beforeDate").asText());
    }

    public Modifier toModifier(Mapping mapping) {
        var path = mapping.timeRestrictionMapping()
                .orElseThrow(() -> new IllegalStateException("Missing time restriction in mapping with key %s."
                        .formatted(mapping.key())));
        return TimeRestrictionModifier.of(path, afterDate, beforeDate);
    }
}
