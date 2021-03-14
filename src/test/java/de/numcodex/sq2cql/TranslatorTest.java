package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.ConceptNode;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.Library;
import de.numcodex.sq2cql.model.structured_query.ConceptCriterion;
import de.numcodex.sq2cql.model.structured_query.Criterion;
import de.numcodex.sq2cql.model.structured_query.NumericCriterion;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public static final TermCode HYPERTENSION = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "I10",
            "Essential (Primary) Hypertension");
    public static final TermCode SERUM = TermCode.of("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "Serum",
            "Serum");
    public static final TermCode TMZ = TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "L01AX03",
            "Temozolomide");
    public static final TermCode LIPID = TermCode.of("http://fhir.de/CodeSystem/dimdi/atc", "C10AA",
            "lipid lowering drugs");

    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10",
            "http://loinc.org", "loinc",
            "https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample",
            "http://fhir.de/CodeSystem/dimdi/atc", "atc",
            "http://snomed.info/sct", "snomed",
            "http://hl7.org/fhir/administrative-gender", "gender");

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

        assertEquals("(true) or\n(false)", library.getExpressionDefinitions().get(0).getExpression()
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_TwoDisjunctionsWithOneCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE))));

        assertEquals("(true) and\n(false)", library.getExpressionDefinitions().get(0).getExpression()
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_TwoDisjunctionsWithTwoCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE, Criterion.TRUE), List.of(Criterion.FALSE, Criterion.FALSE))));

        assertEquals("((true) or\n(true)) and\n((false) or\n(false))", library.getExpressionDefinitions().get(0)
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

        assertEquals("define Exclusion:\n  (true) and\n  (false)", library.getExpressionDefinitions().get(1)
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_And_Exclusion_TwoConjunctionsWithOneCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE)),
                List.of(List.of(Criterion.TRUE), List.of(Criterion.FALSE))));

        assertEquals("(true) or\n(false)", library.getExpressionDefinitions().get(1).getExpression()
                .print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Inclusion_And_Exclusion_TwoConjunctionsWithTwoCriterionEach() {
        Library library = Translator.of().toCql(StructuredQuery.of(
                List.of(List.of(Criterion.TRUE)),
                List.of(List.of(Criterion.TRUE, Criterion.TRUE), List.of(Criterion.FALSE, Criterion.FALSE))));

        assertEquals("((true) and\n(true)) or\n((false) and\n(false))", library.getExpressionDefinitions().get(1)
                .getExpression().print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Usage_Documentation() {
        var neoplasm = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71", "Malignant neoplasm of brain");
        var mappings = Map.of(neoplasm, Mapping.of(neoplasm, "Condition"));
        var conceptTree = ConceptNode.of(neoplasm);
        var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
        var mappingContext = MappingContext.of(mappings, conceptTree, codeSystemAliases);

        Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
                List.of(ConceptCriterion.of(neoplasm)))));

        assertEquals("""
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                   
                codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'                
                                
                define InInitialPopulation:
                  exists([Condition: Code 'C71' from icd10])
                """, library.print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Test_Task1() {
        var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation"),
                C71_0, Mapping.of(C71_0, "Condition"),
                C71_1, Mapping.of(C71_1, "Condition"),
                TMZ, Mapping.of(TMZ, "MedicationStatement"));
        var conceptTree = ConceptNode.of(ROOT, List.of(ConceptNode.of(TMZ),
                ConceptNode.of(C71, List.of(ConceptNode.of(C71_0), ConceptNode.of(C71_1)))));
        var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);

        Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
                List.of(ConceptCriterion.of(C71)),
                List.of(NumericCriterion.of(PLATELETS, LESS_THAN, BigDecimal.valueOf(50), "g/dl")),
                List.of(ConceptCriterion.of(TMZ)))));

        assertEquals("""
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                   
                codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'                
                codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'                
                codesystem loinc: 'http://loinc.org'
                                
                define InInitialPopulation:
                  ((exists([Condition: Code 'C71.0' from icd10])) or
                  (exists([Condition: Code 'C71.1' from icd10]))) and 
                  (exists(from [Observation: Code '26515-7' from loinc] O
                    where (O.value as Quantity) < (50 'g/dl'))) and
                  (exists([MedicationStatement: Code 'L01AX03' from atc]))
                """, library.print(PrintContext.ZERO));
    }

    @Test
    void toCQL_Test_Task2() {
        var mappings = Map.of(PLATELETS, Mapping.of(PLATELETS, "Observation"),
                HYPERTENSION, Mapping.of(HYPERTENSION, "Condition"),
                SERUM, Mapping.of(SERUM, "Specimen"),
                LIPID, Mapping.of(LIPID, "MedicationStatement"));
        var conceptTree = ConceptNode.of(ROOT, List.of(ConceptNode.of(HYPERTENSION),
                ConceptNode.of(SERUM), ConceptNode.of(LIPID)));
        var mappingContext = MappingContext.of(mappings,
                conceptTree,
                CODE_SYSTEM_ALIASES);

        Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
                List.of(ConceptCriterion.of(HYPERTENSION)),
                List.of(ConceptCriterion.of(SERUM))), List.of(
                List.of(ConceptCriterion.of(LIPID)))));

        assertEquals("""
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                         
                codesystem atc: 'http://fhir.de/CodeSystem/dimdi/atc'          
                codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'                
                codesystem sample: 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType'                
                                
                define Inclusion:
                  (exists([Condition: Code 'I10' from icd10])) and 
                  (exists([Specimen: Code 'Serum' from sample]))
                                
                define Exclusion:
                  exists([MedicationStatement: Code 'C10AA' from atc])                
                                
                define InInitialPopulation:
                  (Inclusion) and 
                  (not (Exclusion))
                """, library.print(PrintContext.ZERO));
    }
}
