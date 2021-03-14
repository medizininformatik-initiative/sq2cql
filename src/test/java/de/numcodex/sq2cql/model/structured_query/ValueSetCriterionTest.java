package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.ConceptNode;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.numcodex.sq2cql.model.common.Comparator.GREATER_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class ValueSetCriterionTest {

    public static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "COVID");
    public static final TermCode SEX = TermCode.of("http://loinc.org", "76689-9", "Sex assigned at birth");
    public static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    public static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male", "Male");
    public static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female", "Female");

    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "http://snomed.info/sct", "snomed",
            "http://hl7.org/fhir/administrative-gender", "gender");

    public static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(COVID, Mapping.of(COVID, "Observation"),
            SEX, Mapping.of(SEX, "Observation")), ConceptNode.of(), CODE_SYSTEM_ALIASES);

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
                  "termCode": {
                    "system": "http://loinc.org", 
                    "code": "76689-9",
                    "display": "Sex assigned at birth"
                  },
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

        assertEquals(SEX, criterion.getTermCode());
        assertEquals(List.of(MALE, FEMALE), criterion.getSelectedConcepts());
    }

    @Test
    void toCql_OneConcept() {
        Criterion criterion = ValueSetCriterion.of(COVID, List.of(POSITIVE));

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '94500-6' from loinc] O
                          where O.value.coding contains Code 'positive' from snomed""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, SNOMED_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_TwoConcepts() {
        Criterion criterion = ValueSetCriterion.of(SEX, List.of(MALE, FEMALE));

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '76689-9' from loinc] O
                          where O.value.coding contains Code 'male' from gender or
                          O.value.coding contains Code 'female' from gender""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, GENDER_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }
}
