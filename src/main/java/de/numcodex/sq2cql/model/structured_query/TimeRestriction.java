package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.Mapping;

public record TimeRestriction(String afterDate, String beforeDate) {

  public Modifier toModifier(Mapping mapping) {
    var path = mapping.timeRestrictionPath()
            .orElseThrow(() -> new IllegalStateException("Missing timeRestrictionPath in mapping with key %s."
                    .formatted(mapping.key())));
    return TimeRestrictionModifier.of(path, afterDate, beforeDate);
  }

  public static TimeRestriction of(String afterDate, String beforeDate) {
    return new TimeRestriction(afterDate, beforeDate);
  }

  @JsonCreator
  public static TimeRestriction create(@JsonProperty("afterDate") String afterDate,
      @JsonProperty("beforeDate") String beforeDate) {
    //FIXME: quick and dirty for empty timeRestriction
    if (afterDate == null && beforeDate == null) {
      return null;
    }
    return TimeRestriction.of(afterDate, beforeDate);
  }
}
