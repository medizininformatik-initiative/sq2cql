package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.Library;
import de.numcodex.sq2cql.model.structured_query.CodingModifier;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.ConceptCriterion;
import de.numcodex.sq2cql.model.structured_query.Criterion;
import de.numcodex.sq2cql.model.structured_query.NumericCriterion;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import de.numcodex.sq2cql.model.structured_query.TimeRestriction;
import de.numcodex.sq2cql.model.structured_query.TranslationException;
import de.numcodex.sq2cql.model.structured_query.ValueSetAttributeFilter;
import de.numcodex.sq2cql.model.structured_query.ValueSetCriterion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Alexander Kiel
 */
class TranslatorTest {

  static final TermCode ROOT = TermCode.of("", "", "");
  static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71",
      "Malignant neoplasm of brain");
  static final TermCode C71_0 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.0",
      "");
  static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1",
      "");
  static final TermCode PLATELETS = TermCode.of("http://loinc.org", "26515-7", "Platelets");
  static final TermCode FRAILTY_SCORE = TermCode.of("http://snomed.info/sct", "713636003",
      "Canadian Study of Health and Aging Clinical Frailty Scale score");
  static final TermCode VERY_FIT = TermCode.of(
      "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", "1",
      "Very Fit");
  static final TermCode WELL = TermCode.of(
      "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", "2", "Well");
  static final TermCode COPD = TermCode.of("http://snomed.info/sct", "13645005",
      "Chronic obstructive lung disease (disorder)");
  static final TermCode G47_31 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "G47.31",
      "Obstruktives Schlafapnoe-Syndrom");
  static final TermCode TOBACCO_SMOKING_STATUS = TermCode.of("http://loinc.org", "72166-2",
      "Tobacco smoking status");
  static final TermCode CURRENT_EVERY_DAY_SMOKER = TermCode.of("http://loinc.org", "LA18976-3",
      "Current every day smoker");
  static final TermCode HYPERTENSION = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm",
      "I10",
      "Essential (Primary) Hypertension");
  static final TermCode SERUM = TermCode.of("https://fhir.bbmri.de/CodeSystem/SampleMaterialType",
      "Serum",
      "Serum");
  static final TermCode TMZ = TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "L01AX03",
      "Temozolomide");
  static final TermCode LIPID = TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "C10AA",
      "lipid lowering drugs");
  static final TermCode CONFIRMED = TermCode.of(
      "http://terminology.hl7.org/CodeSystem/condition-ver-status",
      "confirmed", "Confirmed");
  static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
      "http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10",
      "http://loinc.org", "loinc",
      "https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample",
      "http://fhir.de/CodeSystem/dimdi/atc", "atc",
      "http://snomed.info/sct", "snomed",
      "http://hl7.org/fhir/administrative-gender", "gender",
      "http://terminology.hl7.org/CodeSystem/condition-ver-status", "ver_status",
      "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score",
      "frailty-score");
  static final TermCode VERIFICATION_STATUS = TermCode.of("hl7.org", "verificationStatus",
      "verificationStatus");
  static final AttributeMapping VERIFICATION_STATUS_ATTR_MAPPING =
      AttributeMapping.of("coding", VERIFICATION_STATUS, "verificationStatus");

  @Test
  void toCQL_Inclusion_OneDisjunctionWithOneCriterion() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE))));

    assertEquals("true",
        library.expressionDefinitions().get(0).getExpression().print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Inclusion_OneDisjunctionWithTwoCriteria() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE, Criterion.FALSE))));

    assertEquals("true or\nfalse", library.expressionDefinitions().get(0).getExpression()
        .print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Inclusion_TwoDisjunctionsWithOneCriterionEach() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE))));

    assertEquals("true and\nfalse", library.expressionDefinitions().get(0).getExpression()
        .print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Inclusion_TwoDisjunctionsWithTwoCriterionEach() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE, Criterion.TRUE),
            List.of(Criterion.FALSE, Criterion.FALSE))));

    assertEquals("(true or\ntrue) and\n(false or\nfalse)", library.expressionDefinitions().get(0)
        .getExpression().print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Inclusion_And_Exclusion_OneConjunctionWithOneCriterion() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE)),
        List.of(List.of(Criterion.FALSE))));

    assertEquals("define Inclusion:\n  true",
        library.expressionDefinitions().get(0).print(PrintContext.ZERO));
    assertEquals("define Exclusion:\n  false",
        library.expressionDefinitions().get(1).print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Inclusion_And_Exclusion_OneConjunctionWithTwoCriteria() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE)),
        List.of(List.of(Criterion.TRUE, Criterion.FALSE))));

    assertEquals("define Exclusion:\n  true and\n  false", library.expressionDefinitions().get(1)
        .print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Inclusion_And_Exclusion_TwoConjunctionsWithOneCriterionEach() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE)),
        List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE))));

    assertEquals("true or\nfalse", library.expressionDefinitions().get(1).getExpression()
        .print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Inclusion_And_Exclusion_TwoConjunctionsWithTwoCriterionEach() {
    Library library = Translator.of().toCql(StructuredQuery.of(
        List.of(List.of(Criterion.TRUE)),
        List.of(List.of(Criterion.TRUE, Criterion.TRUE),
            List.of(Criterion.FALSE, Criterion.FALSE))));

    assertEquals("true and\ntrue or\nfalse and\nfalse", library.expressionDefinitions().get(1)
        .getExpression().print(PrintContext.ZERO));
  }

  @Test
  void toCQL_NonExpandableConcept() {
    var message = assertThrows(TranslationException.class,
        () -> Translator.of().toCql(StructuredQuery.of(
            List.of(List.of(ConceptCriterion.of(Concept.of(C71))))))).getMessage();

    assertEquals(
        "Failed to expand the concept (system: http://fhir.de/CodeSystem/dimdi/icd-10-gm, code: C71, display: Malignant neoplasm of brain).",
        message);
  }

  @Test
  void toCQL_NonMappableConcept() {
    var conceptTree = TermCodeNode.of(C71, TermCodeNode.of(C71_0), TermCodeNode.of(C71_1));
    var mappingContext = MappingContext.of(Map.of(), conceptTree, CODE_SYSTEM_ALIASES);

    var message = assertThrows(TranslationException.class,
        () -> Translator.of(mappingContext).toCql(StructuredQuery.of(
            List.of(List.of(ConceptCriterion.of(Concept.of(C71))))))).getMessage();

    assertEquals(
        "Failed to expand the concept (system: http://fhir.de/CodeSystem/dimdi/icd-10-gm, code: C71, display: Malignant neoplasm of brain).",
        message);
  }

  @Test
  void toCQL_Usage_Documentation() {
    var c71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1",
        "Malignant neoplasm of brain");
    var mappings = Map.of(c71_1, Mapping.of(c71_1, "Condition"));
    var conceptTree = TermCodeNode.of(c71_1);
    var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
    var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);

    Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
        List.of(ConceptCriterion.of(Concept.of(c71_1))))));

    assertEquals("""
        library Retrieve
        using FHIR version '4.0.0'
        include FHIRHelpers version '4.0.0'
                                           
        codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
               
        context Patient
                        
        define InInitialPopulation:
          exists [Condition: Code 'C71.1' from icd10]
        """, library.print(PrintContext.ZERO));
  }

  @Test
  void toCQL_TimeRestriction() {
    var c71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1",
        "Malignant neoplasm of brain");
    var mappings = Map.of(c71_1,
        Mapping.of(c71_1, "Condition", null, null, List.of(), List.of(), "onset"));
    var conceptTree = TermCodeNode.of(c71_1);
    var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
    var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);

    Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
        List.of(ConceptCriterion.of(Concept.of(c71_1),
            TimeRestriction.of("2020-01-01T", "2020-01-02T"))))));

    assertEquals("""
            library Retrieve
            using FHIR version '4.0.0'
            include FHIRHelpers version '4.0.0'
                                               
            codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
                           
            context Patient
                            
            define InInitialPopulation:
              exists (from [Condition: Code 'C71.1' from icd10] C
                where C.onset as dateTime in Interval[@2020-01-01T, @2020-01-02T] or
                  C.onset overlaps Interval[@2020-01-01T, @2020-01-02T])
            """, library.print(PrintContext.ZERO));
  }

  @Test
  void toCQL_TimeRestriction_missingPathInMapping() {
    var c71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1",
            "Malignant neoplasm of brain");
    var mappings = Map.of(c71_1,
            Mapping.of(c71_1, "Condition", null, null, List.of(), List.of(), null));
    var conceptTree = TermCodeNode.of(c71_1);
    var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
    var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);
    var query = StructuredQuery.of(List.of(
            List.of(ConceptCriterion.of(Concept.of(c71_1),
                    TimeRestriction.of("2020-01-01T", "2020-01-02T")))));
    var translator = Translator.of(mappingContext);

    assertThatIllegalStateException().isThrownBy(() -> translator.toCql(query))
            .withMessage("Missing timeRestrictionPath in mapping with key TermCode[system=http://fhir.de/CodeSystem/dimdi/icd-10-gm, code=C71.1, display=Malignant neoplasm of brain].");
  }

  @Test
  void toCQL_Test_Task1() {
    var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation", "value"),
            C71_0, Mapping.of(C71_0, "Condition", null, null, List.of(),
                    List.of(VERIFICATION_STATUS_ATTR_MAPPING)),
            C71_1, Mapping.of(C71_1, "Condition", null, null, List.of(),
                    List.of(VERIFICATION_STATUS_ATTR_MAPPING)),
            TMZ, Mapping.of(TMZ, "MedicationStatement"));
    var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(TMZ),
        TermCodeNode.of(C71, TermCodeNode.of(C71_0),
            TermCodeNode.of(C71_1)));
    var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
    var structuredQuery = StructuredQuery.of(List.of(
        List.of(ConceptCriterion.of(Concept.of(C71),
            ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED))),
        List.of(
            NumericCriterion.of(Concept.of(PLATELETS), LESS_THAN, BigDecimal.valueOf(50), "g/dl")),
        List.of(ConceptCriterion.of(Concept.of(TMZ)))));

    Library library = Translator.of(mappingContext).toCql(structuredQuery);

    assertEquals("""
            library Retrieve
            using FHIR version '4.0.0'
            include FHIRHelpers version '4.0.0'

            codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'
            codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
            codesystem loinc: 'http://loinc.org'
            codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'

            context Patient

            define InInitialPopulation:
              (exists (from [Condition: Code 'C71.0' from icd10] C
                where C.verificationStatus.coding contains Code 'confirmed' from ver_status) or
              exists (from [Condition: Code 'C71.1' from icd10] C
                where C.verificationStatus.coding contains Code 'confirmed' from ver_status)) and
              exists (from [Observation: Code '26515-7' from loinc] O
                where O.value as Quantity < 50 'g/dl') and
              exists [MedicationStatement: Code 'L01AX03' from atc]
            """, library.print(PrintContext.ZERO));
  }

  @Test
  void toCQL_Test_Task2() {
    var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation", "value"),
        HYPERTENSION, Mapping.of(HYPERTENSION, "Condition", null, null, List.of(),
            List.of(VERIFICATION_STATUS_ATTR_MAPPING)),
        SERUM, Mapping.of(SERUM, "Specimen"),
        LIPID, Mapping.of(LIPID, "MedicationStatement"));
    var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(HYPERTENSION), TermCodeNode.of(SERUM),
        TermCodeNode.of(LIPID));
    var mappingContext = MappingContext.of(mappings,
        conceptTree,
        CODE_SYSTEM_ALIASES);
    var structuredQuery = StructuredQuery.of(List.of(
        List.of(ConceptCriterion.of(Concept.of(HYPERTENSION),
            ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED))),
        List.of(ConceptCriterion.of(Concept.of(SERUM)))), List.of(
        List.of(ConceptCriterion.of(Concept.of(LIPID)))));

    Library library = Translator.of(mappingContext).toCql(structuredQuery);

    assertEquals("""
            library Retrieve
            using FHIR version '4.0.0'
            include FHIRHelpers version '4.0.0'

            codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'
            codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
            codesystem sample: 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType'
            codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'
                            
            context Patient

            define Inclusion:
              exists (from [Condition: Code 'I10' from icd10] C
                where C.verificationStatus.coding contains Code 'confirmed' from ver_status) and
              exists [Specimen: Code 'Serum' from sample]

            define Exclusion:
              exists [MedicationStatement: Code 'C10AA' from atc]

            define InInitialPopulation:
              Inclusion and
              not Exclusion
            """, library.print(PrintContext.ZERO));
  }

  @Test
  void toCQL_GeccoTask2() {
    var mappings = Map.of(FRAILTY_SCORE, Mapping.of(FRAILTY_SCORE, "Observation", "value"),
        COPD, Mapping.of(COPD, "Condition", null, null,
            List.of(CodingModifier.of("verificationStatus", CONFIRMED)),
            List.of()),
        G47_31, Mapping.of(G47_31, "Condition", null, null,
            List.of(CodingModifier.of("verificationStatus", CONFIRMED)),
            List.of()),
        TOBACCO_SMOKING_STATUS, Mapping.of(TOBACCO_SMOKING_STATUS, "Observation", "value"));
    var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(COPD), TermCodeNode.of(G47_31));
    var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
    var structuredQuery = StructuredQuery.of(List.of(
        List.of(ValueSetCriterion.of(Concept.of(FRAILTY_SCORE), VERY_FIT, WELL))), List.of(
        List.of(ConceptCriterion.of(Concept.of(COPD)), ConceptCriterion.of(Concept.of(G47_31))),
        List.of(
            ValueSetCriterion.of(Concept.of(TOBACCO_SMOKING_STATUS), CURRENT_EVERY_DAY_SMOKER))));

    Library library = Translator.of(mappingContext).toCql(structuredQuery);

    assertEquals("""
            library Retrieve
            using FHIR version '4.0.0'
            include FHIRHelpers version '4.0.0'

            codesystem frailty-score: 'https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score'
            codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
            codesystem loinc: 'http://loinc.org'
            codesystem snomed: 'http://snomed.info/sct'
            codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'
                            
            context Patient
                            
            define Inclusion:
              exists (from [Observation: Code '713636003' from snomed] O
                where O.value.coding contains Code '1' from frailty-score or
                  O.value.coding contains Code '2' from frailty-score)
                            
            define Exclusion:
              exists (from [Condition: Code '13645005' from snomed] C
                where C.verificationStatus.coding contains Code 'confirmed' from ver_status) and
              exists (from [Condition: Code 'G47.31' from icd10] C
                where C.verificationStatus.coding contains Code 'confirmed' from ver_status) or
              exists (from [Observation: Code '72166-2' from loinc] O
                where O.value.coding contains Code 'LA18976-3' from loinc)
                            
            define InInitialPopulation:
              Inclusion and
              not Exclusion
            """, library.print(PrintContext.ZERO));
  }
}
