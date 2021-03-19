package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class MappingTest {

    public static final TermCode TC_1 = TermCode.of("http://loinc.org", "72166-2", "tabacco smoking status");

    @Test
    void fromJson_OneInclusionCriteria() throws Exception {
        var mapper = new ObjectMapper();

        var mapping = mapper.readValue("""
                {"fhirResourceType": "Observation",
                 "termCode": {
                   "system": "http://loinc.org",
                   "code": "72166-2",
                   "display": "tabacco smoking status"
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
                {"foo-153729": "bar-153733",
                 "fhirResourceType": "Observation",
                 "termCode": {
                   "system": "http://loinc.org",
                   "code": "72166-2",
                   "display": "tabacco smoking status"
                 }  
                }
                """, Mapping.class);

        assertEquals(TC_1, mapping.getConcept());
        assertEquals("Observation", mapping.getResourceType());
    }
}
