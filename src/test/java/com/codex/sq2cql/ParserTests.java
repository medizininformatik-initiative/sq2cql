package com.codex.sq2cql;

import com.codex.sq2cql.data_model.common.Comparator;
import com.codex.sq2cql.data_model.common.TermCode;
import com.codex.sq2cql.data_model.cql.translator.CriterionTranslator;
import com.codex.sq2cql.data_model.structured_query.NumericCriterion;
import com.codex.sq2cql.data_model.structured_query.RangeCriterion;
import com.codex.sq2cql.data_model.structured_query.ValueSetCriterion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ParserTests {
    private TermCode termcode = new TermCode("A", "test.org");

    @Test
    @DisplayName("A < 5")
    @Disabled("Have to reevaluate if NumericCriterion type is sufficient for the evaluation or of more specific types like NumericObservation are needed")
    public void parseNumericCriterion()
    {

        var numericCriterion = new NumericCriterion(termcode, 5, Comparator.LESS_THAN);
        var comparatorExpression = CriterionTranslator.translateCriterion(numericCriterion);
        Assertions.assertEquals("(A) < (5)", comparatorExpression.toString());
    }

    @Test
    @DisplayName("2 < A < 5")
    public void parseRangeCriterion() {
        var rangeCriterion = new RangeCriterion(termcode, 2, 5);
        var rangeExpression = CriterionTranslator.translateCriterion(rangeCriterion);
        Assertions.assertEquals("((A) >= (2)) and\n((A) <= (5))", rangeExpression.toString());
    }

    @Test
    @DisplayName("(A = male) or (A = female)")
    public void parseValueSetCriterion()
    {
        var valueSetCriterion = new ValueSetCriterion(termcode, List.of("male", "female"));
        var valueSetExpression = CriterionTranslator.translateCriterion(valueSetCriterion);
        Assertions.assertEquals("((A) = (male)) or\n((A) = (female))", valueSetExpression.toString());
    }
    

}
