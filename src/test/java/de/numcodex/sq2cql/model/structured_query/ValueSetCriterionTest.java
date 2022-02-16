package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alexander Kiel
 */
class ValueSetCriterionTest {

    static final TermCode COVID = TermCode.of("http://loinc.org", "94500-6", "COVID");
    static final TermCode SEX = TermCode.of("http://loinc.org", "76689-9", "Sex assigned at birth");
    static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male", "Male");
    static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female", "Female");
    static final TermCode FINDING = TermCode.of("http://snomed.info/sct", "404684003", "Clinical finding (finding)");
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct", "24484000", "Severe (severity modifier)");
    static final TermCode TNM_C = TermCode.of("http://loinc.org", "21908-9", "Stage group.clinical Cancer");
    static final TermCode TNM_P = TermCode.of("http://loinc.org", "21902-2", "Stage group.pathology Cancer");
    static final TermCode LA3649_6 = TermCode.of("http://loinc.org", "LA3649-6", "Stage IVB");
    static final TermCode STATUS = TermCode.of("http://hl7.org/fhir", "observation-status", "observation-status");
    static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final", "final");

    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "http://snomed.info/sct", "snomed",
            "http://hl7.org/fhir/administrative-gender", "gender");

    static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(
            COVID, Mapping.of(COVID, "Observation", "value", List.of(),
                    List.of(AttributeMapping.of("code", STATUS, "status"))),
            SEX, Mapping.of(SEX, "Observation", "value"),
            FINDING, Mapping.of(FINDING, "Condition", "severity"),
            TNM_C, Mapping.of(TNM_C, "Observation", "value"),
            TNM_P, Mapping.of(TNM_P, "Observation", "value")
    ), null, CODE_SYSTEM_ALIASES);

    static final CodeSystemDefinition LOINC_CODE_SYSTEM_DEF = CodeSystemDefinition.of("loinc",
            "http://loinc.org");
    static final CodeSystemDefinition SNOMED_CODE_SYSTEM_DEF = CodeSystemDefinition.of("snomed",
            "http://snomed.info/sct");
    static final CodeSystemDefinition GENDER_CODE_SYSTEM_DEF = CodeSystemDefinition.of("gender",
            "http://hl7.org/fhir/administrative-gender");

    @Test
    void fromJson_WithTwoSelectedConcepts() throws Exception {
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
    void fromJson_WithMissingSelectedConcepts() {
        var mapper = new ObjectMapper();

        var error = assertThrows(JsonMappingException.class, () -> mapper.readValue("""
                {
                  "termCodes": [{
                    "system": "http://loinc.org",
                    "code": "76689-9",
                    "display": "Sex assigned at birth"
                  }],
                  "valueFilter": {
                    "type": "concept"
                  }
                }
                """, Criterion.class));

        assertTrue(error.getMessage().startsWith("Cannot construct instance of `de.numcodex.sq2cql.model.structured_query.Criterion`"));
    }

    @Test
    void fromJson_WithEmptySelectedConcepts() {
        var mapper = new ObjectMapper();

        var error = assertThrows(JsonMappingException.class, () -> mapper.readValue("""
                {
                  "termCodes": [{
                    "system": "http://loinc.org",
                    "code": "76689-9",
                    "display": "Sex assigned at birth"
                  }],
                  "valueFilter": {
                    "type": "concept",
                    "selectedConcepts": []
                  }
                }
                """, Criterion.class));

        assertTrue(error.getMessage().startsWith("Cannot construct instance of `de.numcodex.sq2cql.model.structured_query.Criterion`"));
    }

    @Test
    void fromJson_WithAttributeFilter() throws Exception {
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
                      }
                    ]
                  },
                  "attributeFilters": [
                    {
                      "attributeCode": {
                        "system": "http://hl7.org/fhir",
                        "code": "observation-status",
                        "display": "observation-status"
                      },
                      "type": "concept",
                      "selectedConcepts": [
                        {
                          "system": "http://hl7.org/fhir/observation-status",
                          "code": "final",
                          "display": "final"
                        }
                      ]
                    }
                  ]
                }
                """, Criterion.class);

        assertEquals(Concept.of(SEX), criterion.getConcept());
        assertEquals(List.of(MALE), criterion.getSelectedConcepts());
        assertEquals(List.of(ValueSetAttributeFilter.of(STATUS, FINAL)), criterion.attributeFilters);
    }

    @Test
    void fromJson_WithAttributeFilterAndMissingSelectedConcepts() throws Exception {
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
                      }
                    ]
                  },
                  "attributeFilters": [
                    {
                      "attributeCode": {
                        "system": "http://hl7.org/fhir",
                        "code": "observation-status",
                        "display": "observation-status"
                      },
                      "type": "concept"
                    }
                  ]
                }
                """, Criterion.class);

        assertTrue(criterion.attributeFilters.isEmpty());
    }

    @Test
    void fromJson_WithAttributeFilterAndEmptySelectedConcepts() throws Exception {
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
                      }
                    ]
                  },
                  "attributeFilters": [
                    {
                      "attributeCode": {
                        "system": "http://hl7.org/fhir",
                        "code": "observation-status",
                        "display": "observation-status"
                      },
                      "type": "concept",
                      "selectedConcepts": []
                    }
                  ]
                }
                """, Criterion.class);

        assertTrue(criterion.attributeFilters.isEmpty());
    }

    @Test
    void toCql_WithNoConcept() {
        assertThrows(IllegalArgumentException.class, () -> ValueSetCriterion.of(Concept.of(COVID)));
    }

    @Test
    void toCql_WithOneConcept() {
        var criterion = ValueSetCriterion.of(Concept.of(COVID), POSITIVE);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '94500-6' from loinc] O
                          where O.value.coding contains Code 'positive' from snomed""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, SNOMED_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithOneConceptAndMultipleTermCodes() {
        var criterion = ValueSetCriterion.of(Concept.of(TNM_C, TNM_P), LA3649_6);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '21908-9' from loinc] O
                          where O.value.coding contains Code 'LA3649-6' from loinc or
                        exists from [Observation: Code '21902-2' from loinc] O
                          where O.value.coding contains Code 'LA3649-6' from loinc""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithTwoConcepts() {
        var criterion = ValueSetCriterion.of(Concept.of(SEX), MALE, FEMALE);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '76689-9' from loinc] O
                          where O.value.coding contains Code 'male' from gender or
                            O.value.coding contains Code 'female' from gender""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, GENDER_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithConditionSeverity() {
        var criterion = ValueSetCriterion.of(Concept.of(FINDING), SEVERE);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Condition: Code '404684003' from snomed] C
                          where C.severity.coding contains Code '24484000' from snomed""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(SNOMED_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithAttributeFilter() {
        var criterion = ValueSetCriterion.of(Concept.of(COVID), List.of(POSITIVE),
                ValueSetAttributeFilter.of(STATUS, FINAL));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '94500-6' from loinc] O
                          where O.value.coding contains Code 'positive' from snomed and
                            O.status = 'final'""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, SNOMED_CODE_SYSTEM_DEF),
                container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithFixedCriteria() {
        var criterion = ValueSetCriterion.of(Concept.of(COVID), List.of(POSITIVE));
        var mappingContext = MappingContext.of(Map.of(
                COVID, Mapping.of(COVID, "Observation", "value", List.of(CodeModifier.of("status", "final")),
                        List.of())
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertEquals("""
                        exists from [Observation: Code '94500-6' from loinc] O
                          where O.value.coding contains Code 'positive' from snomed and
                            O.status = 'final'""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF, SNOMED_CODE_SYSTEM_DEF),
                container.getCodeSystemDefinitions());
    }
}
