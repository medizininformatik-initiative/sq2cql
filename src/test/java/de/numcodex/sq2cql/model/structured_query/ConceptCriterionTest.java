package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.ConceptNode;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class ConceptCriterionTest {

    public static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71",
            "Malignant neoplasm of brain");
    public static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1",
            "Frontal lobe");
    public static final TermCode C71_2 = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.2",
            "Temporal lobe");
    public static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    public static final TermCode THERAPEUTIC_PROCEDURE = TermCode.of("http://snomed.info/sct",
            "277132007", "Therapeutic procedure (procedure)");
    public static final CodeSystemDefinition ICD10_CODE_SYSTEM_DEF = CodeSystemDefinition.of("icd10",
            "http://fhir.de/CodeSystem/dimdi/icd-10-gm");
    public static final CodeSystemDefinition SNOMED_CODE_SYSTEM_DEF = CodeSystemDefinition.of("snomed",
            "http://snomed.info/sct");
    public static final CodeSystemDefinition VER_STATUS_CODE_SYSTEM_DEF = CodeSystemDefinition.of("ver_status",
            "http://terminology.hl7.org/CodeSystem/condition-ver-status");
    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10",
            "http://snomed.info/sct", "snomed",
            "http://terminology.hl7.org/CodeSystem/condition-ver-status", "ver_status");

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (ConceptCriterion) mapper.readValue("""
                {
                  "termCode": {
                    "system": "http://fhir.de/CodeSystem/dimdi/icd-10-gm", 
                    "code": "C71",
                    "display": "Malignant neoplasm of brain"
                  }
                }
                """, Criterion.class);

        assertEquals(C71, criterion.getTermCode());
    }

    @Test
    void fromJson_WithModifier() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (ConceptCriterion) mapper.readValue("""
                {
                  "termCode": {
                    "system": "http://fhir.de/CodeSystem/dimdi/icd-10-gm", 
                    "code": "C71",
                    "display": "Malignant neoplasm of brain"
                  }
                }
                """, Criterion.class);

        assertEquals(C71, criterion.getTermCode());
    }

    @Test
    void toCql() {
        Criterion criterion = ConceptCriterion.of(C71);
        var mappingContext = MappingContext.of(Map.of(C71, Mapping.of(C71, "Condition")), ConceptNode.of(C71),
                CODE_SYSTEM_ALIASES);

        Container<BooleanExpression> container = criterion.toCql(mappingContext);

        assertEquals("exists [Condition: Code 'C71' from icd10]", container.getExpression()
                .map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithModifier() {
        Criterion criterion = ConceptCriterion.of(C71, CodingModifier.of("verificationStatus", CONFIRMED));
        var mappingContext = MappingContext.of(Map.of(C71, Mapping.of(C71, "Condition")), ConceptNode.of(C71),
                CODE_SYSTEM_ALIASES);

        Container<BooleanExpression> container = criterion.toCql(mappingContext);

        assertEquals("""
                        exists from [Condition: Code 'C71' from icd10] C
                          where C.verificationStatus.coding contains Code 'confirmed' from ver_status""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF, VER_STATUS_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_FixedCriteria_Code() {
        Criterion criterion = ConceptCriterion.of(THERAPEUTIC_PROCEDURE);
        var mappingContext = MappingContext.of(Map.of(THERAPEUTIC_PROCEDURE, Mapping.of(THERAPEUTIC_PROCEDURE,
                "Procedure", CodeModifier.of("status", "completed", "in-progress"))),
                ConceptNode.of(THERAPEUTIC_PROCEDURE), CODE_SYSTEM_ALIASES);

        Container<BooleanExpression> container = criterion.toCql(mappingContext);

        assertEquals("""
                        exists from [Procedure: Code '277132007' from snomed] P
                          where P.status in { 'completed', 'in-progress' }""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(SNOMED_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_FixedCriteria_Coding() {
        Criterion criterion = ConceptCriterion.of(C71);
        var mappingContext = MappingContext.of(Map.of(C71, Mapping.of(C71, "Condition",
                CodingModifier.of("verificationStatus", CONFIRMED))), ConceptNode.of(C71), CODE_SYSTEM_ALIASES);

        Container<BooleanExpression> container = criterion.toCql(mappingContext);

        assertEquals("""
                        exists from [Condition: Code 'C71' from icd10] C
                          where C.verificationStatus.coding contains Code 'confirmed' from ver_status""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF, VER_STATUS_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_Expanded_WithModifier() {
        Criterion criterion = ConceptCriterion.of(C71, CodingModifier.of("verificationStatus", CONFIRMED));
        var mappingContext = MappingContext.of(Map.of(C71_1, Mapping.of(C71_1, "Condition"), C71_2, Mapping.of(C71_2,
                "Condition")), ConceptNode.of(C71, ConceptNode.of(C71_1), ConceptNode.of(C71_2)), CODE_SYSTEM_ALIASES);

        Container<BooleanExpression> container = criterion.toCql(mappingContext);

        assertEquals("""
                        exists from [Condition: Code 'C71.1' from icd10] C
                          where C.verificationStatus.coding contains Code 'confirmed' from ver_status or
                        exists from [Condition: Code 'C71.2' from icd10] C
                          where C.verificationStatus.coding contains Code 'confirmed' from ver_status""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF, VER_STATUS_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }
}
