package de.numcodex.sq2cql;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.*;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static de.numcodex.sq2cql.Assertions.assertThat;
import static de.numcodex.sq2cql.Util.*;
import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static de.numcodex.sq2cql.model.Mapping.TimeRestrictionMapping.Type.DATE_TIME;
import static de.numcodex.sq2cql.model.Mapping.TimeRestrictionMapping.Type.PERIOD;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Alexander Kiel
 */
class TranslatorTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode COMBINED_CONSENT = ContextualTermCode.of(CONTEXT,
            TermCode.of("mii.abide", "combined-consent", ""));
    static final ContextualTermCode AGE = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://snomed.info/sct", "424144002", "Current chronological age"));
    static final ContextualTermCode GENDER = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://snomed.info/sct", "263495000", "Gender"));
    static final TermCode CONTEXT_CONSENT = TermCode.of("fdpg.mii.cds", "Einwilligung", "Einwilligung");
    static final ContextualTermCode CONSENT_MDAT = ContextualTermCode.of(CONTEXT_CONSENT,
            TermCode.of("urn:oid:2.16.840.1.113883.3.1937.777.24.5.3",
                    "2.16.840.1.113883.3.1937.777.24.5.3.8",
                    "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU"));
    static final TermCode CONTEXT_ENCOUNTER = TermCode.of("fdpg.mii.cds", "Fall", "Fall");
    static final ContextualTermCode ENCOUNTER_CLASS = ContextualTermCode.of(CONTEXT_ENCOUNTER,
            TermCode.of("http://terminology.hl7.org/CodeSystem/v3-ActCode",
                    "VR", "virtual"));
    static final ContextualTermCode C71 = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
                    "Malignant neoplasm of brain"));
    static final ContextualTermCode C71_0 = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.0",
                    "Malignant neoplasm of frontal lobe"));
    static final ContextualTermCode C71_1 = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
                    "Malignant neoplasm of temporal lobe"));
    static final ContextualTermCode PLATELETS = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://loinc.org", "26515-7", "Platelets"));
    static final ContextualTermCode FRAILTY_SCORE = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://snomed.info/sct",
                    "713636003", "Canadian Study of Health and Aging Clinical Frailty Scale score"));
    static final TermCode VERY_FIT = TermCode.of(
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty_score", "1",
            "Very Fit");
    static final TermCode WELL = TermCode.of(
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty_score", "2", "Well");
    static final ContextualTermCode COPD = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://snomed.info/sct", "13645005",
                    "Chronic obstructive lung disease (disorder)"));
    static final ContextualTermCode G47_31 = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "G47.31",
                    "Obstruktives Schlafapnoe-Syndrom"));
    static final ContextualTermCode TOBACCO_SMOKING_STATUS = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://loinc.org", "72166-2", "Tobacco smoking status"));
    static final TermCode CURRENT_EVERY_DAY_SMOKER = TermCode.of("http://loinc.org", "LA18976-3",
            "Current every day smoker");
    static final ContextualTermCode HYPERTENSION = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "I10",
                    "Essential (Primary) Hypertension"));
    static final ContextualTermCode SERUM = ContextualTermCode.of(CONTEXT,
            TermCode.of("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "Serum", "Serum"));
    static final ContextualTermCode TMZ = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "L01AX03", "Temozolomide"));
    static final ContextualTermCode LIPID = ContextualTermCode.of(CONTEXT,
            TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "C10AA", "lipid lowering drugs"));
    static final TermCode CONTEXT_LAB = TermCode.of("fdpg.mii.cds", "Laboruntersuchung", "Laboruntersuchung");
    static final ContextualTermCode BLOOD_PRESSURE = ContextualTermCode.of(CONTEXT_LAB,
            TermCode.of("http://loinc.org", "85354-9", "Blood pressure panel with all children optional"));
    static final TermCode CONFIRMED = TermCode.of(
            "http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed");
    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10",
            "http://loinc.org", "loinc",
            "https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample",
            "http://fhir.de/CodeSystem/dimdi/atc", "atc",
            "http://snomed.info/sct", "snomed",
            "http://hl7.org/fhir/administrative-gender", "gender",
            "http://terminology.hl7.org/CodeSystem/condition-ver-status", "ver_status",
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty_score", "frailty_score",
            "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3", "consent",
            "http://terminology.hl7.org/CodeSystem/v3-ActCode", "act_code");
    static final TermCode VERIFICATION_STATUS = TermCode.of("hl7.org", "verificationStatus",
            "verificationStatus");
    static final AttributeMapping VERIFICATION_STATUS_ATTR_MAPPING = AttributeMapping.of(List.of("Coding"),
            VERIFICATION_STATUS, "verificationStatus");

    private static Mapping readMapping(String s) throws Exception {
        return new ObjectMapper().readValue(s, Mapping.class);
    }

    private static StructuredQuery readStructuredQuery(String s) throws Exception {
        return new ObjectMapper().readValue(s, StructuredQuery.class);
    }

    @Nested
    class ToCql {

        @Test
        void nonExpandableConcept() {
            var structuredQuery = StructuredQuery.of(List.of(List.of(ConceptCriterion.of(ContextualConcept.of(C71)))));

            var message = assertThrows(TranslationException.class, () -> Translator.of().toCql(structuredQuery)).getMessage();

            assertEquals(
                    "Failed to expand the concept ContextualConcept[context=TermCode[system=context, code=context, display=context], concept=Concept[termCodes=[TermCode[system=http://fhir.de/CodeSystem/bfarm/icd-10-gm, code=C71, display=Malignant neoplasm of brain]]]].",
                    message);
        }

        @Test
        void nonMappableConcept() {
            var conceptTree = createTreeWithChildren(C71, C71_0, C71_1);
            var mappingContext = MappingContext.of(Map.of(), conceptTree, CODE_SYSTEM_ALIASES);

            var message = assertThrows(TranslationException.class, () -> Translator.of(mappingContext)
                    .toCql(StructuredQuery.of(
                            List.of(List.of(ConceptCriterion.of(ContextualConcept.of(C71))))))).getMessage();

            assertEquals(
                    "Failed to expand the concept ContextualConcept[context=TermCode[system=context, code=context, display=context], concept=Concept[termCodes=[TermCode[system=http://fhir.de/CodeSystem/bfarm/icd-10-gm, code=C71, display=Malignant neoplasm of brain]]]].",
                    message);
        }

        @Test
        void usage_Documentation() {
            var c71_1 = ContextualTermCode.of(CONTEXT,
                    TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
                            "Malignant neoplasm of brain"));
            var mappings = Map.of(c71_1, Mapping.of(c71_1, "Condition"));
            var conceptTree = createTreeWithoutChildren(c71_1);
            var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10");
            var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);

            var library = Translator.of(mappingContext).toCql(
                    StructuredQuery.of(List.of(List.of(ConceptCriterion.of(ContextualConcept.of(c71_1))))));

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                    
                    context Patient
                    
                    define Criterion:
                      exists [Condition: Code 'C71.1' from icd10]
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void timeRestriction() {
            var c71_1 = ContextualTermCode.of(CONTEXT,
                    TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
                            "Malignant neoplasm of brain"));
            var mappings = Map.of(c71_1,
                    Mapping.of(c71_1, "Condition", null, List.of(), List.of(), Mapping.TimeRestrictionMapping.of("onset", DATE_TIME, PERIOD)));
            var conceptTree = createTreeWithoutChildren(c71_1);
            var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10");
            var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);

            var library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(List.of(
                    ConceptCriterion.of(ContextualConcept.of(c71_1),
                            TimeRestriction.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2)))))));

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                    
                    context Patient
                    
                    define Criterion:
                      exists (from [Condition: Code 'C71.1' from icd10] C
                        where ToDate(C.onset as dateTime) in Interval[@2020-01-01T, @2020-01-02T] or
                          C.onset overlaps Interval[@2020-01-01T, @2020-01-02T])
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void timeRestriction_missingInMapping() {
            var c71_1 = ContextualTermCode.of(CONTEXT,
                    TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1",
                            "Malignant neoplasm of brain"));
            var mappings = Map.of(c71_1,
                    Mapping.of(c71_1, "Condition", null, List.of(), List.of()));
            var conceptTree = createTreeWithoutChildren(c71_1);
            var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10");
            var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);
            var query = StructuredQuery.of(List.of(List.of(ConceptCriterion.of(ContextualConcept.of(c71_1),
                    TimeRestriction.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2))))));
            var translator = Translator.of(mappingContext);

            assertThatIllegalStateException().isThrownBy(() -> translator.toCql(query)).withMessage(
                    "Missing time restriction in mapping with key ContextualTermCode[context=TermCode[system=context, code=context, display=context], termCode=TermCode[system=http://fhir.de/CodeSystem/bfarm/icd-10-gm, code=C71.1, display=Malignant neoplasm of brain]].");
        }

        @Test
        void test_Task1() {
            var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.QUANTITY)), C71_0,
                    Mapping.of(C71_0, "Condition", null, List.of(),
                            List.of(VERIFICATION_STATUS_ATTR_MAPPING)), C71_1,
                    Mapping.of(C71_1, "Condition", null, List.of(),
                            List.of(VERIFICATION_STATUS_ATTR_MAPPING)), TMZ,
                    Mapping.of(TMZ, "MedicationStatement"));
            var conceptTree = new MappingTreeBase(List.of(
                    createTreeRootWithoutChildren(TMZ),
                    createTreeRootWithChildren(C71, C71_0, C71_1)));
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
            var structuredQuery = StructuredQuery.of(List.of(List.of(
                            ConceptCriterion.of(ContextualConcept.of(C71))
                                    .appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED))),
                    List.of(
                            NumericCriterion.of(ContextualConcept.of(PLATELETS), LESS_THAN, BigDecimal.valueOf(50),
                                    "g/dl")), List.of(ConceptCriterion.of(ContextualConcept.of(TMZ)))));

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'
                    codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                    codesystem loinc: 'http://loinc.org'
                    codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'
                    
                    context Unfiltered
                    
                    define L01AX03Ref:
                      from [Medication: Code 'L01AX03' from atc] M
                        return 'Medication/' + M.id
                    
                    context Patient
                    
                    define "Criterion 1":
                      exists (from [Condition: Code 'C71.0' from icd10] C
                        where C.verificationStatus ~ Code 'confirmed' from ver_status) or
                      exists (from [Condition: Code 'C71.1' from icd10] C
                        where C.verificationStatus ~ Code 'confirmed' from ver_status)
                    
                    define "Criterion 2":
                      exists (from [Observation: Code '26515-7' from loinc] O
                        where O.value as Quantity < 50 'g/dl')
                    
                    define "Criterion 3":
                      exists (from [MedicationStatement] M
                        where M.medication.reference in L01AX03Ref)
                    
                    define InInitialPopulation:
                      "Criterion 1" and
                      "Criterion 2" and
                      "Criterion 3"
                    """);
        }

        @Test
        void test_Task2() {
            var mappings = Map.of(HYPERTENSION, Mapping.of(HYPERTENSION, "Condition", null, List.of(),
                            List.of(VERIFICATION_STATUS_ATTR_MAPPING)), SERUM, Mapping.of(SERUM, "Specimen"), LIPID,
                    Mapping.of(LIPID, "MedicationStatement"));
            var conceptTree = new MappingTreeBase(List.of(
                    createTreeRootWithoutChildren(HYPERTENSION),
                    createTreeRootWithoutChildren(SERUM),
                    createTreeRootWithoutChildren(LIPID)));
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
            var structuredQuery = StructuredQuery.of(List.of(List.of(
                                    ConceptCriterion.of(ContextualConcept.of(HYPERTENSION))
                                            .appendAttributeFilter(ValueSetAttributeFilter.of(VERIFICATION_STATUS, CONFIRMED))),
                            List.of(ConceptCriterion.of(ContextualConcept.of(SERUM)))),
                    List.of(List.of(ConceptCriterion.of(ContextualConcept.of(LIPID)))));

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'
                    codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                    codesystem sample: 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType'
                    codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'
                    
                    context Unfiltered
                    
                    define C10AARef:
                      from [Medication: Code 'C10AA' from atc] M
                        return 'Medication/' + M.id
                    
                    context Patient
                    
                    define "Criterion 1":
                      exists (from [Condition: Code 'I10' from icd10] C
                        where C.verificationStatus ~ Code 'confirmed' from ver_status)
                    
                    define "Criterion 2":
                      exists [Specimen: Code 'Serum' from sample]
                    
                    define Inclusion:
                      "Criterion 1" and
                      "Criterion 2"
                    
                    define "Criterion 3":
                      exists (from [MedicationStatement] M
                        where M.medication.reference in C10AARef)
                    
                    define Exclusion:
                      "Criterion 3"
                    
                    define InInitialPopulation:
                      Inclusion and
                      not Exclusion
                    """);
        }

        @Test
        void geccoTask2() {
            var mappings = Map.of(FRAILTY_SCORE, Mapping.of(FRAILTY_SCORE, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.CODEABLE_CONCEPT)), COPD,
                    Mapping.of(COPD, "Condition", null,
                            List.of(CodeEquivalentModifier.of("verificationStatus", CONFIRMED)), List.of()), G47_31,
                    Mapping.of(G47_31, "Condition", null,
                            List.of(CodeEquivalentModifier.of("verificationStatus", CONFIRMED)), List.of()),
                    TOBACCO_SMOKING_STATUS, Mapping.of(TOBACCO_SMOKING_STATUS, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.CODEABLE_CONCEPT)));
            var conceptTree = new MappingTreeBase(List.of(createTreeRootWithoutChildren(COPD), createTreeRootWithoutChildren(G47_31)));
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
            var structuredQuery = StructuredQuery.of(
                    List.of(List.of(ValueSetCriterion.of(ContextualConcept.of(FRAILTY_SCORE), VERY_FIT, WELL))),
                    List.of(List.of(ConceptCriterion.of(ContextualConcept.of(COPD)),
                            ConceptCriterion.of(ContextualConcept.of(G47_31))), List.of(
                            ValueSetCriterion.of(ContextualConcept.of(TOBACCO_SMOKING_STATUS),
                                    CURRENT_EVERY_DAY_SMOKER))));

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'

                    codesystem frailty_score: 'https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty_score'
                    codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                    codesystem loinc: 'http://loinc.org'
                    codesystem snomed: 'http://snomed.info/sct'
                    codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'
                    
                    context Patient
                    
                    define "Criterion 1":
                      exists (from [Observation: Code '713636003' from snomed] O
                        where O.value ~ Code '1' from frailty_score or
                          O.value ~ Code '2' from frailty_score)
                    
                    define Inclusion:
                      "Criterion 1"
                    
                    define "Criterion 2":
                      exists (from [Condition: Code '13645005' from snomed] C
                        where C.verificationStatus ~ Code 'confirmed' from ver_status)
                    
                    define "Criterion 3":
                      exists (from [Condition: Code 'G47.31' from icd10] C
                        where C.verificationStatus ~ Code 'confirmed' from ver_status)
                    
                    define "Criterion 4":
                      exists (from [Observation: Code '72166-2' from loinc] O
                        where O.value ~ Code 'LA18976-3' from loinc)
                    
                    define Exclusion:
                      "Criterion 2" and
                      "Criterion 3" or
                      "Criterion 4"
                    
                    define InInitialPopulation:
                      Inclusion and
                      not Exclusion
                    """);
        }

        @Test
        void onlyFixedCriteria() throws Exception {
            var mapping = readMapping("""
                    {
                        "resourceType": "Consent",
                        "fixedCriteria": [
                            {
                                "path": "status",
                                "types": ["code"],
                                "value": [
                                    {
                                        "code": "active",
                                        "display": "Active",
                                        "system": "http://hl7.org/fhir/consent-state-codes"
                                    }
                                ]
                            },
                            {
                                "path": "provision.provision.code",
                                "types": ["Coding"],
                                "cardinality": "many",
                                "value": [
                                    {
                                        "code": "2.16.840.1.113883.3.1937.777.24.5.3.5",
                                        "display": "IDAT bereitstellen EU DSGVO NIVEAU",
                                        "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
                                    }
                                ]
                            },
                            {
                                "path": "provision.provision.code",
                                "types": ["Coding"],
                                "cardinality": "many",
                                "value": [
                                    {
                                        "code": "2.16.840.1.113883.3.1937.777.24.5.3.2",
                                        "display": "IDAT erheben",
                                        "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
                                    }
                                ]
                            }
                        ],
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "combined-consent",
                            "display": "Einwilligung f\\u00fcr die zentrale Datenanalyse",
                            "system": "mii.abide"
                        },
                        "primaryCode": {
                            "code": "54133-1",
                            "display": "Consent Document",
                            "system": "http://loinc.org"
                        }
                    }
                    """);

            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "combined-consent",
                                "system": "mii.abide",
                                "display": "Einwilligung für die zentrale Datenanalyse"
                              }
                            ]
                          }
                        ]
                      ]
                    }
                    """);

            var conceptTree = createTreeWithoutChildren(COMBINED_CONSENT);
            var mappings = Map.of(COMBINED_CONSENT, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem consent: 'urn:oid:2.16.840.1.113883.3.1937.777.24.5.3'
                    codesystem loinc: 'http://loinc.org'
                    
                    context Patient
                    
                    define Criterion:
                      exists (from [Consent: Code '54133-1' from loinc] C
                        where C.status = 'active' and
                          exists (from C.provision.provision.code C
                            where C ~ Code '2.16.840.1.113883.3.1937.777.24.5.3.5' from consent) and
                          exists (from C.provision.provision.code C
                            where C ~ Code '2.16.840.1.113883.3.1937.777.24.5.3.2' from consent))
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void numericAgeTranslation() throws Exception {
            var mapping = readMapping("""
                    {
                        "resourceType": "Patient",
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "424144002",
                            "display": "Current chronological age",
                            "system": "http://snomed.info/sct"
                        }
                    }
                    """);

            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "424144002",
                                "system": "http://snomed.info/sct",
                                "display": "Current chronological age"
                              }
                            ],
                            "valueFilter": {
                              "selectedConcepts": [],
                              "type": "quantity-comparator",
                              "unit": {
                                "code": "a",
                                "display": "a"
                              },
                              "value": 5,
                              "comparator": "gt"
                            }
                          }
                        ]
                      ]
                    }
                    """);
            var conceptTree = createTreeWithoutChildren(AGE);
            var mappings = Map.of(AGE, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      AgeInYears() > 5
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void ageRangeTranslation() throws Exception {
            var mapping = readMapping("""
                    {
                        "resourceType": "Patient",
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "424144002",
                            "display": "Current chronological age",
                            "system": "http://snomed.info/sct"
                        },
                        "value": {
                          "path": "birthDate",
                          "types": [ "date" ]
                        }
                    }
                    """);

            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "424144002",
                                "system": "http://snomed.info/sct",
                                "display": "Current chronological age"
                              }
                            ],
                            "valueFilter": {
                              "selectedConcepts": [],
                              "type": "quantity-range",
                              "unit": {
                                "code": "a",
                                "display": "a"
                              },
                              "minValue": 5,
                              "maxValue": 10
                            }
                          }
                        ]
                      ]
                    }
                    """);
            var conceptTree = createTreeWithoutChildren(AGE);
            var mappings = Map.of(AGE, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      AgeInYears() between 5 and 10
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void numericAgeTranslationInHours() throws Exception {
            var mapping = readMapping("""
                    {
                        "resourceType": "Patient",
                        "context": {
                            "code": "context",
                            "display": "context",
                            "system": "context"
                        },
                        "key": {
                            "code": "424144002",
                            "display": "Current chronological age",
                            "system": "http://snomed.info/sct"
                        },
                        "value": {
                          "path": "birthDate",
                          "types": [ "date" ]
                        }
                    }
                    """);

            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "424144002",
                                "system": "http://snomed.info/sct",
                                "display": "Current chronological age"
                              }
                            ],
                            "valueFilter": {
                              "selectedConcepts": [],
                              "type": "quantity-comparator",
                              "unit": {
                                "code": "h",
                                "display": "h"
                              },
                              "value": 5,
                              "comparator": "lt"
                            }
                          }
                        ]
                      ]
                    }
                    """);
            var conceptTree = createTreeWithoutChildren(AGE);
            var mappings = Map.of(AGE, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      AgeInHours() < 5
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void patientGender() throws Exception {
            var mapping = readMapping("""
                    {
                      "context": {
                        "code": "Patient",
                        "display": "Patient",
                        "system": "fdpg.mii.cds",
                        "version": "1.0.0"
                      },
                      "key": {
                        "code": "263495000",
                        "display": "Geschlecht",
                        "system": "http://snomed.info/sct"
                      },
                      "resourceType": "Patient",
                      "value": {
                        "path": "gender",
                        "types": [
                          "code"
                        ]
                      }
                    }
                    """);
            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                                "code": "context",
                                "display": "context",
                                "system": "context"
                            },
                            "termCodes": [
                              {
                                "code": "263495000",
                                "display": "Geschlecht",
                                "system": "http://snomed.info/sct"
                              }
                            ],
                            "valueFilter": {
                              "type": "concept",
                              "selectedConcepts": [
                                {
                                  "code": "female",
                                  "system": "http://hl7.org/fhir/administrative-gender",
                                  "display": "Female"
                                }
                              ]
                            }
                          }
                        ]
                      ]
                    }
                    """);
            var conceptTree = createTreeWithoutChildren(GENDER);
            var mappings = Map.of(GENDER, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      Patient.gender = 'female'
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void consent() throws Exception {
            var mapping = readMapping("""
                    {
                      "context": {
                        "code": "Einwilligung",
                        "display": "Einwilligung",
                        "system": "fdpg.mii.cds",
                        "version": "1.0.0"
                      },
                      "key": {
                        "code": "2.16.840.1.113883.3.1937.777.24.5.3.8",
                        "display": "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU",
                        "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3",
                        "version": "1.0.2"
                      },
                      "resourceType": "Consent",
                      "termCode": {
                        "path": "provision.provision.code",
                        "types": [
                          "CodeableConcept"
                        ],
                        "cardinality": "many"
                      }
                    }
                    """);

            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "http://to_be_decided.com/draft-1/schema#",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                              "code": "Einwilligung",
                              "display": "Einwilligung",
                              "system": "fdpg.mii.cds",
                              "version": "1.0.0"
                            },
                            "termCodes": [
                              {
                                "code": "2.16.840.1.113883.3.1937.777.24.5.3.8",
                                "display": "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU",
                                "system": "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
                              }
                            ]
                          }
                        ]
                      ]
                    }
                    """);
            var conceptTree = createTreeWithoutChildren(CONSENT_MDAT);
            var mappings = Map.of(CONSENT_MDAT, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem consent: 'urn:oid:2.16.840.1.113883.3.1937.777.24.5.3'
                    
                    context Patient
                    
                    define Criterion:
                      exists (from [Consent] C
                        where exists (from C.provision.provision.code C
                            where C ~ Code '2.16.840.1.113883.3.1937.777.24.5.3.8' from consent))
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void encounter() throws Exception {
            var mapping = readMapping("""
                    {
                      "context": {
                        "code": "Fall",
                        "display": "Fall",
                        "system": "fdpg.mii.cds",
                        "version": "1.0.0"
                      },
                      "key": {
                        "code": "VR",
                        "display": "virtual",
                        "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                        "version": "9.0.0"
                      },
                      "resourceType": "Encounter",
                      "termCode": {
                        "path": "class",
                        "types": [
                          "Coding"
                        ]
                      }
                    }
                    """);

            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "http://to_be_decided.com/draft-1/schema#",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                              "code": "Fall",
                              "display": "Fall",
                              "system": "fdpg.mii.cds",
                              "version": "1.0.0"
                            },
                            "termCodes": [
                              {
                                "code": "VR",
                                "display": "virtual",
                                "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                                "version": "9.0.0"
                              }
                            ]
                          }
                        ]
                      ]
                    }
                    """);
            var conceptTree = createTreeWithoutChildren(ENCOUNTER_CLASS);
            var mappings = Map.of(ENCOUNTER_CLASS, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem act_code: 'http://terminology.hl7.org/CodeSystem/v3-ActCode'
                    
                    context Patient
                    
                    define Criterion:
                      exists (from [Encounter] E
                        where E.class ~ Code 'VR' from act_code)
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Test
        void bloodPressure() throws Exception {
            var mapping = readMapping("""
                    {
                        "context": {
                          "code": "Laboruntersuchung",
                          "display": "Laboruntersuchung",
                          "system": "fdpg.mii.cds",
                          "version": "1.0.0"
                        },
                        "key": {
                            "system": "http://loinc.org",
                            "code": "85354-9",
                            "display": "Blood pressure panel with all children optional"
                        },
                        "resourceType": "Observation",
                        "attributes": [
                          {
                            "types": ["Coding"],
                            "key": {
                              "system": "http://loinc.org",
                              "code": "8462-4",
                              "display": "Diastolic blood pressure"
                            },
                            "path": "component.where(code.coding.exists(system = 'http://loinc.org' and code = '8462-4')).value.first()"
                          }
                        ]
                    }
                    """);

            var structuredQuery = readStructuredQuery("""
                    {
                      "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                      "display": "",
                      "inclusionCriteria": [
                        [
                          {
                            "context": {
                              "code": "Laboruntersuchung",
                              "display": "Laboruntersuchung",
                              "system": "fdpg.mii.cds",
                              "version": "1.0.0"
                            },
                            "termCodes": [
                              {
                                "system": "http://loinc.org",
                                "code": "85354-9",
                                "display": "Blood pressure panel with all children optional"
                              }
                            ],
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
                        ]
                      ]
                    }
                    """);
            var conceptTree = createTreeWithoutChildren(BLOOD_PRESSURE);
            var mappings = Map.of(BLOOD_PRESSURE, mapping);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

            var library = Translator.of(mappingContext).toCql(structuredQuery);

            assertThat(library).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    codesystem loinc: 'http://loinc.org'
                    
                    context Patient
                    
                    define Criterion:
                      exists (from [Observation: Code '85354-9' from loinc] O
                        where O.component.where(code.coding.exists(system = 'http://loinc.org' and code = '8462-4')).value.first() as Quantity < 80 'mm[Hg]')
                    
                    define InInitialPopulation:
                      Criterion
                    """);
        }

        @Nested
        class Inclusion {

            @Test
            void oneDisjunctionWithOneCriterion() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define Criterion:
                          true
                        
                        define InInitialPopulation:
                          Criterion
                        """);
            }

            @Test
            void oneDisjunctionWithTwoCriteria() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE, Criterion.FALSE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define "Criterion 2":
                          false
                        
                        define InInitialPopulation:
                          "Criterion 1" or
                          "Criterion 2"
                        """);
            }

            @Test
            void twoDisjunctionsWithOneCriterionEach() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define "Criterion 2":
                          false
                        
                        define InInitialPopulation:
                          "Criterion 1" and
                          "Criterion 2"
                        """);
            }

            @Test
            void twoDisjunctionsWithTwoCriterionEach() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE, Criterion.TRUE),
                        List.of(Criterion.FALSE, Criterion.FALSE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define "Criterion 2":
                          true
                        
                        define "Criterion 3":
                          false
                        
                        define "Criterion 4":
                          false
                        
                        define InInitialPopulation:
                          ("Criterion 1" or
                          "Criterion 2") and
                          ("Criterion 3" or
                          "Criterion 4")
                        """);
            }
        }

        @Nested
        class InclusionAndExclusion {

            @Test
            void oneConjunctionWithOneCriterion() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)), List.of(List.of(Criterion.FALSE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define Inclusion:
                          "Criterion 1"
                        
                        define "Criterion 2":
                          false
                        
                        define Exclusion:
                          "Criterion 2"
                        
                        define InInitialPopulation:
                          Inclusion and
                          not Exclusion
                        """);
            }

            @Test
            void oneConjunctionWithTwoCriteria() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)),
                        List.of(List.of(Criterion.FALSE, Criterion.FALSE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define Inclusion:
                          "Criterion 1"
                        
                        define "Criterion 2":
                          false
                        
                        define "Criterion 3":
                          false
                        
                        define Exclusion:
                          "Criterion 2" and
                          "Criterion 3"
                        
                        define InInitialPopulation:
                          Inclusion and
                          not Exclusion
                        """);
            }

            @Test
            void twoConjunctionsWithOneCriterionEach() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)),
                        List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE)));


                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define Inclusion:
                          "Criterion 1"
                        
                        define "Criterion 2":
                          true
                        
                        define "Criterion 3":
                          false
                        
                        define Exclusion:
                          "Criterion 2" or
                          "Criterion 3"
                        
                        define InInitialPopulation:
                          Inclusion and
                          not Exclusion
                        """);
            }

            @Test
            void twoConjunctionsWithTwoCriterionEach() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE)),
                        List.of(List.of(Criterion.FALSE, Criterion.FALSE),
                                List.of(Criterion.FALSE, Criterion.FALSE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define Inclusion:
                          "Criterion 1"
                        
                        define "Criterion 2":
                          false
                        
                        define "Criterion 3":
                          false
                        
                        define "Criterion 4":
                          false
                        
                        define "Criterion 5":
                          false
                        
                        define Exclusion:
                          "Criterion 2" and
                          "Criterion 3" or
                          "Criterion 4" and
                          "Criterion 5"
                        
                        define InInitialPopulation:
                          Inclusion and
                          not Exclusion
                        """);
            }

            @Test
            void twoInclusionAndTwoExclusionCriteria() {
                var structuredQuery = StructuredQuery.of(List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE)),
                        List.of(List.of(Criterion.TRUE, Criterion.FALSE)));

                var library = Translator.of().toCql(structuredQuery);

                assertThat(library).patientContextPrintsTo("""
                        context Patient
                        
                        define "Criterion 1":
                          true
                        
                        define "Criterion 2":
                          false
                        
                        define Inclusion:
                          "Criterion 1" and
                          "Criterion 2"
                        
                        define "Criterion 3":
                          true
                        
                        define "Criterion 4":
                          false
                        
                        define Exclusion:
                          "Criterion 3" and
                          "Criterion 4"
                        
                        define InInitialPopulation:
                          Inclusion and
                          not Exclusion
                        """);
            }
        }
    }
}
