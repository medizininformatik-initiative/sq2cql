package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.CodeModifier;
import de.numcodex.sq2cql.model.structured_query.CodingModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class MappingTest {

    public static final TermCode TC_1 = TermCode.of("http://loinc.org", "72166-2", "tabacco smoking status");

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "fhirResourceType": "Observation",
                  "termCode": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  }  
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.getConcept());
        assertEquals("Observation", mapping.getResourceType());
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "foo-153729": "bar-153733",
                  "fhirResourceType": "Observation",
                  "termCode": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  }  
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.getConcept());
        assertEquals("Observation", mapping.getResourceType());
    }

    @Test
    void fromJson_WithFixedCriteria_Code() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "fhirResourceType": "Observation",
                  "termCode": {
                    "system": "http://loinc.org",
                    "code": "72166-2",
                    "display": "tobacco smoking status"
                  },
                  "fixedCriteria": [
                    {
                      "type": "code",
                      "searchParameter": "status",
                      "fhirPath": "status",
                      "value": [
                        "completed",
                        "in-progress"
                      ]
                    }
                  ]  
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.getConcept());
        assertEquals("Observation", mapping.getResourceType());
        assertEquals(CodeModifier.of("status", "completed", "in-progress"), mapping.getFixedCriteria().get(0));
    }

    @Test
    void fromJson_WithFixedCriteria_Coding() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {
                  "fhirResourceType": "Observation",
                  "termCode": {
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

        assertEquals(TC_1, mapping.getConcept());
        assertEquals("Observation", mapping.getResourceType());
        assertEquals(CodingModifier.of("verificationStatus",
                TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed")),
                mapping.getFixedCriteria().get(0));
    }
}
