package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.CodeModifier;
import de.numcodex.sq2cql.model.structured_query.CodingModifier;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class MappingTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode TC_1 = ContextualTermCode.of(CONTEXT, TermCode.of("http://loinc.org", "72166-2", "tabacco smoking status"));
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    private static final TermCode TNM_T = TermCode.of("http://loinc.org", "21899-0", "Primary tumor.pathology Cancer");

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "resource_type": "Observation",
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "key": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  },
                  "valueFhirPath": "value"
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.key());
        assertEquals("Observation", mapping.resourceType());
        assertEquals("value", mapping.valueFhirPath());
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "foo-153729": "bar-153733",
                  "resource_type": "Observation",
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "key": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  },
                  "valueFhirPath": "value"
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.key());
        assertEquals("Observation", mapping.resourceType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"value", "other"})
    void fromJson_WithValueFhirPath(String valueFhirPath) throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue(String.format("""
                {
                  "resource_type": "Observation",
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "key": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  },
                  "valueFhirPath": "%s"
                }
                """, valueFhirPath), Mapping.class);

        assertEquals(TC_1, mapping.key());
        assertEquals("Observation", mapping.resourceType());
        assertEquals(valueFhirPath, mapping.valueFhirPath());
    }

    @Test
    void fromJson_WithFixedCriteria_Code() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "resource_type": "Observation",
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "key": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  },
                  "fixedCriteria": [
                    {
                      "type": "code",
                      "searchParameter": "status",
                      "fhirPath": "status",
                      "value": [ {
                          "code": "completed",
                          "system": "http://hl7.org/fhir/report-status-codes",
                          "display": "completed"
                        },
                        {
                          "code": "in-progress",
                          "system": "http://hl7.org/fhir/report-status-codes",
                          "display": "in-progress"
                        }
                      ]
                    }
                  ]
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.key());
        assertEquals("Observation", mapping.resourceType());
        assertEquals(CodeModifier.of("status", "completed", "in-progress"), mapping.fixedCriteria().get(0));
    }

    @Test
    void fromJson_WithFixedCriteria_Coding() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "resource_type": "Observation",
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "key": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  },
                  "fixedCriteria": [
                    {
                      "type": "coding",
                      "searchParameter": "verification-status",
                      "fhirPath": "verificationStatus",
                      "value": [
                        {
                          "system": "http://terminology.hl7.org/CodeSystem/condition-ver-status",
                          "code": "confirmed",
                          "display": "Confirmed"
                        }
                      ]
                    }
                  ]
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.key());
        assertEquals("Observation", mapping.resourceType());
        assertEquals(CodingModifier.of("verificationStatus", CONFIRMED), mapping.fixedCriteria().get(0));
    }

    @Test
    void fromJson_WithAttributeMapping() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "resource_type": "Observation",
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "key": {
                    "system": "http://loinc.org",
                    "code": "21908-9",
                    "display": "Stage group.clinical Cancer"
                  },
                  "attributeSearchParameters": [
                    {
                      "attributeType": "coding",
                      "attributeKey": {
                        "system": "http://loinc.org",
                        "code": "21899-0",
                        "display": "Primary tumor.pathology Cancer"
                      },
                      "attributeFhirPath": "component.where(code.coding.exists(system = 'http://loinc.org' and code = '21899-0')).value.first()"
                    }
                  ]
                }
                """, Mapping.class);

        assertThat(mapping.attributeMappings()).containsEntry(TNM_T, AttributeMapping.of("coding", TNM_T,
                "component.where(code.coding.exists(system = 'http://loinc.org' and code = '21899-0')).value.first()"));
    }

    @Test
    void fromJson_WithTimeRestriction() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "resource_type": "Observation",
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "key": {
                    "system": "http://loinc.org",
                    "code": "21908-9",
                    "display": "Stage group.clinical Cancer"
                  },
                  "timeRestrictionPath": "effective",
                  "valueFhirPath": "value"
                }
                """, Mapping.class);

        assertThat(mapping.timeRestrictionPath()).contains("effective");
    }
}
