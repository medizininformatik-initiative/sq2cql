package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.Mapping;

public record TimeRestriction(String afterDate, String beforeDate) {

  public Modifier toModifier(Mapping mapping) {
    return TimeRestrictionModifier.of(mapping.timeRestrictionPath(), afterDate, beforeDate);
  }

  public static TimeRestriction of(String afterDate, String beforeDate) {
    return new TimeRestriction(afterDate, beforeDate);
  }

  @JsonCreator
  public static TimeRestriction create(@JsonProperty("afterDate") String afterDate,
      @JsonProperty("beforeDate") String beforeDate) {
    return TimeRestriction.of(afterDate, beforeDate);
  }
}
