package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.model.common.TermCode;

import java.util.List;
import java.util.Map;

public class AgeFunctionMapping {

    public static final TermCode AGE = TermCode.of("http://snomed.info/sct", "424144002", "Current chronological age");

    private static final Map<String, FunctionInvocation> ageFunctionMap = Map.of(
            "a", FunctionInvocation.of("AgeInYears", List.of()),
            "m", FunctionInvocation.of("AgeInMonths", List.of()),
            "wk", FunctionInvocation.of("AgeInWeeks", List.of()),
            "d", FunctionInvocation.of("AgeInDays", List.of()),
            "h", FunctionInvocation.of("AgeInHours", List.of())
    );

    public static FunctionInvocation getAgeFunction(String unit) {
        var ageFunction = ageFunctionMap.get(unit);
        if (ageFunction == null) {
            throw new IllegalArgumentException("Invalid unit for age function: " + unit);
        }
        return ageFunction;
    }
}
