package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Alexander Kiel
 */
class StructuredQueryTest {

  static final TermCode CONTEXT = TermCode.of("context", "context", "context");
  static final TermCode TC_1_TC = TermCode.of("tc", "1", "");
  static final TermCode TC_2_TC = TermCode.of("tc", "2", "");
  static final ContextualTermCode TC_1 = ContextualTermCode.of(CONTEXT, TC_1_TC);
  static final ContextualTermCode TC_2 = ContextualTermCode.of(CONTEXT, TC_2_TC);


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
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "1",
            "display": ""
          }]
        }]]}
        """, StructuredQuery.class);

    assertEquals(ContextualConcept.of(TC_1),
        ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
  }

  @Test
  void fromJson_AdditionalPropertyIsIgnored() throws Exception {
    var mapper = new ObjectMapper();

    var structuredQuery = mapper.readValue("""
        {"foo-151633": "bar-151639",
         "inclusionCriteria": [[{
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "1",
            "display": ""
          }]
        }]]}
        """, StructuredQuery.class);

    assertEquals(ContextualConcept.of(TC_1),
        ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
  }

  @Test
  void fromJson_TwoInclusionCriteriaAnd() throws Exception {
    var mapper = new ObjectMapper();

    var structuredQuery = mapper.readValue("""
        {"inclusionCriteria": [[{
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "1",
            "display": ""
          }]
        }], [{
          "context": {
              "system": "context",
              "code": "context",
              "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "2",
            "display": ""
          }]
        }]]}
        """, StructuredQuery.class);

    assertEquals(ContextualConcept.of(TC_1),
        ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
    assertEquals(ContextualConcept.of(TC_2),
        ((ConceptCriterion) structuredQuery.inclusionCriteria().get(1).get(0)).getConcept());
  }

  @Test
  void fromJson_TwoInclusionCriteriaOr() throws Exception {
    var mapper = new ObjectMapper();

    var structuredQuery = mapper.readValue("""
        {"inclusionCriteria": [[{
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "1",
            "display": ""
          }]
        }, {
          "context": {
              "system": "context",
              "code": "context",
              "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "2",
            "display": ""
          }]
        }]]}
        """, StructuredQuery.class);

    assertEquals(ContextualConcept.of(TC_1),
        ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
    assertEquals(ContextualConcept.of(TC_2),
        ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(1)).getConcept());
  }

  @Test
  void fromJson_OneInclusionCriteria_OneExclusionCriteria() throws Exception {
    var mapper = new ObjectMapper();

    var structuredQuery = mapper.readValue("""
        {"inclusionCriteria": [[{
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "1",
            "display": ""
          }]
        }]], "exclusionCriteria": [[{
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "tc",
            "code": "2",
            "display": ""
          }]
        }]]}
        """, StructuredQuery.class);

    assertEquals(ContextualConcept.of(TC_1),
        ((ConceptCriterion) structuredQuery.inclusionCriteria().get(0).get(0)).getConcept());
    assertEquals(ContextualConcept.of(TC_2),
        ((ConceptCriterion) structuredQuery.exclusionCriteria().get(0).get(0)).getConcept());
  }

  @Test
  void fromJson_EmptyTimeRestriction() throws Exception {
    var mapper = new ObjectMapper();

    var structuredQuery = mapper.readValue("""
        {
          "version": "http://to_be_decided.com/draft-1/schema#",
          "display": "",
          "inclusionCriteria": [
            [
              {
                 "context": {
                  "system": "context",
                  "code": "context",
                  "display": "context"
                },
                "termCodes": [
                  {
                    "code": "Q50",
                    "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                    "version": "2021",
                    "display": "Angeborene Fehlbildungen der Ovarien, der Tubae uterinae und der Ligg. lata uteri"
                  }
                ],
                "attributeFilters": [],
                "timeRestriction": {}
              }
            ]
          ]
        }
        """, StructuredQuery.class);

    AbstractCriterion criterion = (AbstractCriterion) structuredQuery.inclusionCriteria().get(0)
        .get(0);
    assertThat(criterion.timeRestriction()).isNull();
  }
}
