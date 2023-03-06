package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.model.common.TermCode;
import java.util.HashMap;
import java.util.List;

public class AgeFunctionMapping {
  public static final TermCode AGE = TermCode.of("http://snomed.info/sct", "424144002",
      "Current chronological age");

  private static FunctionInvocation ageFunction;
  private static final HashMap<String, FunctionInvocation> ageFunctionMap;

  static {
    ageFunctionMap = new HashMap<>();
    ageFunctionMap.put("a", FunctionInvocation.of("AgeInYears", List.of()));
    ageFunctionMap.put("m", FunctionInvocation.of("AgeInMonths", List.of()));
    ageFunctionMap.put("wk", FunctionInvocation.of("AgeInWeeks", List.of()));
    ageFunctionMap.put("d", FunctionInvocation.of("AgeInDays", List.of()));
    ageFunctionMap.put("h", FunctionInvocation.of("AgeInHours", List.of()));
  }

  public static FunctionInvocation getAgeFunction(String unit) {
    var ageFunction = ageFunctionMap.get(unit);
    if (ageFunction == null) {
      throw new IllegalArgumentException("Invalid unit for age function: " + unit);
    }
    return ageFunction;
  }
}
