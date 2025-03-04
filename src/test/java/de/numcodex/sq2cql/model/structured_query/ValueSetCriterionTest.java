package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static de.numcodex.sq2cql.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alexander Kiel
 */
class ValueSetCriterionTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode COVID = ContextualTermCode.of(CONTEXT, TermCode.of("http://loinc.org", "94500-6", "COVID"));
    static final ContextualTermCode SEX = ContextualTermCode.of(CONTEXT, TermCode.of("http://loinc.org", "76689-9", "Sex assigned at birth"));
    static final TermCode POSITIVE = TermCode.of("http://snomed.info/sct", "positive", "positive");
    static final TermCode MALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "male", "Male");
    static final TermCode FEMALE = TermCode.of("http://hl7.org/fhir/administrative-gender", "female", "Female");
    static final ContextualTermCode FINDING = ContextualTermCode.of(CONTEXT, TermCode.of("http://snomed.info/sct", "404684003", "Clinical finding (finding)"));
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct", "24484000", "Severe (severity modifier)");
    static final TermCode TNM_C_TC = TermCode.of("http://loinc.org", "21908-9", "Tumor size.clinical Cancer");
    static final TermCode TNM_P_TC = TermCode.of("http://loinc.org", "21902-2", "Tumor size.pathology Cancer");
    static final ContextualTermCode TNM_C = ContextualTermCode.of(CONTEXT, TNM_C_TC);
    static final ContextualTermCode TNM_P = ContextualTermCode.of(CONTEXT, TNM_P_TC);
    static final MappingContext MAPPING_CONTEXT;
    static final TermCode LA3649_6 = TermCode.of("http://loinc.org", "LA3649-6", "Stage IVB");
    static final TermCode STATUS = TermCode.of("http://hl7.org/fhir", "observation-status", "observation-status");
    static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final", "final");
    static final ContextualTermCode ETHNIC_GROUP = ContextualTermCode.of(CONTEXT, TermCode.of("http://snomed.info/sct", "372148003", "Ethnic group (ethnic group)"));
    static final TermCode MIXED = TermCode.of("http://snomed.info/sct", "26242008",
            "Mixed (qualifier value)");
    static final ContextualTermCode GENDER = ContextualTermCode.of(CONTEXT, TermCode.of("http://snomed.info/sct", "263495000", "Gender"));
    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "http://snomed.info/sct", "snomed",
            "http://hl7.org/fhir/administrative-gender", "gender");

    static {
        MAPPING_CONTEXT = MappingContext.of(Map.of(
                COVID, Mapping.of(COVID, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.CODEABLE_CONCEPT), List.of(),
                        List.of(AttributeMapping.of(List.of("code"), STATUS, "status"))),
                SEX, Mapping.of(SEX, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.CODEABLE_CONCEPT)),
                FINDING, Mapping.of(FINDING, "Condition", Mapping.PathMapping.of("severity", Mapping.PathMapping.Type.CODEABLE_CONCEPT)),
                TNM_C, Mapping.of(TNM_C, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.CODEABLE_CONCEPT)),
                TNM_P, Mapping.of(TNM_P, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.CODEABLE_CONCEPT))
        ), null, CODE_SYSTEM_ALIASES);
    }

    @Test
    void fromJson_WithTwoSelectedConcepts() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (ValueSetCriterion) mapper.readValue("""
                {
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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

        assertEquals(ContextualConcept.of(SEX), criterion.getConcept());
        assertEquals(List.of(MALE, FEMALE), criterion.getSelectedConcepts());
    }

    @Test
    void fromJson_WithMissingSelectedConcepts() {
        var mapper = new ObjectMapper();

        var error = assertThrows(JsonMappingException.class, () -> mapper.readValue("""
                {
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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

        assertEquals(ContextualConcept.of(SEX), criterion.getConcept());
        assertEquals(List.of(MALE), criterion.getSelectedConcepts());
        assertEquals(List.of(ValueSetAttributeFilter.of(STATUS, FINAL)), criterion.attributeFilters);
    }

    @Test
    void fromJson_WithAttributeFilterAndMissingSelectedConcepts() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (ValueSetCriterion) mapper.readValue("""
                {
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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
        assertThrows(IllegalArgumentException.class, () -> ValueSetCriterion.of(ContextualConcept.of(COVID)));
    }

    @Test
    void toCql_WithOneConcept() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(COVID), POSITIVE);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem loinc: 'http://loinc.org'
                codesystem snomed: 'http://snomed.info/sct'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code '94500-6' from loinc] O
                    where O.value ~ Code 'positive' from snomed)
                """);
    }

    @Test
    void toCql_WithOneConceptAndMultipleTermCodes() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(CONTEXT, Concept.of(TNM_C_TC, TNM_P_TC)), LA3649_6);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem loinc: 'http://loinc.org'
                                                    
                context Patient
                                
                define Criterion:
                  exists (from [Observation: Code '21908-9' from loinc] O
                    where O.value ~ Code 'LA3649-6' from loinc) or
                  exists (from [Observation: Code '21902-2' from loinc] O
                    where O.value ~ Code 'LA3649-6' from loinc)
                """);
    }

    @Test
    void toCql_WithTwoConcepts() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(SEX), MALE, FEMALE);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                         
                codesystem gender: 'http://hl7.org/fhir/administrative-gender'
                codesystem loinc: 'http://loinc.org'
                                                    
                context Patient
                                
                define Criterion:
                  exists (from [Observation: Code '76689-9' from loinc] O
                    where O.value ~ Code 'male' from gender or
                      O.value ~ Code 'female' from gender)
                """);
    }

    @Test
    void toCql_WithConditionSeverity() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(FINDING), SEVERE);

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                         
                codesystem snomed: 'http://snomed.info/sct'
                                                    
                context Patient
                                
                define Criterion:
                  exists (from [Condition: Code '404684003' from snomed] C
                    where C.severity ~ Code '24484000' from snomed)
                """);
    }

    @Test
    void toCql_WithAttributeFilter() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(COVID), POSITIVE)
                .appendAttributeFilter(ValueSetAttributeFilter.of(STATUS, FINAL));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                         
                codesystem loinc: 'http://loinc.org'
                codesystem snomed: 'http://snomed.info/sct'
                                                    
                context Patient
                                
                define Criterion:
                  exists (from [Observation: Code '94500-6' from loinc] O
                    where O.value ~ Code 'positive' from snomed and
                      O.status = 'final')
                """);
    }

    @Test
    void toCql_WithFixedCriteria() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(COVID), POSITIVE);
        var mappingContext = MappingContext.of(Map.of(
                COVID, Mapping.of(COVID, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.CODEABLE_CONCEPT),
                        List.of(CodeModifier.of("status", "final")),
                        List.of())
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                    
                codesystem loinc: 'http://loinc.org'
                codesystem snomed: 'http://snomed.info/sct'
                  
                context Patient
                                
                define Criterion:
                  exists (from [Observation: Code '94500-6' from loinc] O
                    where O.value ~ Code 'positive' from snomed and
                      O.status = 'final')
                """);
    }

    @Test
    void toCql_WithCodingValueTypeOnPatient() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(ETHNIC_GROUP), MIXED);
        var mappingContext = MappingContext.of(Map.of(
                ETHNIC_GROUP, Mapping.of(ETHNIC_GROUP, "Patient",
                        Mapping.PathMapping.of("extension.where(url='https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group').value.first()",
                        Mapping.PathMapping.Type.CODING))
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                         
                codesystem snomed: 'http://snomed.info/sct'
                                                    
                context Patient
                                
                define Criterion:
                  Patient.extension.where(url='https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group').value.first() ~ Code '26242008' from snomed
                """);
    }

    @Test
    void toCql_WithPatientGender() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(GENDER), MALE);
        var mappingContext = MappingContext.of(Map.of(
                GENDER, Mapping.of(GENDER, "Patient", Mapping.PathMapping.of("gender", Mapping.PathMapping.Type.CODE))
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                         
                context Patient
                                
                define Criterion:
                  Patient.gender = 'male'
                """);
    }

    @Test
    void toCql_WithPatientGender_TwoConcepts() {
        var criterion = ValueSetCriterion.of(ContextualConcept.of(GENDER), MALE, FEMALE);
        var mappingContext = MappingContext.of(Map.of(
                GENDER, Mapping.of(GENDER, "Patient", Mapping.PathMapping.of("gender", Mapping.PathMapping.Type.CODE))
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                         
                context Patient
                                
                define Criterion:
                  Patient.gender = 'male' or
                  Patient.gender = 'female'
                """);
    }
}
