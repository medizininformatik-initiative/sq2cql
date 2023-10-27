package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class ConceptCriterionTest {

  static final TermCode CONTEXT = TermCode.of("context", "context", "context");
  static final ContextualTermCode C71 = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
          "Malignant neoplasm of brain"));
  static final TermCode C71_1_TC = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
      "Frontal lobe");
  static final ContextualTermCode C71_1 = ContextualTermCode.of(CONTEXT, C71_1_TC);
  static final TermCode C71_2_TC = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.2",
      "Temporal lobe");
  static final ContextualTermCode C71_2 = ContextualTermCode.of(CONTEXT, C71_2_TC);
  static final ContextualTermCode BLOOD_PRESSURE = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://loinc.org", "85354-9",
          "Blood pressure panel with all children optional"));
  static final TermCode DIASTOLIC_BLOOD_PRESSURE = TermCode.of("http://loinc.org", "8462-4",
      "Diastolic blood pressure");
  static final TermCode CONFIRMED = TermCode.of(
      "http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed");
  static final ContextualTermCode THERAPEUTIC_PROCEDURE = ContextualTermCode.of(CONTEXT,
      TermCode.of("http://snomed.info/sct", "277132007", "Therapeutic procedure (procedure)"));
  static final CodeSystemDefinition ICD10_CODE_SYSTEM_DEF = CodeSystemDefinition.of("icd10",
      "http://fhir.de/CodeSystem/bfarm/icd-10-gm");
  static final CodeSystemDefinition SNOMED_CODE_SYSTEM_DEF = CodeSystemDefinition.of("snomed",
      "http://snomed.info/sct");
  static final CodeSystemDefinition VER_STATUS_CODE_SYSTEM_DEF = CodeSystemDefinition.of(
      "ver_status", "http://terminology.hl7.org/CodeSystem/condition-ver-status");
  static final TermCode VERIFICATION_STATUS = TermCode.of("hl7.org", "verificationStatus",
      "verificationStatus");

  static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
      "http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10", "http://snomed.info/sct", "snomed",
      "http://loinc.org", "loinc", "http://terminology.hl7.org/CodeSystem/condition-ver-status",
      "ver_status");

  static final CodeSystemDefinition LOINC_CODE_SYSTEM_DEF = CodeSystemDefinition.of("loinc",
      "http://loinc.org");

  @Test
  void fromJson() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71",
            "display": "Malignant neoplasm of brain"
          }]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(C71), criterion.getConcept());
  }

  @Test
  void fromJson_WithMultipleTermCodes() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71.1",
            "display": "Frontal lobe"
          }, {
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71.2",
            "display": "Temporal lobe"
          }]
        }
        """, Criterion.class);

    assertEquals(List.of(C71_1, C71_2), criterion.getConcept().contextualTermCodes());
  }

  @Test
  void fromJson_AdditionalPropertyIsIgnored() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "foo-151633": "bar-151639",
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
            "code": "C71",
            "display": "Malignant neoplasm of brain"
          }]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(C71), criterion.getConcept());
  }

  @Test
  void fromJson_BloodPressure() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "85354-9",
            "display": "Blood pressure panel with all children optional"
          }],
          "attributeFilters": [
            {
              "attributeCode": {
                "system": "http://loinc.org",
                "code": "8462-4",
                "display": "Diastolic blood pressure"
              },
              "type": "quantity-comparator",
              "comparator": "lt",
              "value": 80,
              "unit": {
                "code": "mm[Hg]"
              }
            }
          ]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(BLOOD_PRESSURE), criterion.getConcept());
    assertEquals(List.of(
        NumericAttributeFilter.of(DIASTOLIC_BLOOD_PRESSURE, LESS_THAN, BigDecimal.valueOf(80),
            "mm[Hg]")), criterion.attributeFilters);
  }

  @Test
  void fromJson_BloodPressureRange() throws Exception {
    var mapper = new ObjectMapper();

    var criterion = (ConceptCriterion) mapper.readValue("""
        {
          "context": {
            "system": "context",
            "code": "context",
            "display": "context"
          },
          "termCodes": [{
            "system": "http://loinc.org",
            "code": "85354-9",
            "display": "Blood pressure panel with all children optional"
          }],
          "attributeFilters": [
            {
              "attributeCode": {
                "system": "http://loinc.org",
                "code": "8462-4",
                "display": "Diastolic blood pressure"
              },
              "type": "quantity-range",
              "minValue": 60,
              "maxValue": 100,
              "unit": {
                "code": "mm[Hg]"
              }
            }
          ]
        }
        """, Criterion.class);

    assertEquals(ContextualConcept.of(BLOOD_PRESSURE), criterion.getConcept());
    assertEquals(List.of(RangeAttributeFilter.of(DIASTOLIC_BLOOD_PRESSURE, BigDecimal.valueOf(60),
        BigDecimal.valueOf(100), "mm[Hg]")), criterion.attributeFilters);
  }

  @Test
  void toCql() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71));
    var mappingContext = MappingContext.of(Map.of(C71, Mapping.of(C71, "Condition")),
        TermCodeNode.of(C71), CODE_SYSTEM_ALIASES);

    var container = criterion.toCql(mappingContext);

    assertEquals("exists [Condition: Code 'C71' from icd10]",
        container.getExpression().map(PrintContext.ZERO::print).orElse(""));
    assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
  }

  @Test
  void toCql_WithMultipleTermCodes() {
    var criterion = ConceptCriterion.of(
        ContextualConcept.of(CONTEXT, Concept.of(C71_1_TC, C71_2_TC)));
    var mappings = Map.of(C71_1, Mapping.of(C71_1, "Condition"), C71_2,
        Mapping.of(C71_2, "Condition"));
    var mappingContext = MappingContext.of(mappings, TermCodeNode.of(C71), CODE_SYSTEM_ALIASES);

    var container = criterion.toCql(mappingContext);

    assertEquals("""
            exists [Condition: Code 'C71.1' from icd10] or
            exists [Condition: Code 'C71.2' from icd10]""",
        container.getExpression().map(PrintContext.ZERO::print).orElse(""));
    assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
  }

  @Test
  void toCql_WithAttributeFilter() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71))
        .appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED));
    var mapping = Mapping.of(C71, "Condition", null, null, List.of(),
        List.of(AttributeMapping.of("Coding", VERIFICATION_STATUS, "verificationStatus")));
    var mappingContext = MappingContext.of(Map.of(C71, mapping), TermCodeNode.of(C71),
        CODE_SYSTEM_ALIASES);

    var container = criterion.toCql(mappingContext);

    assertEquals("""
            exists (from [Condition: Code 'C71' from icd10] C
              where C.verificationStatus.coding contains Code 'confirmed' from ver_status)""",
        PrintContext.ZERO.print(container));
    assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF, VER_STATUS_CODE_SYSTEM_DEF),
        container.getCodeSystemDefinitions());
  }

  @Test
  void toCql_Expanded_WithAttributeFilter() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71))
        .appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED));
    var mapping1 = Mapping.of(C71_1, "Condition", null, null, List.of(),
        List.of(AttributeMapping.of("Coding", VERIFICATION_STATUS, "verificationStatus")));
    var mapping2 = Mapping.of(C71_2, "Condition", null, null, List.of(),
        List.of(AttributeMapping.of("Coding", VERIFICATION_STATUS, "verificationStatus")));
    var mappingContext = MappingContext.of(Map.of(C71_1, mapping1, C71_2, mapping2),
        TermCodeNode.of(C71, TermCodeNode.of(C71_1), TermCodeNode.of(C71_2)), CODE_SYSTEM_ALIASES);

    var container = criterion.toCql(mappingContext);

    assertEquals("""
            exists (from [Condition: Code 'C71.1' from icd10] C
              where C.verificationStatus.coding contains Code 'confirmed' from ver_status) or
            exists (from [Condition: Code 'C71.2' from icd10] C
              where C.verificationStatus.coding contains Code 'confirmed' from ver_status)""",
        PrintContext.ZERO.print(container));
    assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF, VER_STATUS_CODE_SYSTEM_DEF),
        container.getCodeSystemDefinitions());
  }

  @Test
  void toCql_WithDiastolicBloodPressureAttributeFilter() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(BLOOD_PRESSURE)).appendAttributeFilter(
        NumericAttributeFilter.of(DIASTOLIC_BLOOD_PRESSURE, LESS_THAN, BigDecimal.valueOf(80),
            "mm[Hg]"));
    var mappingContext = MappingContext.of(Map.of(BLOOD_PRESSURE,
            Mapping.of(BLOOD_PRESSURE, "Observation", "value", null, List.of(), List.of(
                AttributeMapping.of("", DIASTOLIC_BLOOD_PRESSURE, format(
                    "component.where(code.coding.exists(system = '%s' and code = '%s')).value.first()",
                    DIASTOLIC_BLOOD_PRESSURE.system(), DIASTOLIC_BLOOD_PRESSURE.code()))))), null,
        CODE_SYSTEM_ALIASES);

    var container = criterion.toCql(mappingContext);

    assertEquals("""
            exists (from [Observation: Code '85354-9' from loinc] O
              where O.component.where(code.coding.exists(system = 'http://loinc.org' and code = '8462-4')).value.first() as Quantity < 80 'mm[Hg]')""",
        PrintContext.ZERO.print(container));
    assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
  }

  @Test
  void toCql_FixedCriteria_Code() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(THERAPEUTIC_PROCEDURE));
    var mappingContext = MappingContext.of(Map.of(THERAPEUTIC_PROCEDURE,
            Mapping.of(THERAPEUTIC_PROCEDURE, "Procedure", null, null,
                List.of(CodeModifier.of("status", "completed", "in-progress")), List.of())), null,
        CODE_SYSTEM_ALIASES);

    var container = criterion.toCql(mappingContext);

    assertEquals("""
        exists (from [Procedure: Code '277132007' from snomed] P
          where P.status in { 'completed', 'in-progress' })""", PrintContext.ZERO.print(container));
    assertEquals(Set.of(SNOMED_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
  }

  @Test
  void toCql_FixedCriteria_Coding() {
    var criterion = ConceptCriterion.of(ContextualConcept.of(C71));
    var mappingContext = MappingContext.of(Map.of(C71, Mapping.of(C71, "Condition", null, null,
            List.of(CodingModifier.of("verificationStatus", CONFIRMED)), List.of())),
        TermCodeNode.of(C71), CODE_SYSTEM_ALIASES);

    var container = criterion.toCql(mappingContext);

    assertEquals("""
            exists (from [Condition: Code 'C71' from icd10] C
              where C.verificationStatus.coding contains Code 'confirmed' from ver_status)""",
        PrintContext.ZERO.print(container));
    assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF, VER_STATUS_CODE_SYSTEM_DEF),
        container.getCodeSystemDefinitions());
  }
}
