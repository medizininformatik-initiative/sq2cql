package com.codex.sq2cql.data_model.cql.translator;

import com.codex.sq2cql.data_model.common.Comparator;
import com.codex.sq2cql.data_model.cql.*;
import com.codex.sq2cql.data_model.structured_query.Criterion;
import com.codex.sq2cql.data_model.structured_query.NumericCriterion;
import com.codex.sq2cql.data_model.structured_query.RangeCriterion;
import com.codex.sq2cql.data_model.structured_query.ValueSetCriterion;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class CriterionTranslator {
    //TODO: This is not a fixed map and should it be here?
    private static HashMap<String, String> codesystemNaming = new HashMap<>() {{
       put("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
       put("https://loinc.org", "loinc");
    }};



    public static Query translateCriterion(NumericCriterion numericCriterion) {
        var retrieveExpression = new RetrieveExpression(new NamedTypeSpecifier(ConceptMapper.getConcept(numericCriterion.getTermCode())),
                new Terminology(new CodeSelector(numericCriterion.getTermCode().getCode(), codesystemNaming.get(numericCriterion.getTermCode().getSystem()))));
        var sourceClause = new SourceClause(retrieveExpression);
        var castExpression = new TypeExpression(new LiteralExpression("O.value"), "Quantity");
        var value = new NumericExpression(numericCriterion.getValue(), numericCriterion.getUnit());
        var comparatorExpression = new ComparatorExpression(castExpression, numericCriterion.getComparator(), value);
        var whereClause = new WhereClause(comparatorExpression);
        return new Query(sourceClause, whereClause);
    }

    public static OrExpression translateCriterion(ValueSetCriterion valueSetCriterion) {
        var conceptExpression = new LiteralExpression(ConceptMapper.getConcept(valueSetCriterion.getTermCode()));
        var valueSetExpression = new OrExpression();
        for (var value : valueSetCriterion.getValues()) {
            var valueExpression = new LiteralExpression(value);
            var equalsExpression = new ComparatorExpression(conceptExpression, Comparator.EQUAL, valueExpression);
            valueSetExpression.addExpression(equalsExpression);
        }
        return valueSetExpression;
    }

    public static ExistExpression translateCriterion(Criterion criterion) {
        var conceptExpression = new RetrieveExpression(new NamedTypeSpecifier(ConceptMapper.getConcept(criterion.getTermCode())),
                new Terminology(new CodeSelector(criterion.getTermCode().getCode(), codesystemNaming.get(criterion.getTermCode().getSystem()))));
        return new ExistExpression(conceptExpression);
    }

    public static AndExpression translateCriterion(RangeCriterion rangeCriterion) {
        var numericConcept = new LiteralExpression(ConceptMapper.getConcept(rangeCriterion.getTermCode()));
        var minValue = new NumericExpression(rangeCriterion.getMinValue(), rangeCriterion.getUnit());
        var maxValue = new NumericExpression(rangeCriterion.getMaxValue(), rangeCriterion.getUnit());

        var greaterEqualExpression = new ComparatorExpression(numericConcept, Comparator.GREATER_EQUAL, minValue);
        var smallerEqualExpression = new ComparatorExpression(numericConcept, Comparator.LESS_EQUAL, maxValue);

        return new AndExpression(List.of(greaterEqualExpression, smallerEqualExpression));
    }

}
