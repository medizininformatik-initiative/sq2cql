package com.codex.sq2cql;


import com.codex.sq2cql.data_model.common.Comparator;
import com.codex.sq2cql.data_model.common.TermCode;
import com.codex.sq2cql.data_model.cql.*;
import com.codex.sq2cql.data_model.cql.translator.CriterionTranslator;
import com.codex.sq2cql.data_model.structured_query.Criterion;
import com.codex.sq2cql.data_model.structured_query.NumericCriterion;
import com.codex.sq2cql.data_model.structured_query.ValueSetCriterion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAufgabe1 {
    private BooleanExpression generateFemaleExpression()
    {
        var genderCode = new TermCode("LL2191-6", "https://loinc.org");
        var criterionFemale = new ValueSetCriterion(genderCode, List.of("'female'"));
        return CriterionTranslator.translateCriterion(criterionFemale);
    }

    private BooleanExpression generateConditionExpression()
    {
        var C71_codes = List.of("C71.0", "C71.1", "C71.2", "C71.3", "C71.4", "C71.5");
        var diagnose = new OrExpression();
        for(var code : C71_codes) {
            var c71_code = new TermCode(code, "http://fhir.de/CodeSystem/dimdi/icd-10-gm");
            var criterionC71 = new Criterion(c71_code);
            var expressionC71 = CriterionTranslator.translateCriterion(criterionC71);
            diagnose.addExpression(expressionC71);
        }
        return diagnose;
    }

    private BooleanExpression generatePlatesUnder50Expression(){
        var plateletsCode = new TermCode("26515-7", "https://loinc.org");
        var criterionPlatesUnder50 = new NumericCriterion(plateletsCode, 50, Comparator.LESS_THAN);
        criterionPlatesUnder50.setUnit("g/dl");
        return new ExistExpression(CriterionTranslator.translateCriterion(criterionPlatesUnder50));
    }

    @Test
    public void testAufgabe1()
    {
        var cnf = new AndExpression();
        cnf.addExpression(generateFemaleExpression());
        cnf.addExpression(generateConditionExpression());
        cnf.addExpression(generatePlatesUnder50Expression());
        assertEquals("""
                (((Patient.gender) = ('female'))) and
                ((exists([Condition: Code 'C71.0' from icd10])) or
                (exists([Condition: Code 'C71.1' from icd10])) or
                (exists([Condition: Code 'C71.2' from icd10])) or
                (exists([Condition: Code 'C71.3' from icd10])) or
                (exists([Condition: Code 'C71.4' from icd10])) or
                (exists([Condition: Code 'C71.5' from icd10]))) and
                (exists(from [Observation: Code '26515-7' from loinc] O
                where (O.value as Quantity) < (50 'g/dl')))""", cnf.toString());
    }
}
