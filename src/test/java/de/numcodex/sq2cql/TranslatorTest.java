package de.numcodex.sq2cql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.ConceptNode;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.Library;
import de.numcodex.sq2cql.model.structured_query.CodingModifier;
import de.numcodex.sq2cql.model.structured_query.ConceptCriterion;
import de.numcodex.sq2cql.model.structured_query.Criterion;
import de.numcodex.sq2cql.model.structured_query.NumericCriterion;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import de.numcodex.sq2cql.model.structured_query.TranslationException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alexander Kiel
 */
class TranslatorTest {

    public static final TermCode ROOT = TermCode.of("", "", "");
    public static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71",
            "Malignant neoplasm of brain");
    public static final TermCode C71_0 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.0", "");
    public static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1", "");
    public static final TermCode PLATELETS = TermCode.of("http://loinc.org", "26515-7", "Platelets");
    public static final TermCode FRAILTY_SCORE = TermCode.of("http://snomed.info/sct", "713636003",
            "Canadian Study of Health and Aging Clinical Frailty Scale score");
    public static final TermCode VERY_FIT = TermCode.of(
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", "1", "Very Fit");
    public static final TermCode WELL = TermCode.of(
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", "2", "Well");
    public static final TermCode COPD = TermCode.of("http://snomed.info/sct", "13645005",
            "Chronic obstructive lung disease (disorder)");
    public static final TermCode G47_31 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "G47.31",
            "Obstruktives Schlafapnoe-Syndrom");
    public static final TermCode TOBACCO_SMOKING_STATUS = TermCode.of("http://loinc.org", "72166-2",
            "Tobacco smoking status");
    public static final TermCode CURRENT_EVERY_DAY_SMOKER = TermCode.of("http://loinc.org", "LA18976-3",
            "Current every day smoker");
    public static final TermCode HYPERTENSION = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "I10",
            "Essential (Primary) Hypertension");
    public static final TermCode SERUM = TermCode.of("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "Serum",
            "Serum");
    public static final TermCode TMZ = TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "L01AX03",
            "Temozolomide");
    public static final TermCode LIPID = TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "C10AA",
            "lipid lowering drugs");
    public static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");

    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10",
            "http://loinc.org", "loinc",
            "https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample",
            "http://fhir.de/CodeSystem/dimdi/atc", "atc",
            "http://snomed.info/sct", "snomed",
            "http://hl7.org/fhir/administrative-gender", "gender",
            "http://terminology.hl7.org/CodeSystem/condition-ver-status", "ver_status",
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", "frailty-score");
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void toCQL_Inclusion_OneDisjunctionWithOneCriterion() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE))));

        assertEquals("true", library.getExpressionDefinitions().get(0).getExpression().print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_OneDisjunctionWithTwoCriteria() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE, Criterion.FALSE))));

        assertEquals("true or\nfalse", library.getExpressionDefinitions().get(0).getExpression()
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_TwoDisjunctionsWithOneCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE))));

        assertEquals("true and\nfalse", library.getExpressionDefinitions().get(0).getExpression()
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_TwoDisjunctionsWithTwoCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE, Criterion.TRUE), List.of(Criterion.FALSE, Criterion.FALSE))));

        assertEquals("(true or\ntrue) and\n(false or\nfalse)", library.getExpressionDefinitions().get(0)
                .getExpression().print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_And_Exclusion_OneConjunctionWithOneCriterion() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE)),
                List.of(List.of(Criterion.FALSE))));

        assertEquals("define Inclusion:\n  true", library.getExpressionDefinitions().get(0).print(PrintContext.ZERO));
        assertEquals("define Exclusion:\n  false", library.getExpressionDefinitions().get(1).print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_And_Exclusion_OneConjunctionWithTwoCriteria() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE)),
                List.of(List.of(Criterion.TRUE, Criterion.FALSE))));

        assertEquals("define Exclusion:\n  true and\n  false", library.getExpressionDefinitions().get(1)
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_And_Exclusion_TwoConjunctionsWithOneCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE)),
                List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE))));

        assertEquals("true or\nfalse", library.getExpressionDefinitions().get(1).getExpression()
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_And_Exclusion_TwoConjunctionsWithTwoCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE)),
                List.of(List.of(Criterion.TRUE, Criterion.TRUE), List.of(Criterion.FALSE, Criterion.FALSE))));

        assertEquals("true and\ntrue or\nfalse and\nfalse", library.getExpressionDefinitions().get(1)
                .getExpression().print(PrintContext.ZERO));
    }

    @Test
    void toCQL_NonExpandableConcept() {
        var message = assertThrows(TranslationException.class, () -> Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(ConceptCriterion.of(C71)))))).getMessage();

        assertEquals("Failed to expand concept with system `http://fhir.de/CodeSystem/dimdi/icd-10-gm`, code `C71` and display `Malignant neoplasm of brain`.", message);
    }

    @Test
    void toCQL_NonMappableConcept() {
        var conceptTree = ConceptNode.of(C71, ConceptNode.of(C71_0),
                ConceptNode.of(C71_1));
        var mappingContext = MappingContext.of(Map.of(), conceptTree, CODE_SYSTEM_ALIASES);

        var message = assertThrows(TranslationException.class, () -> Translator.of(mappingContext).toCql(StructuredQuery.of(
                List.of(List.of(ConceptCriterion.of(C71)))))).getMessage();

        assertEquals("Mapping for concept with system `http://fhir.de/CodeSystem/dimdi/icd-10-gm`, code `C71.0` and display `` not found.", message);
    }

    @Test
    void toCQL_Usage_Documentation() {
        var c71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1", "Malignant neoplasm of brain");
        var mappings = Map.of(c71_1, Mapping.of(c71_1, "Condition", ""));
        var conceptTree = ConceptNode.of(c71_1);
        var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
        var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);

        Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
                List.of(ConceptCriterion.of(c71_1)))));

        assertEquals("""
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                   
                codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
                                
                define InInitialPopulation:
                  exists [Condition: Code 'C71.1' from icd10]
                """, library.print(PrintContext.ZERO));
    }

    @Test
    @Disabled
    void toCQL_TimeContraint() {
        var c71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1", "Malignant neoplasm of brain");
        var mappings = Map.of(c71_1, Mapping.of(c71_1, "Condition", ""));
        var conceptTree = ConceptNode.of(c71_1);
        var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
        var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);

        Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
                List.of(ConceptCriterion.of(c71_1)))));

        assertEquals("""
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                   
                codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
                                
                define InInitialPopulation:
                  exists from [Condition: Code 'C71.1' from icd10] C
                  where FHIRHelpers.ToDateTime(C.onset) < @2006
                """, library.print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Test_Task1() throws IOException {
        Map<TermCode,Mapping> mapping = loadMapping("codex-term-code-mapping.json");
        /*
        var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation", ""),
                C71_0, Mapping.of(C71_0, "Condition", ""),
                C71_1, Mapping.of(C71_1, "Condition", ""),
                TMZ, Mapping.of(TMZ, "MedicationStatement", ""));#
         */
        var conceptTree = ConceptNode.of(ROOT, ConceptNode.of(TMZ), ConceptNode.of(C71, ConceptNode.of(C71_0),
                ConceptNode.of(C71_1)));
        var mappingContext = MappingContext.of(mapping, conceptTree, CODE_SYSTEM_ALIASES);

        StructuredQuery structuredQuery = mapper.readValue(slurp("Task1.json"), StructuredQuery.class);

        Library library = Translator.of(mappingContext).toCql(structuredQuery);

        assertEquals("""
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'

                codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'
                codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
                codesystem loinc: 'http://loinc.org'
                codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'

                define InInitialPopulation:
                  (exists from [Condition: Code 'C71.0' from icd10] C
                    where C.verificationStatus.coding contains Code 'confirmed' from ver_status or
                  exists from [Condition: Code 'C71.1' from icd10] C
                    where C.verificationStatus.coding contains Code 'confirmed' from ver_status) and
                  exists from [Observation: Code '26515-7' from loinc] O
                    where O.value as Quantity < 50 'g/dL' and
                  exists [MedicationStatement: Code 'L01AX03' from atc]
                """, library.print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Test_Task2() throws IOException {

        Map<TermCode,Mapping> mapping = loadMapping("codex-term-code-mapping.json");
        /*
        var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation", ""),
                HYPERTENSION, Mapping.of(HYPERTENSION, "Condition", ""),
                SERUM, Mapping.of(SERUM, "Specimen", ""),
                LIPID, Mapping.of(LIPID, "MedicationStatement", ""));
         */
        var conceptTree = ConceptNode.of(ROOT, ConceptNode.of(HYPERTENSION), ConceptNode.of(SERUM),
                ConceptNode.of(LIPID));
        var mappingContext = MappingContext.of(mapping,
                conceptTree,
                CODE_SYSTEM_ALIASES);

        StructuredQuery structuredQuery = mapper.readValue(slurp("Task2.json"), StructuredQuery.class);

        Library library = Translator.of(mappingContext).toCql(structuredQuery);

        assertEquals("""
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'

                codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'
                codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'
                codesystem sample: 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType'
                codesystem ver_status: 'http://terminology.hl7.org/CodeSystem/condition-ver-status'

                define Inclusion:
                  exists from [Condition: Code 'I10' from icd10] C
                    where C.verificationStatus.coding contains Code 'confirmed' from ver_status and
                  exists [Specimen: Code 'Serum' from sample]

                define Exclusion:
                  exists [MedicationStatement: Code 'C10AA' from atc]

                define InInitialPopulation:
                  Inclusion and
                  not Exclusion
                """, library.print(PrintContext.ZERO));
    }

    @Test
    void toCQL_GeccoTask2() throws IOException {

        Map<TermCode,Mapping> mapping = loadMapping("codex-term-code-mapping.json");
        /*
        var mappings = Map.of(FRAILTY_SCORE, Mapping.of(FRAILTY_SCORE, "Observation", ""),
                COPD, Mapping.of(COPD, "Condition", "",CodingModifier.of("verificationStatus", CONFIRMED)),
                G47_31, Mapping.of(G47_31, "Condition", "",CodingModifier.of("verificationStatus", CONFIRMED)),
                TOBACCO_SMOKING_STATUS, Mapping.of(TOBACCO_SMOKING_STATUS, "Observation", ""));

         */
        var conceptTree = ConceptNode.of(ROOT, ConceptNode.of(COPD), ConceptNode.of(G47_31));
        var mappingContext = MappingContext.of(mapping,
                conceptTree,
                CODE_SYSTEM_ALIASES);

        StructuredQuery structuredQuery = mapper.readValue(slurp("GeccoTask2.json"), StructuredQuery.class);

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
                                
                define Inclusion:
                  exists from [Observation: Code '713636003' from snomed] O
                    where O.value.coding contains Code '1' from frailty-score or
                      O.value.coding contains Code '2' from frailty-score
                                
                define Exclusion:
                  exists from [Condition: Code '13645005' from snomed] C
                    where C.verificationStatus.coding contains Code 'confirmed' from ver_status and
                  exists from [Condition: Code 'G47.31' from icd10] C
                    where C.verificationStatus.coding contains Code 'confirmed' from ver_status or
                  exists from [Observation: Code '72166-2' from loinc] O
                    where O.value.coding contains Code 'LA18976-3' from loinc
                                
                define InInitialPopulation:
                  Inclusion and
                  not Exclusion
                """, library.print(PrintContext.ZERO));
    }

    @Test
    void toCQL_mapJSONMapping() throws IOException {
        Map<TermCode, Mapping> loadedMap = loadMapping("mapping_test.json");

        var mappings = Map.of(
                COPD, Mapping.of(COPD, "Condition", null,CodingModifier.of("verificationStatus", CONFIRMED)),
                TOBACCO_SMOKING_STATUS, Mapping.of(TOBACCO_SMOKING_STATUS, "Observation", "valueConcept"));

        assertEquals(loadedMap.keySet(),mappings.keySet());

        var loadedMap_ValueArray = new ArrayList<>( loadedMap.values() );
        var mappings_ValueArray = new ArrayList<>( mappings.values() );
        for(int i = 0; i<mappings.size(); i++){
            var loadedMap_ValueEntry = loadedMap_ValueArray.get(i);
            var mappings_ValueEntry = mappings_ValueArray.get(i);

            assertTrue(loadedMap_ValueEntry.getConcept().equals(mappings_ValueEntry.getConcept()));
            assertTrue(loadedMap_ValueEntry.getFixedCriteria().equals(mappings_ValueEntry.getFixedCriteria()));
            assertTrue(loadedMap_ValueEntry.getResourceType().equals(mappings_ValueEntry.getResourceType()));
            assertTrue(loadedMap_ValueEntry.getValueFhirPath().equals(mappings_ValueEntry.getValueFhirPath()));
        }
    }



    @Test
    void toCQL_readMapping() throws IOException {
        Map<TermCode, Mapping> map = loadMapping("codex-term-code-mapping.json");
    }

    private Map<TermCode,Mapping> loadMapping(String file) throws IOException {
        InputStream inputStream = TranslatorTest.class.getResourceAsStream(file);

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Mapping> sourceMappingEntries = objectMapper.readValue(inputStream, new TypeReference<>() {});

        Map<TermCode, Mapping> mappings = new HashMap<>();
        sourceMappingEntries.forEach(sourceMappingEntry -> mappings.put(sourceMappingEntry.getTermCode(), sourceMappingEntry));

        return mappings;
    }

    private static String slurp(String name) throws IOException {
        try (InputStream in = TranslatorTest.class.getResourceAsStream(name)) {
             return new String(Objects.requireNonNull(in).readAllBytes(), UTF_8);
        }
    }
}
