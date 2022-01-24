package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Alexander Kiel
 */
class ValueSetCriterionTest {

    public static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "COVID");
    public static final TermCode SEX = TermCode.of("http://loinc.org", "76689-9", "Sex assigned at birth");
    public static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    public static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male", "Male");
    public static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female", "Female");
    public static final TermCode FINDING = TermCode.of("http://snomed.info/sct", "404684003", "Clinical finding (finding)");
    public static final TermCode SEVERE = TermCode.of("http://snomed.info/sct", "24484000", "Severe (severity modifier)");

    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "http://snomed.info/sct", "snomed",
            "http://hl7.org/fhir/administrative-gender", "gender");

    public static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(
            COVID, Mapping.of(COVID, "Observation", "value"),
            SEX, Mapping.of(SEX, "Observation", "value"),
            FINDING, Mapping.of(FINDING, "Condition", "severity")
    ), null, CODE_SYSTEM_ALIASES);

    public static final CodeSystemDefinition LOINC_CODE_SYSTEM_DEF = CodeSystemDefinition.of("loinc",
            "http://loinc.org");
    public static final CodeSystemDefinition SNOMED_CODE_SYSTEM_DEF = CodeSystemDefinition.of("snomed",
            "http://snomed.info/sct");
    public static final CodeSystemDefinition GENDER_CODE_SYSTEM_DEF = CodeSystemDefinition.of("gender",
            "http://hl7.org/fhir/administrative-gender");

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (ValueSetCriterion) mapper.readValue("""
                {
                  "termCodes": [{
                    "system": "http://loinc.org",
                    "code": "76689-9",
                    "display": "Sex assigned at birth"
                  }],
                  "valueFilter": {
                    "type": "concept",
                    "selectedConcepts": [
                      {
                        "system": "http://hl7.org/fhir/administrative-gender",
                        "code": "male",
                        "display": "Male"
                      },
                      {
                        "system": "http://hl7.org/fhir/administrative-gender",
                        "code": "female",
                        "display": "Female"
                      }
                    ]
                  }
                }
                """, Criterion.class);

        assertEquals(Concept.of(SEX), criterion.getConcept());
        assertEquals(List.of(MALE, FEMALE), criterion.getSelectedConcepts());
    }

    @Test
    void toCql_NoConcept() {
        assertThrows(IllegalArgumentException.class, () -> ValueSetCriterion.of(Concept.of(COVID)));
    }

    @Test
    void toCql_OneConcept() {
        var criterion = ValueSetCriterion.of(Concept.of(COVID), POSITIVE);

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '94500-6' from loinc] O
                          where O.value.coding contains Code 'positive' from snomed""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, SNOMED_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_TwoConcepts() {
        var criterion = ValueSetCriterion.of(Concept.of(SEX), MALE, FEMALE);

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '76689-9' from loinc] O
                          where O.value.coding contains Code 'male' from gender or
                            O.value.coding contains Code 'female' from gender""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, GENDER_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_ConditionSeverity() {
        var criterion = ValueSetCriterion.of(Concept.of(FINDING), SEVERE);

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Condition: Code '404684003' from snomed] C
                          where C.severity.coding contains Code '24484000' from snomed""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(SNOMED_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }
}
