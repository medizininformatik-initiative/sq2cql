package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Alexander Kiel
 */
class StructuredQueryTest {

    static final TermCode TC_1 = TermCode.of("tc", "1", "");
    static final TermCode TC_2 = TermCode.of("tc", "2", "");

    @Test
    void fromJson_NoInclusionCriteria() {
        var mapper = new ObjectMapper();

        assertThrows(ValueInstantiationException.class, () -> mapper.readValue("""
                {"inclusionCriteria": [[]]}
                """, StructuredQuery.class));
    }

    @Test
    void fromJson_OneInclusionCriteria() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCodes": [{
                    "system": "tc",
                    "code": "1",
                    "display": ""
                  }]
                }]]}
                """, StructuredQuery.class);

        assertEquals(Concept.of(TC_1),
                ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"foo-151633": "bar-151639",
                 "inclusionCriteria": [[{
                  "termCodes": [{
                    "system": "tc",
                    "code": "1",
                    "display": ""
                  }]
                }]]}
                """, StructuredQuery.class);

        assertEquals(Concept.of(TC_1),
                ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
    }

    @Test
    void fromJson_TwoInclusionCriteriaAnd() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCodes": [{
                    "system": "tc",
                    "code": "1",
                    "display": ""
                  }]
                }], [{
                  "termCodes": [{
                    "system": "tc",
                    "code": "2",
                    "display": ""
                  }]
                }]]}
                """, StructuredQuery.class);

        assertEquals(Concept.of(TC_1),
                ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
        assertEquals(Concept.of(TC_2),
                ((ConceptCriterion) structuredQuery.inclusionCriteria().get(1).get(0)).getConcept());
    }

    @Test
    void fromJson_TwoInclusionCriteriaOr() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCodes": [{
                    "system": "tc",
                    "code": "1",
                    "display": ""
                  }]
                }, {
                  "termCodes": [{
                    "system": "tc",
                    "code": "2",
                    "display": ""
                  }]
                }]]}
                """, StructuredQuery.class);

        assertEquals(Concept.of(TC_1),
                ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
        assertEquals(Concept.of(TC_2),
                ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(1)).getConcept());
    }

    @Test
    void fromJson_OneInclusionCriteria_OneExclusionCriteria() throws Exception {
        var mapper = new ObjectMapper();

        var structuredQuery = mapper.readValue("""
                {"inclusionCriteria": [[{
                  "termCodes": [{
                    "system": "tc",
                    "code": "1",
                    "display": ""
                  }]
                }]], "exclusionCriteria": [[{
                  "termCodes": [{
                    "system": "tc",
                    "code": "2",
                    "display": ""
                  }]
                }]]}
                """, StructuredQuery.class);

        assertEquals(Concept.of(TC_1),
                ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
        assertEquals(Concept.of(TC_2),
                ((ConceptCriterion) structuredQuery.exclusionCriteria().get(0).get(0)).getConcept());
    }
}
