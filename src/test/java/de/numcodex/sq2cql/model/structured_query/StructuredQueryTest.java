package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class StructuredQueryTest {

    public static final TermCode TC_1 = TermCode.of("tc", "1", "");
    public static final TermCode TC_2 = TermCode.of("tc", "2", "");

    @Test
    void fromJson_OneInclusionCriteria() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCode": {
                    "system": "tc", 
                    "code": "1",
                    "display": ""
                  }
                }]]}
                """, StructuredQuery.class);

        assertEquals(TC_1, ((ConceptCriterion) structuredQuery.getInclusionCriteria().get(0).get(0)).getTermCode());
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"foo-151633": "bar-151639",
                 "inclusionCriteria": [[{
                  "termCode": {
                    "system": "tc", 
                    "code": "1",
                    "display": ""
                  }
                }]]}
                """, StructuredQuery.class);

        assertEquals(TC_1, ((ConceptCriterion) structuredQuery.getInclusionCriteria().get(0).get(0)).getTermCode());
    }

    @Test
    void fromJson_TwoInclusionCriteriaAnd() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCode": {
                    "system": "tc", 
                    "code": "1",
                    "display": ""
                  }
                }], [{
                  "termCode": {
                    "system": "tc", 
                    "code": "2",
                    "display": ""
                  }
                }]]}
                """, StructuredQuery.class);

        assertEquals(TC_1, ((ConceptCriterion) structuredQuery.getInclusionCriteria().get(0).get(0)).getTermCode());
        assertEquals(TC_2, ((ConceptCriterion) structuredQuery.getInclusionCriteria().get(1).get(0)).getTermCode());
    }

    @Test
    void fromJson_TwoInclusionCriteriaOr() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCode": {
                    "system": "tc", 
                    "code": "1",
                    "display": ""
                  }
                }, {
                  "termCode": {
                    "system": "tc", 
                    "code": "2",
                    "display": ""
                  }
                }]]}
                """, StructuredQuery.class);

        assertEquals(TC_1, ((ConceptCriterion) structuredQuery.getInclusionCriteria().get(0).get(0)).getTermCode());
        assertEquals(TC_2, ((ConceptCriterion) structuredQuery.getInclusionCriteria().get(0).get(1)).getTermCode());
    }

    @Test
    void fromJson_OneInclusionCriteria_OneExclusionCriteria() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCode": {
                    "system": "tc", 
                    "code": "1",
                    "display": ""
                  }
                }]], "exclusionCriteria": [[{
                  "termCode": {
                    "system": "tc", 
                    "code": "2",
                    "display": ""
                  }
                }]]}
                """, StructuredQuery.class);

        assertEquals(TC_1, ((ConceptCriterion) structuredQuery.getInclusionCriteria().get(0).get(0)).getTermCode());
        assertEquals(TC_2, ((ConceptCriterion) structuredQuery.getExclusionCriteria().get(0).get(0)).getTermCode());
    }
}
